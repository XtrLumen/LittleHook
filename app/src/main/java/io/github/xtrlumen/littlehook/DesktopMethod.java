package io.github.xtrlumen.littlehook;

import android.util.Log;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import static io.github.xtrlumen.littlehook.Entry.*;

public class DesktopMethod {
    private static final String CLASS = "[DesktopMethod] ";
    public void onPackageReady(XposedModule XposedBridge, PackageReadyParam param, ClassLoader classLoader) {
        if (!(desktop_hide_clear_button)) {
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Ignored Hook");
            return;
        }
        // 隐藏最近任务的清理按钮
        if (desktop_hide_clear_button) try {
            Class<?> targetClass;
            try {
                targetClass = classLoader.loadClass("com.miui.home.recents.views.RecentsContainer");
            } catch (ClassNotFoundException ingored) {
                targetClass = classLoader.loadClass("com.miui.home.recents.views.RecentsDecorations");
            } catch (Throwable t) {
                XposedBridge.log(Log.ERROR, TAG, CLASS + "Find Clear Button Display class failed: ", t);
                return;
            }
            Method targetMethod = targetClass.getDeclaredMethod("isClearContainerVisible");
            XposedBridge.hook(targetMethod).intercept(chain -> false);
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "Hide Clear Button Module Hook failed: ", t);
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }
}