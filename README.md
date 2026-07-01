# LittleHook
自用 LSPosed API101 模块。

## 条件
- 系统版本为 Xiaomi Hyper OS 3
- 已安装正常工作的LSPosed框架

## 安装
1. 安装模块APK。
2. 在LSPosed框架的模块页面启用模块并勾选静态作用域。
3. 重新启动设备。
4. 完成！

## 功能
- 彻底关闭 Splash Screen
  - 作用域 `system`
- 恢复并锁定原生软件包安装器
  - 作用域 `system` `com.miui.securitycore`
- 全局伪装开发者相关选项为关闭
  - 作用域 `system`
- 在不允许截图的应用中强制允许截图
  - 作用域 `system` `com.android.systemui`
- 禁用系统桌面触碰图标时预加载应用
  - 作用域 `com.miui.home`
- 禁用环境检查，阻止自动上传已安装应用列表
  - 作用域 `com.miui.guardprovider`
- 使 Xiaomi 17 Ultra 标准版识别徕卡版定制主题
  - 作用域 `com.android.thememanager`

## 致谢
- [LuckyPray/DexKit](https://github.com/LuckyPray/DexKit)
- [5ec1cff/MyInjector](https://github.com/5ec1cff/MyInjector)
- [frknkrc44/HMA-OSS](https://github.com/frknkrc44/HMA-OSS)
- [ReChronoRain/HyperCeiler](https://github.com/ReChronoRain/HyperCeiler)
- [LSPosed/DisableFlagSecure](https://github.com/LSPosed/DisableFlagSecure)
- [GSWXXN/RestoreSplashScreen](https://github.com/GSWXXN/RestoreSplashScreen)
- [Howard20181/HyperOS_PM_Free_Choose](https://github.com/Howard20181/HyperOS_PM_Free_Choose)