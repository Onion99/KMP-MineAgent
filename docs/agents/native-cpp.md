## JNI 与大模型交互架构

为了确保内存安全与异步数据流的稳定性，JNI 通信必须遵守以下面向对象封装准则：

- **引擎生命周期指针管理**：
    - 由 Kotlin 包装类 `LmEngine` 负责底层的原生 C++ 引擎初始化、资源释放，必须保障指针在生命周期结束时正确析构，避免内存泄漏。
    - 桥接层 `LmConversation` 负责管理会话的上下文与流式消息的调度。

- **消息传递机制**：
    - **禁止**直接跨 JNI 传递裸指针或裸原生 JSON 字符串，应在 Kotlin 侧将对象（`Message`, `Content`, `ToolCall`）转换为规范的 `kotlinx.serialization.json` 结构后再行操作。
    - 流式应答（Stream Response）：原生回调应转化为 Kotlin `Flow` 的异步数据流抛出，提供平滑非阻塞的 UI 消费方案。