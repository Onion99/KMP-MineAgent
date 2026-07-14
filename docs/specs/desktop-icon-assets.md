# Desktop Icon Assets

## 目标

将 Android 启动图标 `composeApp/src/androidMain/res/drawable/ic_launcher_round.xml` 作为桌面端图标的单一设计来源，生成 Compose Desktop 原生打包配置引用的三类平台资源：

- Linux: `docs/AppIcon.png`
- Windows: `docs/AppIcon.ico`
- macOS: `docs/AppIcon.icns`
- Desktop runtime window: `composeApp/src/commonMain/composeResources/drawable/app_icon.png`

## 生成方式

运行：

```powershell
python scripts/generate_desktop_icons.py
```

脚本执行流程：

- 解析 Android vector XML，并导出等价的 `docs/AppIcon.svg`。
- 使用本机 Chrome 或 Edge headless 将 SVG 渲染为 1024px 临时 PNG。
- 使用 Pillow 派生 512px Linux PNG、Windows ICO 多尺寸容器和 macOS ICNS 多尺寸容器。
- 校验输出尺寸与 ICNS 文件头，避免将错误格式传给 Gradle/jpackage。

## 构建接入点

`composeApp/build.gradle.kts` 的 `compose.desktop.application.nativeDistributions` 已引用以下路径：

- `linux.iconFile`: `docs/AppIcon.png`
- `windows.iconFile`: `docs/AppIcon.ico`
- `macOS.iconFile`: `docs/AppIcon.icns`

桌面窗口运行时图标在 `composeApp/src/desktopMain/kotlin/org/onion/agro/main.kt` 中通过 `Window(icon = painterResource(Res.drawable.app_icon))` 设置。该设置影响开发运行窗口、标题栏和任务栏图标；`nativeDistributions.iconFile` 仍负责安装包、开始菜单、Dock 或应用文件图标。

后续如果 Android 图标设计变化，应重新运行生成脚本，并同步更新 `composeResources/drawable/app_icon.png` 与 `docs/AppIcon.*` 平台资源。
