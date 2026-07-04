package io.github.xtrlumen.littlehook;

import android.util.Log;

import java.util.List;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import static io.github.xtrlumen.littlehook.Entry.*;

public class PhotoPickerMethod {
    private static final String CLASS = "[PhotoPickerMethod] ";
    public void onPackageReady(XposedModule XposedBridge, PackageReadyParam param, ClassLoader classLoader) {
        if (!(native_file_picker)) {
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Ignored Hook");
            return;
        }
        // 强制原生文件选择器
        if (native_file_picker) try {
            Class<?> deviceConfigClass = classLoader.loadClass("android.provider.DeviceConfig");
            Method targetMethod = deviceConfigClass.getDeclaredMethod(
                "getBoolean",
                String.class,
                String.class,
                boolean.class
            );
            XposedBridge.hook(targetMethod).intercept(chain -> {
                Object result = chain.proceed();

                String namespace = (String) chain.getArg(0);
                String prop = (String) chain.getArg(1);
                if ("securitycenter".equals(namespace) && ("hyper_refer_file_picker".equals(prop))) {
                    XposedBridge.log(Log.DEBUG, TAG, CLASS + "hyper_refer_file_picker -> false");
                    return false;
                }

                return result;
            });
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "'native_file_picker' Module Hook failed: ", t);
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }
}