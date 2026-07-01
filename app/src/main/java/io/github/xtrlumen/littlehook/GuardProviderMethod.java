package io.github.xtrlumen.littlehook;

import android.util.Log;

import java.lang.reflect.Method;

import org.luckypray.dexkit.DexKitBridge;

import org.luckypray.dexkit.query.FindMethod;

import org.luckypray.dexkit.query.matchers.MethodMatcher;

import org.luckypray.dexkit.result.MethodData;
import org.luckypray.dexkit.result.MethodDataList;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import static io.github.xtrlumen.littlehook.Entry.*;

public class GuardProviderMethod {
    private static final String CLASS = "[GuardProviderMethod] ";
    public void onPackageReady(XposedModule XposedBridge, PackageReadyParam param) {
        if (!(disable_root_check || disable_upload_applist)) {
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Ignored Hook");
            return;
        }
        ClassLoader classLoader = param.getClassLoader();
        // 禁用环境检查
        if (disable_root_check) try {
            System.loadLibrary("dexkit");
            DexKitBridge dexIndex = DexKitBridge.create(param.getApplicationInfo().sourceDir);
            MethodDataList mCheckRootMethodList = dexIndex.findMethod(FindMethod.create()
                .matcher(MethodMatcher.create()
                    .returnType(boolean.class)
                    .usingStrings("/system/bin/")
                )
            );
            dexIndex.close();
            for (MethodData methodData : mCheckRootMethodList) {
                Method targetMethod = methodData.getMethodInstance(classLoader);
                XposedBridge.log(Log.DEBUG, TAG, CLASS + "Method Matched: " + targetMethod);
                XposedBridge.hook(targetMethod).intercept(chain -> {
                    return false;
                });
            }
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "DisableRootedCheck failed: ", t);
        }
        // 阻止自动上传已安装应用列表
        if (disable_upload_applist) try {
            System.loadLibrary("dexkit");
            DexKitBridge dexIndex = DexKitBridge.create(param.getApplicationInfo().sourceDir);
            Method mAntiDefraudAppManagerMethod = dexIndex.findMethod(FindMethod.create()
                .matcher(MethodMatcher.create() 
                    .usingStrings(
                        "AntiDefraudAppManager",
                        "https://flash.sec.miui.com/detect/app"
                    )
                )
            ).get(0).getMethodInstance(classLoader);
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Target Method Matched: " + mAntiDefraudAppManagerMethod);
            dexIndex.close();
            XposedBridge.hook(mAntiDefraudAppManagerMethod).intercept(chain -> {
                return null;
            });
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "DisableUploadAppList failed: ", t);
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }
}