# LLM Agent 迭代路线与能力缺口

> 日期: 2026-06-25
> 范围: 当前 KMP 应用内的 LiteRT LLM Chat/Agent 能力扫描
> 目的: 为后续 Agent 化迭代提供仓库内记录和优先级参考

## 1. 当前定位

当前项目已经具备本地 LiteRT LLM 对话壳、模型加载、流式输出和基础工具调用循环，但还不是完整 LLM Agent。

仓库中存在两条相关线索:

- 当前产品主线: `composeApp/src/commonMain/kotlin/org/onion/agent/native/llm` 与 `ChatViewModel`，提供轻量本地 LLM 对话和工具调用框架。
- 参考实现: `agent_code/java/com/google/ai/edge/gallery/customtasks/agentchat`，保留了 Google AI Edge Gallery 的 Agent Chat、MCP、Skill、权限和 WebView 结果展示等较完整实现，但当前未集成进 KMP 主应用。

## 2. 已有能力

- 本地 LLM 加载、会话创建、资源释放: `ChatViewModel.initLLM()`、`LmEngine`。
- 流式文本生成: `LmConversation.sendMessageAsync()` 将 JNI 回调转为 Kotlin `Flow`。
- 基础工具协议: `Message`、`ToolCall`、`ToolResponse` 已能序列化为 LiteRT LM 需要的 JSON 结构。
- 工具调用循环: `ChatViewModel.getTextTalkerResponse()` 支持模型返回 tool call 后执行工具并回填 tool response，当前最多 10 轮。
- 基础采样参数: temperature、topP、topK、max tokens、thinking、speculative decoding 有 UI 状态和部分引擎参数接入。
- Android/Desktop JNI 接入: `LiteRtLmJni.android.kt` 与 `LiteRtLmJni.desktop.kt` 已实现模型选择、引擎创建、会话创建、同步/异步发送、取消和释放。
- 模型下载队列: `DownloadManagerImpl` 支持下载、暂停、恢复、取消和进度更新。
- 基础 UI: 模型选择页、聊天页、参数设置页、资源库展示页已接入主导航。

### 2.1 关键缺口索引

以下缺口必须作为后续迭代排期的显式输入，避免只分散在各能力章节中被漏读:

- 上下文管理: `systemContextShift` 只是 UI 状态，没有参与引擎或会话参数，见 `composeApp/src/commonMain/kotlin/org/onion/agent/viewmodel/ChatViewModel.kt:141`。
- 长上下文压缩: 缺少 token 预估、历史裁剪、摘要记忆和超过窗口前的主动提示。
- 长期记忆/RAG: `LibraryScreen` 是静态展示，没有真实知识库、向量索引、文件导入或记忆检索，见 `composeApp/src/commonMain/kotlin/org/onion/agent/ui/screen/LibraryScreen.kt:394`。
- 真实 MCP 执行: 当前 `runMcpTool` 是假执行，未连接 MCP Server、未发现工具、未做权限确认，见 `composeApp/src/commonMain/kotlin/org/onion/agent/native/llm/AgentTools.kt:233`。
- 真实平台动作: 当前 `runIntent` 是假执行，未对接日历、邮件、通知、文件等系统能力，见 `composeApp/src/commonMain/kotlin/org/onion/agent/native/llm/AgentTools.kt:248`。
- Agent 路由提示词: 当前系统提示词只是普通助手人格，没有像 `agent_code` 那样明确 Skill/MCP 路由流程和失败回退。
- 工具调用可靠性: `createConversation` 默认没有开启 constrained decoding，工具 JSON 可能不稳定；可参考 `agent_code` 的 `enableConversationConstrainedDecoding = true`。
- 会话持久化: `currentChatMessages` 只存在内存中，关闭应用后丢失，见 `composeApp/src/commonMain/kotlin/org/onion/agent/viewmodel/ChatViewModel.kt:429`。

## 3. P0 必须优先补齐

### 3.1 真实 Skill 系统

当前 `AgentTools.loadSkill()` 只返回成功文案，没有读取真实 `SKILL.md`，也没有技能管理、启用状态、脚本资源和描述注入。

需要补齐:

- Skill 数据模型与持久化。
- 从本地目录、内置 assets 或 URL 导入 Skill。
- 解析 `SKILL.md` 的 name、description、instructions 和脚本目录。
- 将已启用 Skill 的名称和描述注入系统提示词。
- `loadSkill` 必须返回真实 instructions，而不是固定 mock 文案。
- Skill 执行日志和错误结果必须可显示、可追踪。

可参考:

- `agent_code/java/com/google/ai/edge/gallery/customtasks/agentchat/SkillManagerViewModel.kt`
- `agent_code/assets/skills`

### 3.2 真实 MCP 工具系统

当前 `AgentTools.runMcpTool()` 是假执行，没有连接 MCP server、发现工具、校验 schema 或处理权限。

需要补齐:

- MCP server 添加、编辑、删除、启用和持久化。
- MCP tool discovery 和工具 schema 展示。
- tool call 参数 schema 校验。
- 工具调用权限确认: allow once、always allow、deny。
- MCP 调用结果标准化为模型可读 JSON。
- 错误分类: server unavailable、tool not found、permission denied、schema mismatch、timeout。

可参考:

- `agent_code/java/com/google/ai/edge/gallery/customtasks/agentchat/McpManagerViewModel.kt`
- `agent_code/java/com/google/ai/edge/gallery/customtasks/agentchat/McpToolCallPermissionDialog.kt`
- `agent_code/java/com/google/ai/edge/gallery/customtasks/agentchat/McpToolManagerBottomSheet.kt`

### 3.3 工具权限与安全沙箱

当前 `runJs`、`runIntent`、`analyzeUrl`、`searchWeb` 均可由模型触发，缺少统一权限和审计边界。

需要补齐:

- 高风险工具调用前用户确认。
- 网络工具域名策略、URL 过滤、SSRF 防护、本地地址禁止访问。
- JS 执行超时、内存限制、全局对象白名单、脚本来源校验。
- secret 管理，禁止把密钥写入聊天记录或模型上下文。
- 工具调用审计日志，至少记录时间、工具名、参数摘要、结果状态。

### 3.4 Agent 路由提示词

当前默认系统提示词偏普通助手人格，没有强制 Skill/MCP 路由流程，也没有失败回退策略。

需要补齐:

- 当有 Skill 和 MCP 时的统一 route prompt。
- 当只有 Skill 时的简化 prompt。
- 明确禁止模型把 Skill 当 MCP 工具调用。
- 明确要求模型只输出最终结果，隐藏内部工具路由过程。
- 将可用 Skill 和 MCP tool 列表注入到系统上下文。

可参考:

- `agent_code/java/com/google/ai/edge/gallery/customtasks/agentchat/AgentChatTaskModule.kt`

### 3.5 工具调用可靠性

当前 `createConversation()` 默认未启用 constrained decoding，工具 JSON 可能不稳定。

需要补齐:

- 开启或可配置 `enableConversationConstrainedDecoding`。
- 对 tool call JSON 做严格解析和容错。
- 对工具返回值大小做截断和摘要。
- 限制递归工具调用轮次并给出明确错误。
- 工具执行失败时让模型获得结构化错误，而不是自然语言 mock。

### 3.6 真实平台动作

当前 `AgentTools.runIntent()` 只返回固定成功文案，没有对接任何真实平台能力。

需要补齐:

- Android 平台动作: 日历读取/写入、邮件发送、通知计划、分享、文件选择、系统设置跳转。
- Desktop 平台动作: 文件打开、目录选择、剪贴板、默认浏览器、系统通知。
- iOS 平台动作: 在平台能力允许范围内接入日历、提醒事项、分享和文件选择，或明确降级。
- 权限请求流: 每个高风险动作必须先检查权限，缺权限时触发用户授权。
- 参数 schema: 每个 intent/action 必须有明确 JSON schema 和错误返回格式。
- 执行回执: 返回结构化结果，包括 `success`、`action`、`parameters`、`result`、`error`。
- 安全边界: 禁止模型静默执行破坏性动作，例如删除文件、发送邮件、创建外部事件，除非用户明确确认。

可参考:

- `agent_code/java/com/google/ai/edge/gallery/customtasks/agentchat/IntentHandler.kt`
- `agent_code/assets/skills/create-calendar-event`
- `agent_code/assets/skills/read-calendar-events`
- `agent_code/assets/skills/schedule-notification`
- `agent_code/assets/skills/send-email`

## 4. P1 产品化能力

### 4.1 会话持久化与历史记录

当前已通过 KMP Room 补齐基础会话持久化与历史记录，详见 `docs/specs/chat-history-room-persistence.md`。

已实现:

- 会话列表、会话标题、创建时间、更新时间。
- 消息持久化，包含 role、content、tool_calls、tool_responses、metadata。
- 历史记录搜索、删除、重命名、导出到剪贴板。
- 应用重启后恢复最近会话 UI。
- 工具调用日志与消息关联。

剩余增强:

- 导出为用户选择的文件路径。
- 将恢复的历史消息重放或摘要进 LLM 原生上下文。
- 为删除动作增加确认弹窗与撤销。

### 4.2 上下文管理

当前缺少 token 预估、历史裁剪、摘要记忆和长上下文策略。

需要补齐:

- 发送前 token 预算估算。
- 历史消息自动裁剪。
- 超限前提示用户或自动摘要。
- 长会话摘要记忆。
- system prompt 与工具说明的预算隔离。
- `systemContextShift` 需要接入真实引擎或上下文策略，否则应移除或改名。

### 4.3 多模态输入

`Message` 已支持 image/audio content，但聊天页附件按钮仍不可用。

需要补齐:

- 图片、音频、文件附件选择。
- 图片压缩、尺寸限制和视觉 token budget。
- 音频格式校验和转码策略。
- 附件预览、删除、失败状态。
- 多模态模型能力检测，避免普通文本模型误收图片或音频。

### 4.4 停止、重试、重新生成

ViewModel 有 `stopGeneration()`，但 UI 生成中点击仍提示不支持中断。`reGenerateMessage()` 目前为空壳。

需要补齐:

- 生成中按钮应直接调用 `stopGeneration()`。
- 停止后保留已生成内容，并标记 interrupted。
- 重新生成需要复用原始 prompt 和当时参数。
- 支持从某条历史消息分叉新会话。
- 失败消息支持 retry。

### 4.5 模型管理

当前模型列表、下载 URL、预期大小和展示信息硬编码。

需要补齐:

- 模型 manifest: 名称、版本、URL、checksum、大小、上下文窗口、支持模态、推荐后端。
- 下载完成后的 checksum 校验。
- 断点任务持久化，应用重启后恢复。
- 模型删除、迁移、磁盘占用展示。
- 模型兼容性检查，避免加载错误格式。
- 根据设备 RAM/VRAM 给出推荐模型和后端。

### 4.6 LoRA 生效链路

当前有 `loraList` 和 LoRA UI，但未传入 LiteRT 引擎或会话。

需要补齐:

- 明确 LiteRT LM 是否支持当前 LoRA 格式。
- LoRA 元数据解析和兼容性校验。
- 将启用的 LoRA、strength 传入引擎或生成请求。
- LoRA 与模型版本绑定。
- LoRA 设置持久化。

### 4.7 Library/RAG/长期记忆

`LibraryScreen` 当前是静态展示，不是可用能力。

需要补齐:

- 文件导入: txt、md、pdf、html、代码文件。
- 文档切片、索引和检索。
- 本地 embedding 或轻量 BM25 检索方案。
- 会话记忆和用户偏好记忆。
- 可启用/禁用知识库。
- 引用来源和片段展示。

## 5. P2 跨平台与工程化

### 5.1 iOS/Wasm 支持

`LiteRtLmJni` 是 common expect，但只有 Android/Desktop actual。iOS 和 Wasm 目标目前无法拥有同等 LLM 能力。

需要补齐:

- iOS LiteRT LM native bridge 或明确禁用 LLM 功能。
- Wasm 平台降级策略，例如只展示 UI 或接远端服务。
- 平台能力矩阵文档。
- 构建任务和 CI 分平台验证。

### 5.2 架构拆分

`ChatViewModel` 同时管理 LLM、模型路径、图像/视频参数、LoRA、对话状态和工具执行，后续维护成本高。

建议拆分:

- `ModelManager`: 模型列表、下载、加载、释放、能力检测。
- `ConversationManager`: 会话状态、消息持久化、上下文裁剪。
- `ToolRuntime`: Skill、MCP、JS、Intent、网络工具执行。
- `SettingsStore`: 采样参数、系统提示词、后端参数持久化。
- `AttachmentManager`: 图片、音频、文件输入。

### 5.3 DI 生命周期

当前 `ChatViewModel` 通过 `singleOf(::ChatViewModel)` 注册，不是标准 ViewModel 生命周期。

需要评估:

- 是否需要全局单例会话状态。
- 如果需要单例，应改名为 store/controller，避免 ViewModel 语义混乱。
- 如果不需要单例，应使用 `viewModelOf` 并显式管理共享状态。

### 5.4 测试体系

当前测试偏探索性和外部网络调用，缺少稳定 Agent 逻辑验证。

需要补齐:

- `Message` JSON 序列化/反序列化测试。
- 工具调用循环测试。
- MCP 权限流测试。
- Skill 解析测试。
- DownloadManager 断点恢复和失败重试测试。
- ChatViewModel 停止、重试、错误处理测试。
- JNI 生命周期 smoke test。

### 5.5 文档与规范同步

需要持续维护以下文档:

- Agent 架构设计: `docs/designs/`
- Tool 协议和安全模型: `docs/specs/`
- Skill 规范: `docs/specs/`
- MCP 配置和权限策略: `docs/specs/`
- 平台能力矩阵: `docs/specs/`

## 6. 建议迭代顺序

1. 把当前 mock 工具替换为真实 Skill loader，并接入内置 `agent_code/assets/skills` 的只读技能加载。
2. 重写系统提示词注入，让模型知道有哪些 Skill，并能稳定调用 `loadSkill`。
3. 接入真实 JS Skill 执行，先支持无 secret、无 WebView 的纯文本结果。
4. 增加工具调用权限弹窗和审计日志。
5. 接入 MCP server 管理和真实 `runMcpTool`。
6. 做会话持久化、历史记录和重新生成。
7. 做多模态附件输入和模型能力检测。
8. 拆分 `ChatViewModel`，沉淀 `ToolRuntime`、`ConversationManager`、`ModelManager`。
9. 建立 Agent loop 和工具协议测试。
10. 补齐 iOS/Wasm 降级或 native 实现。

## 7. 验收标准

一个功能可标记为完成，需要同时满足:

- 功能不是 mock，能在真实模型会话中被调用。
- UI 有可见状态，包括运行中、成功、失败、权限等待。
- 错误返回是结构化的，模型可理解，用户可读。
- 高风险工具有权限确认或白名单。
- 关键状态可持久化，应用重启后不丢失核心数据。
- 有对应测试或可重复的手工验证步骤。
- 对应设计或协议文档已更新。
## 2026-07-10 状态更新

本次已完成 `s01_agent_loop` 方向的 harness 层收敛，详见 `docs/designs/agent-loop-tool-runtime.md`。

- 已将工具调用循环从 `ChatViewModel.getTextTalkerResponse()` 抽离到 `AgentLoopRunner`。
- 已新增 `AgentLoopState` 与 `AgentLoopEvent`，用 `turnCount`、`transition`、`lastToolCallCount` 显式记录循环状态。
- 已将 `AgentTools` 改为注册表模式，由同一份 `AgentToolDefinition` 同时生成 LiteRT tools schema 与执行分发。
- 已将 `loadSkill`、`runMcpTool`、`runIntent` 三个未接入真实系统的占位工具从模型可见 schema 中移除；历史上下文若仍调用，会得到结构化失败而不是伪成功。
- 已统一工具结果为 `ToolExecutionResult` JSON，并在最终助手消息 metadata 中记录 `agent_turn_count` 与 `agent_transition`。
- 已补充 `AgentLoopRunnerTest` 与 `AgentToolsTest`，覆盖循环回灌、无工具结束、禁用工具和 schema 暴露边界。

仍未完成:

- Skill、MCP、Intent 的真实接入仍是后续 P0。
- tool call id 仍未从 native 层透传，当前同轮多工具仍依赖顺序匹配。
- 工具权限确认、SSRF 完整防护、JS 已注册脚本模型、上下文压缩与 token 恢复仍未实现。
