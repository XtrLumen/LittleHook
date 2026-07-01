package io.github.xtrlumen.littlehook;

import android.content.Context;

import android.util.Log;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import static io.github.xtrlumen.littlehook.Entry.*;

public class LbeSecurityMethod {
    private static final String CLASS = "[LbeSecurityMethod] ";
    public void onPackageReady(XposedModule XposedBridge, PackageReadyParam param) {
        if (!(lbe_auto_start)) {
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Ignored Hook");
            return;
        }
        ClassLoader classLoader = param.getClassLoader();
        // 禁止自动关闭自启动
        if (lbe_auto_start) try {
            Class<?> targetClass = classLoader.loadClass(
                "com.miui.privacy.autostart.AutoRevokePermissionManager"
            );
            Method targetMethod = targetClass.getDeclaredMethod(
                "lambda$startScheduleASCheck$1",
                Context.class,
                boolean.class
            );
            XposedBridge.hook(targetMethod).intercept(chain -> {
                return null;
            });
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "Prohibit auto close AutoStart permission Module Hook failed: ", t);
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }
}