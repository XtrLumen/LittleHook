# LittleHook
自用 LSPosed API101 模块

## 条件
- 系统版本为 Xiaomi Hyper OS 3
- 已安装正常工作的LSPosed框架

## 功能介绍

### 单作用域功能

- 作用域 `com.android.htmlviewer`
  - 禁用HTML查看器云控

- 作用域 `com.android.incallui`
  - 从浮动通知接听电话时不进入全屏

- 作用域 `com.android.providers.downloads`
  - 阻止创建.xlDownload文件夹

- 作用域 `com.android.settings`
  - 禁止隐藏Google入口

- 作用域 `com.android.thememanager`
  - 使 Xiaomi 17 Ultra 标准版识别徕卡版定制主题

- 作用域 `com.lbe.security.miui`
  - 禁止自动关闭自启动

- 作用域 `com.miui.guardprovider`
  - 禁用环境检查
  - 阻止自动上传已安装应用列表

- 作用域 `com.miui.home`
  - 隐藏最近任务界面清理按钮
  - 最近任务界面显示内存真实用量
  - 禁用系统桌面触碰图标时预加载应用

- 作用域 `system`
  - 彻底关闭 Splash Screen
  - 全局伪装开发者相关选项为关闭

### 多作用域功能

- 强制原生文件选择器
  - 作用域 `system` `com.android.photopicker`

- 在不允许截图的应用中强制允许截图
  - 作用域 `system` `com.android.systemui`

- 恢复并锁定原生软件包安装器
  - 作用域 `system` `com.miui.securitycore`

## 致谢
- [LuckyPray/DexKit](https://github.com/LuckyPray/DexKit)
- [5ec1cff/MyInjector](https://github.com/5ec1cff/MyInjector)
- [frknkrc44/HMA-OSS](https://github.com/frknkrc44/HMA-OSS)
- [ReChronoRain/HyperCeiler](https://github.com/ReChronoRain/HyperCeiler)
- [LSPosed/DisableFlagSecure](https://github.com/LSPosed/DisableFlagSecure)
- [GSWXXN/RestoreSplashScreen](https://github.com/GSWXXN/RestoreSplashScreen)
- [Howard20181/HyperOS_PM_Free_Choose](https://github.com/Howard20181/HyperOS_PM_Free_Choose)