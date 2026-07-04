
package io.github.xtrlumen.littlehook;

import android.content.Context;

import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import static io.github.xtrlumen.littlehook.Entry.*;

public class DownloadsMethod {
    private static final String CLASS = "[DownloadsMethod] ";
    public void onPackageReady(XposedModule XposedBridge, PackageReadyParam param, ClassLoader classLoader) {
        if (!(various_fuck_xlDownload)) {
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Ignored Hook");
            return;
        }
        // 阻止创建.xlDownload文件夹
        if (various_fuck_xlDownload) try {
            Class<?> xlConfigClass = classLoader.loadClass("com.android.providers.downloads.config.XLConfig");
            for (Method targetMethod : xlConfigClass.getDeclaredMethods()) {
                String methodName = targetMethod.getName();
                switch (methodName) {
                    case "setDebug":
                    case "setSoDebug":
                        XposedBridge.hook(targetMethod).intercept(chain -> null);
                        break;
                }
            }

            Class<?> fileUtilClass = classLoader.loadClass("com.android.providers.downloads.util.FileUtil");
            Method createFileMethod = fileUtilClass.getDeclaredMethod(
                "createFile",
                String.class
            );
            XposedBridge.hook(createFileMethod).setExceptionMode(ExceptionMode.PASSTHROUGH).intercept(chain -> {
                String directoryName = (String) chain.getArg(0);
                if (directoryName.contains(".xlDownload")) {
                    XposedBridge.log(Log.DEBUG, TAG, CLASS + createFileMethod.getName() + "() -> IOException");
                    throw new IOException(".xlDownload is blocked");
                }

                return chain.proceed();
            });
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "'various_fuck_xlDownload' Module Hook failed: ", t);
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }
}