package io.github.xtrlumen.littlehook;

import android.util.Log;

import io.github.libxposed.api.XposedModule;

public class Entry extends XposedModule {
    static final String TAG = "LittleHook";
    static final boolean
        system_settings_unlock_google_header = true,
        html_viewer_disable_cloud_control    = true,
        incallui_answer_in_head_up           = true,
        incallui_answer_in_head_up_desktop   = false,
        various_fuck_xlDownload              = true,
        disable_upload_applist               = true,
        disable_flag_secure                  = true,
        disable_root_check                   = true,
        adb_developer_hide                   = true,
        native_file_picker                   = true,
        package_installer                    = true,
        desktop_hide_clear_button            = true,
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
        ClassLoader
            classLoader = param.getClassLoader();
        switch (packageName) {
            case "com.android.htmlviewer":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new HtmlViewerMethod().onPackageReady(this, param, classLoader);
                break;
            case "com.android.incallui":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new InCallUiMethod().onPackageReady(this, param, classLoader);
                break;
            case "com.android.photopicker":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new PhotoPickerMethod().onPackageReady(this, param, classLoader);
                break;
            case "com.android.providers.downloads":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new DownloadsMethod().onPackageReady(this, param, classLoader);
                break;
            case "com.android.settings":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new SettingsMethod().onPackageReady(this, param, classLoader);
                break;
            case "com.android.systemui":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new SystemUiMethod().onPackageReady(this, param, classLoader);
                break;
            case "com.android.thememanager":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new ThemeGlobal().onPackageReady(this, param, classLoader);
                break;
            case "com.lbe.security.miui":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new LbeSecurityMethod().onPackageReady(this, param, classLoader);
                break;
            case "com.miui.guardprovider":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new GuardProviderMethod().onPackageReady(this, param, classLoader);
                break;
            case "com.miui.home":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new DesktopGlobal().onPackageReady(this, param, classLoader);
                new DesktopMethod().onPackageReady(this, param, classLoader);
                break;
            case "com.miui.securitycore":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new SecurityCoreMethod().onPackageReady(this, param, classLoader);
                break;
            default:
                log(Log.DEBUG, TAG, "Ignored " + packageName + " From " + onTiming);
                break;
        }
    }
    @Override
    public void onSystemServerStarting(SystemServerStartingParam param) {
        String onTiming = "onSystemServerStarting";
        ClassLoader classLoader = param.getClassLoader();
        log(Log.DEBUG, TAG, "Loaded into system From " + onTiming);
        new FrameworkMethod().onSystemServerStarting(this, param, classLoader);
        new FrameworkGlobal().onSystemServerStarting(this, param, classLoader);
    }
}