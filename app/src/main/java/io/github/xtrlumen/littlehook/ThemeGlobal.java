package io.github.xtrlumen.littlehook;

import android.util.Log;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import static io.github.xtrlumen.littlehook.Entry.*;

public class ThemeGlobal {
    private static final String CLASS = "[ThemeGlobal] ";
    public void onPackageReady(XposedModule XposedBridge, PackageReadyParam param, ClassLoader classLoader) {
        if (!(leica_theme)) {
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Ignored Hook");
            return;
        }
        // 使 Xiaomi 17 Ultra 标准版识别徕卡版定制主题
        if (leica_theme) try {
            Class<?> targetClass = classLoader.loadClass("android.os.SystemProperties");
            Method targetMethod = targetClass.getDeclaredMethod(
                "get",
                String.class,
                String.class
            );
            XposedBridge.hook(targetMethod).intercept(chain -> {
                Object result = chain.proceed();
                if ("ro.boot.product.theme_customize".equals(chain.getArg(0))) {
                    result = "P1_Leica";
                    XposedBridge.log(Log.DEBUG, TAG, CLASS + "ro.boot.product.theme_customize -> " + result);
                }
                return result;
            });
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "'android.os.SystemProperties.get' Module Hook failed: ", t);
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }
}