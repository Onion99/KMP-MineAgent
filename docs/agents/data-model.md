
> 本模块定义了系统的数据载体

## 1. 结构与生命周期
- **无状态/纯数据**：本模块只包含 Kotlin 核心 `data class`、`enum` 与 `sealed class/interface`。
- **纯 Kotlin/Common 模块**：不允许依赖平台特定库（如 Android Context / JVM/iOS 专有包）。
- **零依赖原则**：不依赖 `composeApp`、`ui-theme`、`data-network`。它作为基础依赖被所有其他模块所共用。

## 2. 序列化规范
- 数据类需要通过网络传输或在 JNI 边界传递时，必须加 `@Serializable` 注解（`kotlinx.serialization`）。
- 优先为可能缺失的字段设置默认值，保障反序列化的稳定性。