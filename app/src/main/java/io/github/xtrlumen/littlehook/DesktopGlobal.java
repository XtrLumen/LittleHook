package io.github.xtrlumen.littlehook;

import android.util.Log;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import static io.github.xtrlumen.littlehook.Entry.*;

public class DesktopGlobal {
    private static final String CLASS = "[DesktopGlobal] ";
    public void onPackageReady(XposedModule XposedBridge, PackageReadyParam param) {
        if (!(desktop_prestart)) {
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Ignored Hook");
            return;
        }
        ClassLoader classLoader = param.getClassLoader();
        // 禁用系统桌面触碰图标时预加载应用
        if (desktop_prestart) try {
            Class<?> targetClass = classLoader.loadClass("android.os.SystemProperties");
            Method targetMethod = targetClass.getDeclaredMethod(
                "getBoolean",
                String.class,
                Boolean.TYPE
            );
            XposedBridge.hook(targetMethod).intercept(chain -> {
                Object result = chain.proceed();
                if ("persist.sys.prestart.proc".equals(chain.getArg(0))) {
                    result = false;
                    XposedBridge.log(Log.DEBUG, TAG, CLASS + "persist.sys.prestart.proc -> " + result);
                }
                return result;
            });
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "'android.os.SystemProperties.getBoolean' Module Hook failed: ", t);
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }
}