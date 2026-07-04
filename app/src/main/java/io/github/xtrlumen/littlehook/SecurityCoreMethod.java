package io.github.xtrlumen.littlehook;

import android.util.Log;

import java.util.ArrayList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import static io.github.xtrlumen.littlehook.Entry.*;

public class SecurityCoreMethod {
    private static final String CLASS = "[SecurityCoreMethod] ";
    public void onPackageReady(XposedModule XposedBridge, PackageReadyParam param, ClassLoader classLoader) {
        if (!(package_installer)) {
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Ignored Hook");
            return;
        }
        // 恢复并锁定原生软件包安装器
        if (package_installer) try {
            Class<?> XSpaceConstantClass = classLoader.loadClass("miui.securityspace.XSpaceConstant");
            Field requiredAppsField = XSpaceConstantClass.getDeclaredField("REQUIRED_APPS");
            requiredAppsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            ArrayList<String> requiredApps = (ArrayList<String>) requiredAppsField.get(null);
            if (requiredApps != null && !requiredApps.contains("com.android.packageinstaller")) {
                requiredApps.add("com.android.packageinstaller");
            }
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "'package_installer' Module Hook failed: ", t);
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }
}