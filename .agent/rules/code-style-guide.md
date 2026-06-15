---
trigger: always_on
---

# Kotlin Multiplatform 项目编码规范

当前状态：2026-06-15


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
- **`cpp`**：原生 C++ 实现（LiteRT-LM）。

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


## 5. 智能体工作流程

**新功能开发流程**：

```
1. 人类：描述需求 + 指定目标模块
2. 智能体：
   a. 读取 code-style-guide.md（自动加载）
   b. 按需读取 harness-engineering-spec.md
   c. 在 data-model 定义数据模型
   d. 在 data-network 创建 API（如需要）
   e. 在 viewmodel/ 创建 ViewModel + 注册到 ViewModelModule.kt
   f. 在 ui/screen/ 创建 Screen + 注册路由
   g. 在 strings.xml 添加国际化字符串
   h. 自我验证：编译通过 + 设计 token 正确
3. 人类：审查 + 验证 + 反馈
```

**新页面开发流程**：

```
1. 人类：描述页面功能 + UI 需求
2. 智能体：
   a. 检查 ContentType 适配需求（Single / Dual）
   b. 使用 AppTheme.* token 构建 UI
   c. 使用 Modifier.glassSurface() / watercolorGradient() 实现毛玻璃美学
   d. 国际化所有用户可见文本
   e. 响应式布局：Mobile = Column，Desktop = Row
3. 人类：视觉审查 + 交互验证
```

## 6. Agent 开发核心约束

### 架构与技术栈约束
- **核心框架**：Kotlin Multiplatform + Compose Multiplatform
- **注入与路由**：使用 Koin (v4.1.1) 进行依赖注入，使用 Jetpack Navigation Compose (v2.9.0-beta04) 实现类型安全路由。
- **MVVM 规约**：视图层（Composables）严禁编写复杂的业务和网络调用逻辑，必须委派给 ViewModel 处理。所有的 ViewModel 应注册在 `ViewModelModule.kt`。

### UI 样式规约 (Ethereal Minimalism)
- **零硬编码**：禁止使用 `Color(0xFF...)` 或硬编码圆角大小。必须引用 `AppTheme.colors.*` 等系统 Token。
- **毛玻璃与渐变**：
    - 玻璃卡片统一使用 `Modifier.glassSurface()` 扩展函数。
    - 水彩背景统一使用 `Modifier.watercolorGradient()`。
- **布局适配**：
    - 必须使用 `AppTheme.contentType` 适配单栏（Mobile, `ContentType.Single`）和双栏（Desktop/Tablet, `ContentType.Dual`）布局。

### 性能约束
- 对于数据量大于 500 的集合，算法复杂度必须在 $O(n)$ 或以内，禁止双重循环。
- 禁止在 `for`/`forEach` 循环内进行重复计算或高开销的操作（如重复解析 JSON、重复格式化日期）
- Compose 重组域中禁止重复进行未缓存的高开销计算，重组期间的高开销操作必须使用 `remember` 进行包裹。

