package io.github.xtrlumen.littlehook;

import android.util.Log;

import io.github.libxposed.api.XposedModule;

public class Entry extends XposedModule {
    static final String TAG = "LittleHook";
    static final boolean
        system_settings_unlock_google_header = true,
        html_viewer_disable_cloud_control    = true,
        incallui_answer_in_head_up           = true,
        disable_upload_applist               = true,
        disable_flag_secure                  = true,
        disable_root_check                   = true,
        adb_developer_hide                   = true,
        package_installer                    = true,
        desktop_prestart                     = true,
        lbe_auto_start                       = true,
        splash_screen                        = true,
        leica_theme                          = true;
    @Override
    public void onModuleLoaded(ModuleLoadedParam param) {
    }
    @Override
    public void onPackageLoaded(PackageLoadedParam param) {
    }
    @Override
    public void onPackageReady(PackageReadyParam param) {
        String
            packageName = param.getPackageName(),
            onTiming = "onPackageReady";
        switch (packageName) {
            case "com.android.incallui":
                log(Log.DEBUG, TAG, onTiming + " Loaded into " + packageName);
                new InCallUiMethod().onPackageReady(this, param);
                break;
            case "com.lbe.security.miui":
                log(Log.DEBUG, TAG, onTiming + " Loaded into " + packageName);
                new LbeSecurityMethod().onPackageReady(this, param);
                break;
            case "com.android.settings":
                log(Log.DEBUG, TAG, onTiming + " Loaded into " + packageName);
                new SettingsMethod().onPackageReady(this, param);
                break;
            case "com.miui.guardprovider":
                log(Log.DEBUG, TAG, onTiming + " Loaded into " + packageName);
                new GuardProviderMethod().onPackageReady(this, param);
                break;
            case "com.miui.securitycore":
                log(Log.DEBUG, TAG, onTiming + " Loaded into " + packageName);
                new SecurityCoreMethod().onPackageReady(this, param);
                break;
            case "com.android.systemui":
                log(Log.DEBUG, TAG, onTiming + " Loaded into " + packageName);
                new SystemUiMethod().onPackageReady(this, param);
                break;
            case "com.miui.home":
                log(Log.DEBUG, TAG, onTiming + " Loaded into " + packageName);
                new DesktopGlobal().onPackageReady(this, param);
                break;
            case "com.android.thememanager":
                log(Log.DEBUG, TAG, onTiming + " Loaded into " + packageName);
                new ThemeGlobal().onPackageReady(this, param);
                break;
            case "com.android.htmlviewer":
                log(Log.DEBUG, TAG, onTiming + " Loaded into " + packageName);
                new HtmlViewerMethod().onPackageReady(this, param);
                break;
            default:
                log(Log.DEBUG, TAG, onTiming + " Ignored " + packageName);
                break;
        }
    }
    @Override
    public void onSystemServerStarting(SystemServerStartingParam param) {
        String onTiming = "onSystemServerStarting";
        log(Log.DEBUG, TAG, onTiming + " Loaded into system");
        new FrameworkMethod().onSystemServerStarting(this, param);
        new FrameworkGlobal().onSystemServerStarting(this, param);
    }
}