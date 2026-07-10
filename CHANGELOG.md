# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2026-07-10] - Chat 工具日志外键修复
- [修复] 将 `ChatHistoryDao` 中 `chat_sessions`、`chat_messages`、`chat_tool_logs` 的写入从 SQLite `REPLACE` 语义改为 Room `@Upsert`，避免更新 session 或 message 时触发外键级联删除。
- [修复] `ChatHistoryRepository.upsertToolLog()` 写入前检查父消息是否仍存在，防止生成过程中会话被删除或状态切换时工具日志写入导致后台协程崩溃。
- [文档] 更新 `docs/specs/chat-history-room-persistence.md`，记录 `REPLACE` 与外键级联删除的风险及后续约束。

## [2026-07-10] - Agent Loop 与工具运行时重构
- [新增] 新增 `AgentLoopRunner`、`AgentLoopState`、`AgentLoopEvent` 与 `LmChatSession`，将工具调用循环从 `ChatViewModel.getTextTalkerResponse()` 抽离为可测试的 harness 层。
- [修改] 将 `AgentTools` 改为工具注册表模式，统一 LiteRT tools schema 生成和执行分发，并将工具结果标准化为 `ToolExecutionResult` JSON。
- [修改] 下线 `loadSkill`、`runMcpTool`、`runIntent` 三个未接入真实系统的模型可见占位工具，旧调用会返回结构化失败而不是伪成功。
- [修改] `ChatViewModel` 改为消费 `AgentLoopEvent` 更新 UI 与工具日志，并在助手消息 metadata 中记录 `agent_turn_count` 与 `agent_transition`，降低任务状态漂移风险。
- [新增] 新增 `AgentLoopRunnerTest` 与 `AgentToolsTest`，覆盖工具回灌循环、无工具结束、禁用工具和 schema 暴露边界。
- [文档] 新增 `docs/designs/agent-loop-tool-runtime.md`，并更新 `docs/specs/llm-agent-iteration-roadmap.md` 的 2026-07-10 状态记录。

## [2026-07-01] - Bazel Android NDK UTF-8 参数修复
- [修复] 调整 `.bazelrc.user`，移除全局 `build --copt=/utf-8` 与 `build --cxxopt=/utf-8`，避免 Android NDK `clang.exe` 将 MSVC 风格 `/utf-8` 解析为输入文件。
- [修改] 为 Windows desktop Bazel 构建新增显式 `--config=msvc_target_utf8`，并保留 `--config=win_host` 下的 host-only UTF-8 参数，确保 MSVC host 工具链仍按 UTF-8 编译。
- [文档] 新增 `docs/specs/bazel-windows-android-rc.md`，记录 Windows host、Windows target 与 Android target 的 Bazel RC 参数边界及验证方式。

## [2026-06-25] - Chat 会话持久化与历史记录
- [新增] 引入 KMP Room、KSP 与 bundled SQLite，新增 `AgentDatabase`、`ChatHistoryDao`、`ChatHistoryRepository` 及 Android/Desktop/iOS 数据库 builder，实现跨端会话持久化。
- [新增] 新增 `chat_sessions`、`chat_messages`、`chat_tool_logs` 三张表，持久化会话标题、创建/更新时间、消息 role/content/tool_calls/tool_responses/metadata 及工具调用日志关联。
- [新增] `ChatViewModel` 接入 Room，会在启动时恢复最近会话，发送消息时自动创建/更新会话，并在工具调用开始/完成时写入日志。
- [新增] `ChatScreen.kt` 增加历史面板，支持搜索、打开、重命名、删除和导出到剪贴板；`LibraryScreen.kt` 的 Living Memory 改为展示真实最近会话并可跳转 Chat。
- [修改] 扩展 `ChatMessage` 数据载体，新增 `ChatRole`、`PersistentToolCall`、`PersistentToolResponse` 以承载持久化所需结构化字段。
- [文档] 新增 `docs/specs/chat-history-room-persistence.md`，并更新 `docs/specs/llm-agent-iteration-roadmap.md` 的 4.1 状态。

## [2026-06-25] - LLM Agent 迭代路线文档
- [新增] 在 `docs/specs/llm-agent-iteration-roadmap.md` 中沉淀当前 LiteRT LLM Agent 能力扫描、缺口分析、优先级路线、建议迭代顺序与验收标准，用于后续 Agent 化功能迭代。
- [修改] 补充 LLM Agent 关键缺口索引，并将 `runIntent` 真实平台动作独立列为 P0 迭代项，覆盖上下文管理、长上下文压缩、长期记忆/RAG、真实 MCP、路由提示词、工具可靠性与会话持久化等遗漏项。

## [1.0.1] - 2026-06-24
### Added
- 在 `AGENTS.md` 中新增「智能体任务交付标准 (Definition of Done - DoD)」与「任务执行三步走协议」，强制智能体在编程任务中同步更新设计文档及 `CHANGELOG.md`。
- 在 `docs/code-style-guide.md` 的开发流程中嵌入文档与 `CHANGELOG.md` 同步步骤，保证后续对话中严格落实「仓库即记录系统」核心原则。
