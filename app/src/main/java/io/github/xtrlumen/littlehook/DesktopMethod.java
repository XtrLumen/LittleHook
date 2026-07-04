package io.github.xtrlumen.littlehook;

import android.os.SystemProperties;

import android.content.Context;

import android.widget.TextView;

import android.util.Log;
import android.util.AttributeSet;

import java.io.FileReader;
import java.io.BufferedReader;

import java.text.DecimalFormat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import static io.github.xtrlumen.littlehook.Entry.*;

public class DesktopMethod {
    private static final String CLASS = "[DesktopMethod] ";
    public void onPackageReady(XposedModule XposedBridge, PackageReadyParam param, ClassLoader classLoader) {
        if (!(desktop_hide_clear_button || desktop_real_memory_usage)) {
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Ignored Hook");
            return;
        }
        if (desktop_hide_clear_button || desktop_real_memory_usage) {
            Class<?> tmpClass;
            try {
                tmpClass = classLoader.loadClass("com.miui.home.recents.views.RecentsContainer");
            } catch (ClassNotFoundException ignore1) {
                try {
                    tmpClass = classLoader.loadClass("com.miui.home.recents.views.RecentsDecorations");
                } catch (ClassNotFoundException ignore2) {
                    return;
                }
            } catch (Throwable t) {
                XposedBridge.log(Log.ERROR, TAG, CLASS + "Find Recents class failed: ", t);
                return;
            }
            final Class<?> TARGET_CLASS = tmpClass;

            // 隐藏最近任务界面清理按钮
            if (desktop_hide_clear_button) try {
                Method targetMethod = TARGET_CLASS.getDeclaredMethod("isClearContainerVisible");
                XposedBridge.hook(targetMethod).intercept(chain -> false);
            } catch (Throwable t) {
                XposedBridge.log(Log.ERROR, TAG, CLASS + "Hide Clear Button Module Hook failed: ", t);
            }
            // 最近任务界面显示内存真实用量
            if (desktop_real_memory_usage) try {
                final Context[] tmpContext = new Context[1];
                final int[] memoryInfo1StringId = new int[1];
                final int[] memoryInfo2StringId = new int[1];

                Constructor<?> targetConstructor = TARGET_CLASS.getDeclaredConstructor(
                    Context.class,
                    AttributeSet.class
                );
                XposedBridge.hook(targetConstructor).intercept(chain -> {
                    chain.proceed();
                    tmpContext[0] = (Context) chain.getArg(0);
                    memoryInfo1StringId[0] = tmpContext[0].getResources().getIdentifier(
                        "status_bar_recent_memory_info1",
                        "string",
                        "com.miui.home"
                    );
                    memoryInfo2StringId[0] = tmpContext[0].getResources().getIdentifier(
                        "status_bar_recent_memory_info2",
                        "string",
                        "com.miui.home"
                    );
                    return null;
                });

                Method refreshMethod = TARGET_CLASS.getDeclaredMethod("refreshMemoryInfo");
                XposedBridge.hook(refreshMethod).intercept(chain -> {
                    Context context = tmpContext[0];

                    final DecimalFormat decimalFormat = new DecimalFormat("0.0");
                    String availMem = "";
                    String availSwap = "";
                    String totalMem = "";
                    String extmMem = "";
                    try (BufferedReader bufferedReader = new BufferedReader(new FileReader("/proc/meminfo"))) {
                        double memAvailable = 0;
                        double swapFree = 0;
                        double memTotal = 0;
                        double swapTotal = 0;
                        for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                            if (line.startsWith("MemAvailable:")) memAvailable = Long.parseLong(line.split("\\s+")[1]) / (1024.0 * 1024.0);
                            if (line.startsWith("SwapFree:"    )) swapFree     = Long.parseLong(line.split("\\s+")[1]) / (1024.0 * 1024.0);
                            if (line.startsWith("MemTotal:"    )) memTotal     = Long.parseLong(line.split("\\s+")[1]) / (1024.0 * 1024.0);
                            if (line.startsWith("SwapTotal:"   )) swapTotal    = Long.parseLong(line.split("\\s+")[1]) / (1024.0 * 1024.0);
                        }

                        availMem = decimalFormat.format(memAvailable);
                        totalMem = decimalFormat.format(memTotal);
                        boolean extmEnabled = "1".equals(SystemProperties.get("persist.miui.extm.enable", "0"));
                        if (extmEnabled && swapTotal > 0) {
                            extmMem = "+" + decimalFormat.format(swapTotal);
                            availSwap = "+" + decimalFormat.format(swapFree);
                        }
                    } catch (Exception e) {
                        XposedBridge.log(Log.ERROR, TAG, CLASS + "Format /proc/meminfo failed: ", e);
                    }
                    String availPart = availMem + availSwap + " G";
                    String totalPart = totalMem + extmMem + " G";

                    Object thisObject = chain.getThisObject();
                    Field mTxtMemoryInfo1 = TARGET_CLASS.getDeclaredField("mTxtMemoryInfo1");
                    mTxtMemoryInfo1.setAccessible(true);
                    TextView textView1 = (TextView) mTxtMemoryInfo1.get(thisObject);
                    textView1.setText(context.getResources().getString(memoryInfo1StringId[0], availPart, totalPart));
                    Field mTxtMemoryInfo2 = TARGET_CLASS.getDeclaredField("mTxtMemoryInfo2");
                    mTxtMemoryInfo2.setAccessible(true);
                    TextView textView2 = (TextView) mTxtMemoryInfo2.get(thisObject);
                    textView2.setText(context.getResources().getString(memoryInfo2StringId[0], availPart, totalPart));

                    return null;
                });
            } catch (Throwable t) {
                XposedBridge.log(Log.ERROR, TAG, CLASS + "Display real memory usage Module Hook failed: ", t);
            }
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }
}