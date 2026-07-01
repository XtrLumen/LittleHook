
package io.github.xtrlumen.littlehook;

import android.content.Context;

import android.util.Log;

import java.util.List;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import static io.github.xtrlumen.littlehook.Entry.*;

public class InCallUiMethod {
    private static final String CLASS = "[InCallUiMethod] ";
    public void onPackageReady(XposedModule XposedBridge, PackageReadyParam param) {
        if (!(incallui_answer_in_head_up)) {
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Ignored Hook");
            return;
        }
        ClassLoader classLoader = param.getClassLoader();
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
                boolean fullScreen = (boolean) chain.getArg(3);
                if (fullScreen) {
                    Class<?> processManager = classLoader.loadClass("miui.process.ProcessManager");
                    Method getForegroundInfo = processManager.getDeclaredMethod("getForegroundInfo");
                    Object foregroundInfo = getForegroundInfo.invoke(null);
                    if (foregroundInfo != null) {
                        String topPackage = (String) foregroundInfo.getClass().getDeclaredField("mForegroundPackageName").get(foregroundInfo);
                        if (!"com.miui.home".equals(topPackage)) {
                            List<Object> Args = chain.getArgs();
                            Object[] newArgs = Args.toArray();
                            newArgs[3] = false;
                            return chain.proceed(newArgs);
                        }
                    }
                }
                return chain.proceed();
            });
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "InCallUi Module Hook failed: ", t);
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }
}
