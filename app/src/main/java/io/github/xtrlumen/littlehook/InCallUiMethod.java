
package io.github.xtrlumen.littlehook;

import miui.process.ProcessManager;
import miui.process.ForegroundInfo;

import android.content.Context;

import android.util.Log;

import java.util.List;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import static io.github.xtrlumen.littlehook.Entry.*;

public class InCallUiMethod {
    private static final String CLASS = "[InCallUiMethod] ";
    public void onPackageReady(XposedModule XposedBridge, PackageReadyParam param, ClassLoader classLoader) {
        if (!(incallui_answer_in_head_up)) {
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Ignored Hook");
            return;
        }
        // 从浮动通知接听电话时不进入全屏
        if (incallui_answer_in_head_up) try {
            Class<?> targetClass = classLoader.loadClass("com.android.incallui.InCallPresenter");
            Method targetMethod = targetClass.getDeclaredMethod(
                "answerIncomingCall",
                Context.class,
                String.class,
                int.class,
                boolean.class
            );

            XposedBridge.hook(targetMethod).intercept(chain -> {
                if (incallui_answer_in_head_up_desktop) {
                    boolean fullScreen = (boolean) chain.getArg(3);
                    if (fullScreen) {
                        ForegroundInfo foregroundInfo = ProcessManager.getForegroundInfo();
                        if (foregroundInfo != null) {
                            if (!("com.miui.home".equals(foregroundInfo.mForegroundPackageName))) {
                                List<Object> Args = chain.getArgs();
                                Object[] newArgs = Args.toArray();
                                newArgs[3] = false;
                                return chain.proceed(newArgs);
                            }
                        }
                    }
                    return chain.proceed();
                } else {
                    List<Object> Args = chain.getArgs();
                    Object[] newArgs = Args.toArray();
                    newArgs[3] = false;
                    return chain.proceed(newArgs);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "InCallUi Module Hook failed: ", t);
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }
}