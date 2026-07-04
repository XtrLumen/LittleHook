package io.github.xtrlumen.littlehook;

import android.util.Log;

import android.app.Activity;
import android.app.AlertDialog;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import static io.github.xtrlumen.littlehook.Entry.*;

public class SystemUiMethod {
    private static final String CLASS = "[SystemUiMethod] ";
    public void onPackageReady(XposedModule XposedBridge, PackageReadyParam param, ClassLoader classLoader) {
        if (!(disable_flag_secure)) {
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Ignored Hook");
            return;
        }
        // 在不允许截图的应用中强制允许截图
        if (disable_flag_secure) try {
            Method method = Activity.class.getDeclaredMethod("onResume");
            XposedBridge.hook(method).intercept(chain -> {
                Activity activity = (Activity) chain.getThisObject();
                new AlertDialog.Builder(activity)
                    .setTitle("Enable Screenshot")
                    .setMessage("Incorrect module usage, remove this app from scope.")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> System.exit(0))
                    .show();
                return chain.proceed();
            });
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "'disable_flag_secure' Module Hook failed: ", t);
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }
}