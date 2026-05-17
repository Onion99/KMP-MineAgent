---
trigger: always_on
---

# Kotlin Multiplatform 项目编码规范

当前状态：2026-05-17


## 1. 技术栈概览
- **语言**：Kotlin (Multiplatform)
- **UI 框架**：Compose Multiplatform (Android, iOS, Desktop/JVM)
- **依赖注入**：Koin (v4.1.1)
- **导航**：Jetpack Navigation Compose (v2.9.0-beta04)
- **网络**：Ktor (v3.2.3) + Sandwich (v2.1.2)
- **异步**：Kotlin Coroutines (v1.10.2)
- **图片加载**：Coil3 (v3.3.0)
- **文件 I/O**：FileKit (v0.10.0-beta03)

## 2. 项目结构与模块
- **`composeApp`**：主应用模块。包含功能特性、UI 屏幕、ViewModels 和应用入口点。
- **`ui-theme`**：Ethereal Minimalism 设计系统模块。包含 `Theme.kt`（入口 + Material3 桥接）、`style/`（Color、ColorScheme、Typography、Shape、Spacing、Size、Elevation）、`helper/`、`state/`。
- **`data-network`**：网络层。包含 Ktor 客户端配置 (`NetworkModule.kt`)、API 服务。
- **`data-model`**：领域对象和数据类。
- **`shared`**：共享核心逻辑（如果使用）。
- **`cpp`**：原生 C++ 实现（Stable Diffusion）。

## 3. 架构与模式
### 实现架构 - MVVM
- **View**：位于 `ui/screen/` 中的组合函数（Composables）。
- **ViewModel**：位于 `viewmodel/` 中的 `androidx.lifecycle.ViewModel`。
- **DI**：在 `di/` 中定义的 Koin 模块（例如 `KoinDi.kt`, `ViewModelModule.kt`）。

### 导航
- 使用 **Jetpack Navigation Compose**。
- 路由定义在 `ui/navigation/` 中。
- 推荐使用类型安全的导航参数。

### 数据层
- **网络**：使用 `NetworkModule.kt` 提供的 `HttpClient`。
- **JSON 序列化**：`kotlinx.serialization`（配置为 `ignoreUnknownKeys = true`, `isLenient = true`）。

## 4. 编码规范
### UI 开发
- **样式**：**始终**使用 `ui-theme` 中的值（例如 `MaterialTheme.colorScheme`, `MaterialTheme.typography`）。
- **资源**：使用 `compose.components.resources` 加载图片/字符串。
- **国际化 (i18n)**：
    - **字符串资源**：**所有**面向用户的文本**必须**定义在 [composeApp/src/commonMain/composeResources/values/strings.xml](cci:7://file:///d:/Diffusion/composeApp/src/commonMain/composeResources/values/strings.xml:0:0-0:0) 中。
    - **多语言支持**：在特定语言文件夹中提供翻译（例如中文放在 [values-zh/strings.xml](cci:7://file:///d:/Diffusion/composeApp/src/commonMain/composeResources/values-zh/strings.xml:0:0-0:0)）。
    - **UI 中使用**：在 Composables 中使用 `stringResource(Res.string.key_name)`，**切勿**硬编码字符串。
    - **命名约定**：使用描述性、层级化的键名（例如 `settings_advanced_title`, `settings_flash_attn_desc`）。
    - **导入管理**：从 `minediffusion.composeapp.generated.resources.*` 导入生成的资源键。

### 原生集成

- **JNI (大模型文本)**：对于部署端侧大语言模型（如 Gemma），使用基于 Google LiteRT LM 封装的面向对象架构（位于 `native/llm/`）。
    - **核心类**：`LmEngine` 负责引擎初始化及指针管理，`LmConversation` 桥接异步消息流。
    - **消息传递**：使用 `Message`, `Content`, `ToolCall` 等封装类，基于 `kotlinx.serialization.json` 进行 JSON 解析与构建，并使用 `Flow` 接收异步流式响应，避免直接管理裸指针和原生 JSON 字符串。

## 5. 开发工作流
1.  **新功能**：
    - 一切逻辑代码,力求简洁,高效,优雅
    - 在 `data-model` 中定义数据模型。
    - 如果需要，在 `data-network` 中创建/更新 API。
    - 在 `composeApp/.../viewmodel` 中创建 ViewModel 并在 `ViewModelModule.kt` 中注册。
    - 在 `ui/screen` 中创建`Screen`并在 `ui/navigation` 中添加路由。
2.  **新页面**：
    - 一切UI布局,力求美观,动效,富有创造力,向乔布斯学习
    - 在 `ui/screen` 中创建屏幕，并在 `ui/navigation` 中添加路由
    - 根据**适配策略**：使用 `AppTheme.contentType` 创建不同`ContentType`类型的Compose布局。
        - `ContentType.Single`（移动端）：使用垂直布局（Column）防止过度拥挤。
        - `ContentType.Dual`（桌面端）：使用水平布局（Row）最大化屏幕利用率。 


## 6. 性能编码约束

### 时间复杂度：
1. 算法复杂度必须最优（目标 O(n) 或更优）
2. 禁止在大数据量（>500）下使用 O(n²)

### 循环与计算：
1. 禁止在循环内重复计算
2. 禁止嵌套循环（无必要时）


## 7. 主题设计规范 — Ethereal Minimalism

> 灵感源自游戏 *Gris* 的艺术方向。视觉语言融合 **极简主义** 与 **毛玻璃效果（Glassmorphism）**，以水彩有机纹理柔化界面，营造宁静、成长与沉思之美。

### 7.1 设计系统入口

- **主题对象**：统一通过 `AppTheme.*` 访问所有设计 token。
- **Material3 桥接**：`AppTheme` 内部自动桥接 `MaterialTheme`，所有 M3 组件自动继承 Ethereal Minimalism 色板。
- **默认主题**：亮色模式（`isDark = false`），暖白羊皮纸底色。

```kotlin
// ✅ 正确 — 使用 AppTheme
AppTheme.colors.primary
AppTheme.typography.headlineLarge
AppTheme.shape.xxl
AppTheme.spacing.sectionGap
AppTheme.elevation.glassSurfaceAlpha

// ❌ 禁止 — 硬编码颜色/尺寸
Color(0xFF4A654F)  // 应使用 AppTheme.colors.primary
RoundedCornerShape(32.dp)  // 应使用 AppTheme.shape.xxl
```

### 7.2 调色板

| 角色 | 用途 | 亮色值 |
|------|------|--------|
| **Primary (Sage Green)** | 成长性操作、正向状态 | `#4A654F` |
| **Secondary (Dusty Blue)** | 次要交互、信息元素 | `#466275` |
| **Tertiary (Slate Gray)** | 高对比文字、结构边框 | `#50606F` |
| **Surface (Parchment)** | UI 基底，温暖有机 | `#FCF9F2` |
| **Error** | 错误状态 | `#BA1A1A` |

**颜色使用规则**：
- **渐变**：仅使用水彩混合风格——Primary 与 Secondary 间的低透明度过渡。使用 `Modifier.watercolorGradient()`。
- **禁止**硬编码 `Color()` 值，**始终**引用 `AppTheme.colors.*`。
- 暗色主题使用温暖暗底（twilight parchment `#141311`），**非纯黑**。

### 7.3 排版

- **字体族**：Metropolis（Light / Regular / Medium / SemiBold / Bold）。
- **标题**（headline）：使用 **Light (300)** 字重，营造"空灵"质感。
- **标签**（label）：使用 **SemiBold (600)** + 加宽字间距 `0.05em`，保证功能可读性。
- **正文**（body）：行高 **≥ 1.5x** 字号，维持"空灵留白"叙事。
- **禁止**在 UI 中使用 `TextStyle()` 自定义样式，**始终**使用 `AppTheme.typography.*`。

| Token | 字号 | 字重 | 行高 | 场景 |
|-------|------|------|------|------|
| `headlineLarge` | 40sp | Light | 52sp | 桌面端页面标题 |
| `headlineSmall` | 30sp | Light | 38sp | 移动端页面标题 |
| `headlineMedium` | 28sp | Normal | 36sp | 节标题 |
| `bodyLarge` | 18sp | Normal | 30sp | 正文大段 |
| `bodyMedium` | 16sp | Normal | 26sp | 正文默认 |
| `labelMedium` | 14sp | SemiBold | 20sp | 按钮/标签 |
| `bodySmall` | 12sp | Normal | 16sp | 辅助说明/caption |

### 7.4 形状

使用语义化命名，**禁止**使用旧的 `r100~r500` 命名：

| Token | 圆角 | 用途 |
|-------|------|------|
| `sm` | 4dp | 小元素（输入框内角） |
| `regular` | 8dp | 默认 |
| `md` | 12dp | 中等容器 |
| `lg` | 16dp | 大卡片 |
| `xl` | 24dp | 特大容器 |
| `xxl` | 32dp | 毛玻璃卡片、突出容器 |
| `full` | 9999dp | 胶囊按钮、标签、头像 |

### 7.5 间距

基于 **8px 韵律尺度**，通过大间距创造"呼吸空间"：

| Token | 值 | 用途 |
|-------|-----|------|
| `xs` | 4dp | 紧凑间隙 |
| `sm` | 8dp | 小间距 |
| `md` | 16dp | 中间距 |
| `lg` | 24dp | 大间距（= gutter） |
| `xl` | 32dp | 特大间距 |
| `xxl` | 48dp | 组内分隔 |
| `containerPaddingMobile` | 24dp | 移动端容器内边距 |
| `containerPaddingDesktop` | 64dp | 桌面端容器内边距 |
| `sectionGap` | 80dp | 主要内容区块间距 |

兼容旧字段 `s100~s500` 仍可使用。

### 7.6 尺寸（Size）

语义化尺寸 token，**禁止**使用旧的 `s24~s512` 命名：

| 分组 | Token | 默认值 | 说明 |
|------|-------|--------|------|
| 图标 | `iconSmall / icon / iconLarge` | 12 / 18 / 24dp | 图标尺寸 |
| 图标按钮 | `iconButton / iconButtonSmall` | 40 / 32dp | 可点击图标区域 |
| 组件 | `buttonHeight / chipHeight` | 48 / 32dp | 按钮/标签高度 |
| 头像 | `avatarSmall / Medium / Large` | 32 / 48 / 72dp | 头像尺寸 |
| 布局 | `borderWidth / borderWidthThin` | 1 / 0.5dp | 边框粗细 |
| 布局 | `maxContentWidth` | 1200dp | 最大内容宽度 |
| 卡片 | `cardSmall / Medium / Large` | 160 / 240 / 320dp | 卡片尺寸 |

### 7.7 深度与毛玻璃效果（Elevation）

**避免重阴影**，通过毛玻璃与色调层级传达深度：

| 层级 | 描述 | 实现方式 |
|------|------|----------|
| **基底层** | 羊皮纸底色 + 水彩纹理 | `AppTheme.colors.surface` |
| **表面层** | 半透明白色（60-80%）+ 背景模糊 | `Modifier.glassSurface()` |
| **阴影层** | 环境光阴影，大模糊半径，极低透明度 | 自动包含在 `glassSurface()` 中 |

```kotlin
// ✅ 毛玻璃卡片 — 一行搞定
Box(modifier = Modifier.glassSurface()) { content() }

// ✅ 自定义毛玻璃参数
Box(modifier = Modifier.glassSurface(
    shape = AppTheme.shape.lg,
    alpha = 0.6f,
    borderAlpha = 0.1f,
)) { content() }

// ✅ 水彩渐变背景
Box(modifier = Modifier.watercolorGradient()) { content() }
```

**阴影规则**：
- 模糊半径 ≥ 20dp，透明度 5-8%
- 阴影颜色使用 Slate Gray 着色（`#50606F`），**禁止**纯黑阴影
- **禁止**使用 `elevation` 硬编码数值，使用 `AppTheme.elevation.*`

### 7.8 组件设计指南

| 组件 | 设计规范 |
|------|----------|
| **按钮** | Primary 使用 Sage Green 柔和渐变 + `full` 圆角；次要使用 Ghost（仅描边） |
| **卡片** | 毛玻璃风格：`Modifier.glassSurface()` + `xxl` 圆角 + 1px 柔边框 |
| **输入框** | 低调设计：底部边框或极浅半透明填充；聚焦时边框渐变为 Sage Green 发光 |
| **标签/Chip** | 胶囊形状（`full`）+ 50% 透明度背景 + `bodySmall` 字号 |
| **模态/遮罩** | **必须**对底层内容施加背景模糊（`backdrop-filter`） |
| **进度指示器** | 柔和"流淌"色条，**禁止**硬边状态指示器 |

---
> [!NOTE]
> 本文档应随着引入新模式而更新。