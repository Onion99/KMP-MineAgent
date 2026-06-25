# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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
