---
trigger: always_on
---

# Kotlin Multiplatform 项目编码规范

当前状态：2026-06-15


## 1. 架构与设计模式 (MVVM)

- **View**：位于 `src/commonMain/kotlin/org/onion/agent/ui/screen/`。所有 UI 组件均应为无状态（Stateless）或通过 ViewModel 进行状态管理。
- **ViewModel**：位于 `src/commonMain/kotlin/org/onion/agent/viewmodel/`。所有的业务逻辑、异步流拉取等，均应放在 ViewModel 中，避免在 Composable 函数内直接处理。
- **依赖注入 (DI)**：Koin 注册文件位于 `src/commonMain/kotlin/org/onion/agent/di/`。新增 ViewModel 必须在 `ViewModelModule.kt` 中注册。
- **导航**：使用 `Jetpack Navigation Compose`，路由定义在 `src/commonMain/kotlin/org/onion/agent/ui/navigation/`。

---

## 2. 响应式与多端适配策略 (Adaptive Layout)

根据项目的屏幕适配策略，必须利用 `AppTheme.contentType` 进行响应式布局：
- **`ContentType.Single`（移动端模式）**：
    - UI 优先采用垂直布局（`Column`）。
    - 合理隐藏次要面板以防屏幕拥挤。
- **`ContentType.Dual`（桌面端/平板模式）**：
    - 优先采用水平布局（`Row`）以最大化大屏空间利用率。
    - 支持左右/多栏目分栏展示。

---



## 3. 智能体工作流程

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

---

## 4. Agent 开发核心约束

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

---

