package io.github.xtrlumen.littlehook;

import android.util.Log;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import static io.github.xtrlumen.littlehook.Entry.*;

public class HtmlViewerMethod {
    private static final String CLASS = "[HtmlViewerMethod] ";
    public void onPackageReady(XposedModule XposedBridge, PackageReadyParam param) {
        if (!(html_viewer_disable_cloud_control)) {
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Ignored Hook");
            return;
        }
        ClassLoader classLoader = param.getClassLoader();
        // 禁用云控
        if (html_viewer_disable_cloud_control) try {
            Class<?> targetClass = classLoader.loadClass("com.android.settings.cloud.JobTask");
            Method targetMethod = targetClass.getDeclaredMethod("updateCloudAllData");
            XposedBridge.hook(targetMethod).intercept(chain -> {
                return null;
            });
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "HTML Viewer Disable Cloud Control Module Hook failed: ", t);
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }
}