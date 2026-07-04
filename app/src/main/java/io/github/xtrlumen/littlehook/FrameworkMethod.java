package io.github.xtrlumen.littlehook;

import android.util.Log;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;

import android.hardware.display.DisplayManager;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam;

import static io.github.xtrlumen.littlehook.Entry.*;

public class FrameworkMethod {
    private static final String CLASS = "[FrameworkMethod] ";
    public void onSystemServerStarting(XposedModule XposedBridge, SystemServerStartingParam param, ClassLoader classLoader) {
        if (!(disable_flag_secure || native_file_picker || package_installer || splash_screen)) {
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Ignored Hook");
            return;
        }
        // 在不允许截图的应用中强制允许截图
        if (disable_flag_secure) try {
            class HookHelper {
                private void deoptimizeMethods(Class<?> clazz, String... names) {
                    List<String> list = Arrays.asList(names);
                    Arrays.stream(clazz.getDeclaredMethods())
                        .filter(method -> list.contains(method.getName()))
                        .forEach(XposedBridge::deoptimize);
                }
                private void hookMethods(Class<?> clazz, Hooker hooker, String... names) {
                    List<String> list = Arrays.asList(names);
                    Arrays.stream(clazz.getDeclaredMethods())
                        .filter(method -> list.contains(method.getName()))
                        .forEach(method -> XposedBridge.hook(method).intercept(hooker));
                }
            }
            HookHelper hookHelper = new HookHelper();
            try {
                // deoptimizeSystemServer
                hookHelper.deoptimizeMethods(
                    classLoader.loadClass("com.android.server.wm.WindowStateAnimator"),
                    "createSurfaceLocked"
                );
                hookHelper.deoptimizeMethods(
                    classLoader.loadClass("com.android.server.wm.WindowManagerService"),
                    "relayoutWindow"
                );
                for (int i = 0; i < 20; i++) {
                    try {
                        Class<?> clazz = classLoader.loadClass("com.android.server.wm.RootWindowContainer$$ExternalSyntheticLambda" + i);
                        if (BiConsumer.class.isAssignableFrom(clazz)) {
                            hookHelper.deoptimizeMethods(clazz, "accept");
                        }
                    } catch (ClassNotFoundException ignored) {
                    }
                    try {
                        Class<?> clazz = classLoader.loadClass("com.android.server.wm.DisplayContent$" + i);
                        if (BiPredicate.class.isAssignableFrom(clazz)) {
                            hookHelper.deoptimizeMethods(clazz, "test");
                        }
                    } catch (ClassNotFoundException ignored) {
                    }
                }
            } catch (Throwable t) {
                XposedBridge.log(Log.ERROR, TAG, CLASS + "deoptimize system server failed", t);
            }

            // Screen record detection (V~Baklava)
            try {
                // hookWindowManagerService
                Class<?> windowManagerServiceClazz = classLoader.loadClass("com.android.server.wm.WindowManagerService");
                Class<?> iScreenRecordingCallbackClazz = classLoader.loadClass("android.window.IScreenRecordingCallback");
                Method method = windowManagerServiceClazz.getDeclaredMethod("registerScreenRecordingCallback", iScreenRecordingCallbackClazz);
                XposedBridge.hook(method).intercept(chain -> false);
            } catch (Throwable t) {
                XposedBridge.log(Log.ERROR, TAG, CLASS + "hook WindowManagerService failed", t);
            }

            // Screenshot detection (U~Baklava)
            try {
                // hookActivityTaskManagerService
                Class<?> activityTaskManagerServiceClazz = classLoader.loadClass("com.android.server.wm.ActivityTaskManagerService");
                Class<?> iBinderClazz = classLoader.loadClass("android.os.IBinder");
                Class<?> iScreenCaptureObserverClazz = classLoader.loadClass("android.app.IScreenCaptureObserver");
                Method method = activityTaskManagerServiceClazz.getDeclaredMethod("registerScreenCaptureObserver", iBinderClazz, iScreenCaptureObserverClazz);
                XposedBridge.hook(method).intercept(chain -> null);
            } catch (Throwable t) {
                XposedBridge.log(Log.ERROR, TAG, CLASS + "hook ActivityTaskManagerService failed", t);
            }

            // Xiaomi HyperOS (U~Baklava)
            // OS2.0.300.1.WOCCNXM
            try {
                // hookHyperOS
                Class<?> windowManagerServiceImplClazz = classLoader.loadClass("com.android.server.wm.WindowManagerServiceImpl");
                hookHelper.hookMethods(windowManagerServiceImplClazz, chain -> false, "notAllowCaptureDisplay");
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable t) {
                XposedBridge.log(Log.ERROR, TAG, CLASS + "hook HyperOS failed", t);
            }

            // ScreenCapture in WindowManagerService (S~Baklava)
            try {
                // hookScreenCapture
                Class<?> screenCaptureClazz = classLoader.loadClass("android.window.ScreenCapture");
                Class<?> captureArgsClazz = classLoader.loadClass("android.window.ScreenCapture$CaptureArgs");
                Field captureSecureLayersField = captureArgsClazz.getDeclaredField("mCaptureSecureLayers");
                captureSecureLayersField.setAccessible(true);
                Hooker hooker = chain -> {
                    Object captureArgs = chain.getArg(0);
                    try {
                        captureSecureLayersField.set(captureArgs, true);
                    } catch (IllegalAccessException t) {
                        XposedBridge.log(Log.ERROR, TAG, CLASS + "ScreenCaptureHooker failed", t);
                    }
                    return chain.proceed();
                };
                hookHelper.hookMethods(screenCaptureClazz, hooker, "nativeCaptureDisplay");
                hookHelper.hookMethods(screenCaptureClazz, hooker, "nativeCaptureLayers");
            } catch (Throwable t) {
                XposedBridge.log(Log.ERROR, TAG, CLASS + "hook ScreenCapture failed", t);
            }

            // WifiDisplay (S~Baklava) / OverlayDisplay (S~Baklava) / VirtualDisplay (U~Baklava)
            try {
                // hookDisplayControl
                Class<?> displayControlClazz = classLoader.loadClass("com.android.server.display.DisplayControl");
                ClassLoader systemServerCl = displayControlClazz.getClassLoader();
                Method method = displayControlClazz.getDeclaredMethod("createVirtualDisplay", String.class, boolean.class);
                XposedBridge.hook(method).intercept(chain -> {
                    Object[] args = chain.getArgs().toArray();
                    args[1] = true;
                    return chain.proceed(args);
                });
            } catch (Throwable t) {
                XposedBridge.log(Log.ERROR, TAG, CLASS + "hook DisplayControl failed", t);
            }

            // VirtualDisplay with MediaProjection (S~Baklava)
            try {
                // hookVirtualDisplayAdapter
                Class<?> displayControlClazz = classLoader.loadClass("com.android.server.display.VirtualDisplayAdapter");
                hookHelper.hookMethods(displayControlClazz, chain -> {
                    int caller = (int) chain.getArg(2);
                    if (caller >= 10000 && chain.getArg(1) == null) {
                        // not os and not media projection
                        return chain.proceed();
                    }
                    for (int i = 3; i < chain.getArgs().size(); i++) {
                        Object arg = chain.getArg(i);
                        if (arg instanceof Integer) {
                            int flags = (Integer) arg;
                            flags |= DisplayManager.VIRTUAL_DISPLAY_FLAG_SECURE;
                            Object[] args = chain.getArgs().toArray();
                            args[i] = flags;
                            return chain.proceed(args);
                        }
                    }
                    XposedBridge.log(Log.WARN, TAG, "flag not found in CreateVirtualDisplayLockedHooker");
                    return chain.proceed();
                }, "createVirtualDisplayLocked");
            } catch (Throwable t) {
                XposedBridge.log(Log.ERROR, TAG, CLASS + "hook VirtualDisplayAdapter failed", t);
            }

            // secureLocked flag
            try {
                // Screenshot
                // hookWindowState
                Class<?>  windowStateClazz = classLoader.loadClass("com.android.server.wm.WindowState");
                ClassLoader systemServerCl = windowStateClazz.getClassLoader();
                Method isSecureLockedMethod = windowStateClazz.getDeclaredMethod("isSecureLocked");
                XposedBridge.hook(isSecureLockedMethod).intercept(chain -> {
                    StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
                    boolean match = walker.walk(frames -> frames
                        .anyMatch(frame -> frame.getDeclaringClass() != null &&
                            frame.getDeclaringClass().getClassLoader() == systemServerCl &&
                            (frame.getMethodName().equals("setInitialSurfaceControlProperties") ||
                                frame.getMethodName().equals("createSurfaceLocked"))));
                    if (match) return chain.proceed();
                    return false;
                });
            } catch (Throwable t) {
                XposedBridge.log(Log.ERROR, TAG, CLASS + "hook WindowState failed", t);
            }
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "'disable_flag_secure' Module Hook failed: ", t);
        }
        // 强制原生文件选择器
        if (native_file_picker) try {
            Class<?> targetClass = classLoader.loadClass("com.android.server.wm.ActivityTaskManagerServiceImpl");
            Method targetMethod = targetClass.getDeclaredMethod(
                "mayReferToFileExplore",
                Intent.class,
                String.class
            );
            XposedBridge.hook(targetMethod).intercept(chain -> {
                return chain.getArg(0);
            });
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "'native_file_picker' Module Hook failed: ", t);
        }
        // 恢复并锁定原生软件包安装器
        if (package_installer) try {
            Class<?> packageManagerServiceImpl = classLoader.loadClass(
                "com.android.server.pm.PackageManagerServiceImpl"
            );
            Field finalFCurrentPackageInstaller = packageManagerServiceImpl.getDeclaredField(
                "mCurrentPackageInstaller"
            );
            finalFCurrentPackageInstaller.setAccessible(true);
            AtomicBoolean fakeCTS = new AtomicBoolean(false);
            AtomicReference<String> mCurrentPackageInstaller = new AtomicReference<>("");
            for (Method targetMethod : packageManagerServiceImpl.getDeclaredMethods()) {
                String methodName = targetMethod.getName();
                if ("updateDefaultPkgInstallerLocked".equals(methodName)) {
                    XposedBridge.hook(targetMethod).intercept(chain -> {
                        fakeCTS.set(true);
                        if (finalFCurrentPackageInstaller != null && mCurrentPackageInstaller.get().isEmpty() && finalFCurrentPackageInstaller.get(chain.getThisObject()) instanceof String currentPackageInstaller) {
                            mCurrentPackageInstaller.compareAndSet("", currentPackageInstaller);
                        }
                        try {
                            return chain.proceed();
                        } finally {
                            fakeCTS.set(false);
                        }
                    });
                    XposedBridge.deoptimize(targetMethod);
                }
                if ("assertValidApkAndInstaller".equals(methodName)) {
                    XposedBridge.hook(targetMethod).intercept(chain -> {
                        fakeCTS.set(true);
                        try {
                            return chain.proceed();
                        } finally {
                            fakeCTS.set(false);
                        }
                    });
                    XposedBridge.deoptimize(targetMethod);
                }
                if ("hookChooseBestActivity".equals(methodName)) {
                    XposedBridge.hook(targetMethod).intercept(chain -> {
                        try {
                            if (chain.getArg(0) instanceof Intent intent) {
                                if (finalFCurrentPackageInstaller != null && "application/vnd.android.package-archive".equals(intent.getType()) && Intent.ACTION_VIEW.equals(intent.getAction())) {
                                    fakeCTS.set(true);
                                }
                            }
                            return chain.proceed();
                        } finally {
                            fakeCTS.set(false);
                        }
                    });
                    XposedBridge.deoptimize(targetMethod);
                }
            }
            try {
                Method isCTSMethod = packageManagerServiceImpl.getDeclaredMethod("isCTS");
                XposedBridge.hook(isCTSMethod).intercept(chain -> {
                    if (fakeCTS.get()) {
                        return true;
                    }
                    return chain.proceed();
                });
            } catch (Throwable t) {
                XposedBridge.log(Log.ERROR, TAG, CLASS + "Failed to hook isCTS", t);
            }
            try {
                Class<?> XSpaceConstantClass = classLoader.loadClass(
                    "miui.securityspace.XSpaceConstant"
                );
                Field requiredAppsField = XSpaceConstantClass.getDeclaredField(
                    "REQUIRED_APPS"
                );
                requiredAppsField.setAccessible(true);
                @SuppressWarnings("unchecked")
                ArrayList<String> requiredApps = (ArrayList<String>) requiredAppsField.get(null);
                if (requiredApps != null && !requiredApps.contains("com.android.packageinstaller")) {
                    requiredApps.add("com.android.packageinstaller");
                }
            } catch (Throwable t) {
                XposedBridge.log(Log.ERROR, TAG, CLASS + "Failed to add package installer to REQUIRED_APPS", t);
            }
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "'package_installer' Module Hook failed: ", t);
        }
        // 彻底关闭 Splash Screen
        if (splash_screen) try {
            Class<?> activityRecordClass = classLoader.loadClass(
                "com.android.server.wm.ActivityRecord"
            );
            for (Method targetMethod : activityRecordClass.getDeclaredMethods()) {
                if (targetMethod.getName().equals("showStartingWindow") && targetMethod.getParameterCount() == 7) {
                    XposedBridge.hook(targetMethod).intercept(chain -> {
                        return null;
                    });
                    break;
                }
            }
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "'com.android.server.wm.ActivityRecord' Module Hook failed: ", t);
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }
}