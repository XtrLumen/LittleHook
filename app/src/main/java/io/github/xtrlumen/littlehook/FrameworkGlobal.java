package io.github.xtrlumen.littlehook;

import android.content.AttributionSource;

import android.util.Log;

import android.net.Uri;

import android.os.Bundle;

import android.database.Cursor;
import android.database.MatrixCursor;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam;

import static io.github.xtrlumen.littlehook.Entry.*;

public class FrameworkGlobal {
    private static final String CLASS = "[FrameworkGlobal] ";
    public void onSystemServerStarting(XposedModule XposedBridge, SystemServerStartingParam param, ClassLoader classLoader) {
        if (!(adb_developer_hide)) {
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Ignored Hook");
            return;
        }
        // 全局伪装开发者相关选项为关闭
        if (adb_developer_hide) try {
            String notFound = "__NOTFOUND__";
            class SpoofHelper {
                final Set<String> WHITELIST = Set.of(
                    "com.android.settings",
                    "com.android.shell",
                    "android",
                    "root"
                );
                private boolean diffSkipApp(String caller) {
                    return WHITELIST.contains(caller);
                }
                final String[][] DEV_OPTIONS_SPOOF = {
                    {"global", "adb_enabled", "0"},
                    {"global", "adb_wifi_enabled", "0"},
                    {"global", "development_settings_enabled", "0"},
                    {"global", "hidden_api_policy", null},
                    {"global", "hidden_api_policy_p_apps", null},
                    {"global", "hidden_api_policy_pre_p_apps", null},
                    {"global", "hidden_api_policy_blacklist_exemptions", null},
                };
                private String getSpoofValue(String database, String prop) {
                    for (String[] entry : DEV_OPTIONS_SPOOF) {
                        if (entry[0].equals(database) && entry[1].equals(prop)) {
                            return entry[2];
                        }
                    }
                    return notFound;
                }
            }
            SpoofHelper spoofHelper = new SpoofHelper();

            Class<?> transportClass = classLoader.loadClass("android.content.ContentProvider$Transport");
            for (Method targetMethod : transportClass.getDeclaredMethods()) {
                String methodName = targetMethod.getName();
                // 拦截 Settings 单个/列表
                if ("query".equals(methodName)) {
                    XposedBridge.hook(targetMethod).intercept(chain -> {
                        String caller = ((AttributionSource) chain.getArg(0)).getPackageName();
                        if (spoofHelper.diffSkipApp(caller)) return chain.proceed();

                        Uri uri = (Uri) chain.getArg(1);
                        if (!("settings".equals(uri.getAuthority()))) return chain.proceed();

                        List<String> segments = uri.getPathSegments();
                        if (segments.isEmpty()) return chain.proceed();

                        String database = segments.get(0);
                        if (segments.size() >= 2) {
                            String prop = segments.get(1);
                            String spoofed = spoofHelper.getSpoofValue(database, prop);
                            if (!notFound.equals(spoofed)) {
                                XposedBridge.log(Log.DEBUG, TAG, CLASS + "caller: " + caller + ", " + "spoofed " + database + " " + prop + "=" + spoofed);
                                return new MatrixCursor(new String[]{"name", "value"}, 1) {{
                                    addRow(new Object[]{prop, spoofed});
                                }};
                            }
                        } else {
                            Object result = chain.proceed();
                            if (!(result instanceof Cursor cursor)) return result;

                            Map<String, List<String>> columns = new LinkedHashMap<>();
                            for (int i = 0; i < cursor.getColumnCount(); i++) {
                                columns.put(cursor.getColumnName(i), new ArrayList<>());
                            }
                            List<String> keyCol = columns.get("name");
                            List<String> valCol = columns.get("value");
                            if (keyCol == null || valCol == null) return result;

                            while (cursor.moveToNext()) {
                                String prop = cursor.getString(cursor.getColumnIndex("name"));
                                keyCol.add(prop);
                                String spoofed = spoofHelper.getSpoofValue(database, prop);
                                valCol.add(!notFound.equals(spoofed) ? spoofed : cursor.getString(cursor.getColumnIndex("value")));
                                for (String col : columns.keySet()) {
                                    if ("name".equals(col) || "value".equals(col)) continue;
                                    columns.get(col).add(cursor.getString(cursor.getColumnIndex(col)));
                                }
                            }

                            String[] colNames = columns.keySet().toArray(new String[0]);
                            int size = columns.values().iterator().next().size();
                            return new MatrixCursor(colNames, size) {{
                                for (int i = 0; i < size; i++) {
                                    Object[] row = new Object[colNames.length];
                                    for (int j = 0; j < colNames.length; j++) {
                                        row[j] = columns.get(colNames[j]).get(i);
                                    }
                                    addRow(row);
                                }
                            }};
                        }
                        return chain.proceed();
                    });
                }
                // 拦截 GET_global / GET_secure / GET_system
                if ("call".equals(methodName)) {
                    XposedBridge.hook(targetMethod).intercept(chain -> {
                        String caller = ((AttributionSource) chain.getArg(0)).getPackageName();
                        if (spoofHelper.diffSkipApp(caller)) return chain.proceed();

                        String callMethod = (String) chain.getArg(2);
                        String database;
                        switch (callMethod) {
                            case "GET_global":
                                database = "global";
                                break;
                            case "GET_secure":
                                database = "secure";
                                break;
                            case "GET_system":
                                database = "system";
                                break;
                            default:
                                return chain.proceed();
                        }
                        String prop = (String) chain.getArg(3);

                        String spoofed = spoofHelper.getSpoofValue(database, prop);
                        if (!notFound.equals(spoofed)) {
                            Bundle bundle = new Bundle();
                            bundle.putString("value", spoofed);
                            bundle.putInt("_generation_index", -1);
                            XposedBridge.log(Log.DEBUG, TAG, CLASS + "caller: " + caller + ", " + "spoofed " + database + " " + prop + "=" + spoofed);
                            return bundle;
                        }
                        return chain.proceed();
                    });
                }
            }
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "'android.content.ContentProvider$Transport' Module Hook failed: ", t);
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }
}