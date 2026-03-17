# Kotlin Multiplatform 项目编码规范

当前状态：2026-01-17


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
- **`ui-theme`**：设计系统模块。包含颜色、排版、形状 (`Theme.kt`, `style/`)。
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
- **JNI**：通过 `DiffusionLoader`（位于 `native/`）与原生代码交互

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

---
> [!NOTE]
> 本文档应随着引入新模式而更新。
