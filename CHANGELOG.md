# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2026-07-15] - Mobile iOS Bazel CI setup
- [Fixed] Updated `.github/workflows/build.yml` so the mobile `ios` matrix entry installs Bazelisk, restores the Bazel disk cache, and rewrites the CI `.bazelrc.user` before `buildReleaseIpa`, matching the LiteRT LM iOS Bazel task chain.
- [Docs] Updated `docs/specs/ios-litertlm-platform.md` and `docs/specs/bazel-windows-android-rc.md` to record the shared mobile Bazel setup and the macOS runner path boundary.

## [2026-07-15] - iOS simulator native build simplification
- [Changed] Removed `iosX64` from `build-logic/convention/src/main/kotlin/ext/KotlinMultiplatformExt.kt` and `shared/build.gradle.kts` so the Gradle target matrix matches the supported iOS platforms.
- [Changed] Simplified `composeApp/build.gradle.kts` by removing the iOS simulator x64 Bazel task and `lipo` merge task; `buildIosLiteRtLmNativeLibs` now builds only device arm64 and simulator arm64 LiteRT LM archives.
- [Docs] Updated `docs/specs/ios-litertlm-platform.md` to document the current iOS target matrix and the absence of Intel iOS Simulator support.

## [2026-07-14] - iOS LiteRtLmJni platform boundary cleanup
- [Changed] Removed the legacy `sdloader.def` Kotlin/Native cinterop setup, stable-diffusion iOS linker options, and `buildIosNativeLibs` task from `composeApp/build.gradle.kts`.
- [Added] Added `composeApp/src/nativeInterop/cinterop/litertlm.def` and wired iOS targets to the LiteRT LM C API in `cpp/lite-rt-lm/c/engine.h`.
- [Added] Implemented `composeApp/src/iosMain/kotlin/com/google/ai/edge/litertlm/LiteRtLmJni.ios.kt` with C API engine/conversation lifecycle, synchronous send, streaming send, cancellation, and release handling.
- [Changed] iOS linking now expects `liblitertlm_c_api.a` or `liblitertlm_c_api.dylib` under `cpp/libs/ios-device` and `cpp/libs/ios-simulator`, with `validateIosLiteRtLmNativeLibs` failing early on macOS if artifacts are missing.
- [Docs] Added `docs/specs/ios-litertlm-platform.md` to record the iOS LiteRT LM bridge, native library contract, and unsupported common API fields.


## [2026-07-14] - Windows Bazel Rust 链接路径修复
- [修复] 更新 `.github/workflows/build.yml`，将 Windows CI 的 Bazel 输出基准目录从 `$RUNNER_TEMP/bazel-output` 改为 `startup --output_base=C:/b`，避免 `rules_rust` proc-macro 对象文件路径过长导致 MSVC `link.exe` 报 `LNK1181`。
- [文档] 更新 `docs/specs/bazel-windows-android-rc.md`，补充 Windows CI 必须使用短 Bazel 输出根的约束与故障原因。

## [2026-07-14] - CI NDK 版本与 Windows Bazel 环境隔离修复
- [修复] 将 Android NDK 版本收敛到 `gradle/libs.versions.toml` 的 `android-ndk=27.0.12077973`，并由 `build-logic/convention/src/main/kotlin/ext/AndroidExt.kt` 显式写入 `android.ndkVersion`，避免 AGP 默认值与 CI 安装版本漂移。
- [修复] 更新 `.github/workflows/build.yml`，Android release 构建安装 `27.0.12077973` 并写入 `local.properties`，修复 `ndk.dir` 与 `android.ndkVersion` 不一致导致的 `CXX1104`。
- [修复] 更新 `.github/workflows/build.yml`，Windows desktop 构建执行 Gradle 时清空 `ANDROID_NDK_HOME`/`ANDROID_NDK_ROOT`，避免 Bazel 注册 hosted runner 预置 NDK 后触发 `Cannot write outside of the repository directory`。
- [文档] 更新 `docs/specs/bazel-windows-android-rc.md`，补充 Windows NDK 环境隔离和 Android NDK 版本对齐约束。

## [2026-07-14] - 多平台 CI 原生构建前置条件修复
- [修复] 更新 `.github/workflows/build.yml`，为 Android release 构建安装 Android NDK 并写入 `local.properties`，修复 `buildAndroidNativeLib` 创建阶段 `NDK is not installed`。
- [修复] Windows Desktop 构建改为下载真实 `bazelisk.exe` 并加入 `PATH`，避免 Gradle `ExecOperations` 无法启动 npm `.cmd` shim 导致 `command 'bazelisk'` 失败。
- [修复] Linux Desktop 构建在 CI `.bazelrc.user` 中禁用 `xnn_enable_avxvnniint8`，规避 Ubuntu 22.04 clang 14 不支持 `-mavxvnniint8` 的 XNNPACK 编译失败。
- [修复] Desktop 与 Mobile 构建 checkout 启用 Git LFS，并对子模块执行 `git lfs pull`，避免 macOS 链接到 LFS pointer 导致 `ld: unknown file type`。
- [文档] 更新 `docs/specs/bazel-windows-android-rc.md`，补充 Android NDK、Windows Bazelisk、Linux XNNPACK 和子模块 LFS 的 CI 约束。

## [2026-07-14] - Linux Gradle Wrapper 权限修复
- [修复] 将 `gradlew` 的 Git 可执行位调整为 executable，并在 `.github/workflows/build.yml` 的 Unix runner 中增加 `chmod +x ./gradlew` 前置步骤，修复 Linux 包构建执行 `./gradlew` 时 `Permission denied` 的问题。
- [文档] 更新 `docs/specs/bazel-windows-android-rc.md`，补充 GitHub Actions 中 Gradle Wrapper 执行权限约束。

## [2026-07-14] - GitHub Actions 原生构建链路优化
- [修复] 优化 `.github/workflows/build.yml`，为 Desktop 三平台和 Android release 构建显式安装 Bazelisk，修复 CI 中 `buildNativeLibForWindows` 启动 `bazelisk` 失败的问题。
- [修改] 在 CI 中按平台生成临时 `.bazelrc.user`，为 Bazel 配置独立输出目录、磁盘缓存和 Windows Visual Studio C++ toolchain 自动发现，避免复用本机固定路径。
- [修改] 扩展 workflow 触发路径与 `pull_request`/`workflow_dispatch` 入口，覆盖 `cpp/`、KMP 子模块、`build-logic/`、iOS 工程和桌面图标资源变更。
- [文档] 更新 `docs/specs/bazel-windows-android-rc.md`，记录 GitHub Actions 与本机 Bazel RC 配置的边界。

## [2026-07-14] - Desktop App Icon 资源生成
- [新增] 基于 `composeApp/src/androidMain/res/drawable/ic_launcher_round.xml` 生成 `docs/AppIcon.png`、`docs/AppIcon.ico`、`docs/AppIcon.icns` 与中间 SVG，匹配 Compose Desktop Linux、Windows、macOS 打包配置。
- [新增] 新增 `scripts/generate_desktop_icons.py` 与 `docs/specs/desktop-icon-assets.md`，记录桌面图标生成流程、输出文件与 Gradle 接入路径。

## [2026-07-14] - Android App Icon GRIS 风格重构
- [修改] 全局重构 `composeApp/src/androidMain/res/drawable/ic_launcher.xml` 与 `composeApp/src/androidMain/res/drawable/ic_launcher_round.xml`，放弃萌宠卡通路线，改为受 GRIS 艺术方向启发的空灵水彩人物剪影、流动斗篷、种子光点与羊皮纸留白。

## [2026-07-13] - 包名替换脚本资源导入适配
- [修改] 优化 `package_replace.kts`，新增 Compose generated resources 包前缀替换配置，支持 `oldappname.ui_theme.generated.resources.Res` 到 `newappname.xxxxxx.generated.resources.Res` 这类资源导入迁移。
- [修改] 增强脚本目录过滤，跳过所有模块级 `build`、`.gradle`、`.git` 等生成目录，并在替换后清理重复 `import` 行。
- [文档] 新增 `docs/specs/package-replace-script.md`，记录脚本配置项、Compose 资源导入迁移规则和遍历边界。

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
