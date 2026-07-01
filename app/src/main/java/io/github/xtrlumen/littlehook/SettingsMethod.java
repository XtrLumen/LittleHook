package io.github.xtrlumen.littlehook;

import android.content.Context;

import android.util.Log;

import java.util.List;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import static io.github.xtrlumen.littlehook.Entry.*;

public class SettingsMethod {
    private static final String CLASS = "[SettingsMethod] ";

    public void onPackageReady(XposedModule XposedBridge, PackageReadyParam param) {
        if (!(system_settings_unlock_google_header)) {
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Ignored Hook");
            return;
        }
        ClassLoader classLoader = param.getClassLoader();
        // 禁止隐藏Google入口
        if (system_settings_unlock_google_header) try {
            Class<?> mMiuiSettings = classLoader.loadClass("com.android.settings.MiuiSettings");
            Method targetMethod = mMiuiSettings.getDeclaredMethod(
                "updateHeaderList",
                List.class
            );
            final Method ADD_GOOGLE = mMiuiSettings.getDeclaredMethod(
                "AddGoogleSettingsHeaders",
                List.class
            );
            ADD_GOOGLE.setAccessible(true);

            XposedBridge.hook(targetMethod).intercept(chain -> {
                try {
                    return chain.proceed();
                } finally {
                    List settingsList = (List) chain.getArg(0);
                    ADD_GOOGLE.invoke(chain.getThisObject(), settingsList);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "Prohibit hide Google entry Module Hook failed: ", t);
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }
}