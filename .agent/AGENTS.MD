# 项目工程规范

> **版本**：1.0.0
> **更新时间**：2026-06-12


## 🧭 核心规范落地指南

### 1. 仓库即唯一事实源 (Repo as System of Record)

**规则：如果信息不在 Git 仓库里，对 Agent 而言它就是不存在的。**

- **操作要求**：
    1. **方案先行**：在进行任何实质性的代码修改前，Agent 必须通过创建/更新项目目录下的 `implementation_plan.md` 或 `docs/` 目录下的设计文稿，明确架构意图和 JSON 数据协议。
    2. **任务追踪**：执行过程中必须有 `task.md` 来追踪任务拆解与进度。
    3. **变更同步**：所有针对 UI 样式、网络协议接口、原生 C++ 边界的修改，均需首先更新对应的规格文档，再进行代码编写。

---

**核心实践：**
1. 一切架构决策、设计取舍必须以 Markdown 提交到 `docs/`
2. 执行计划是一等工件 —— 智能体的 `implementation_plan.md` 和 `task.md` 天然符合此原则
3. 口头共识必须转化为 `code-style-guide.md` 中的规则条目
4. 任务跟踪器（GitHub Issues）状态对智能体可见 —— 避免重复劳动

---

### 1. 仓库即记录系统（Repo as System of Record）

> 不在仓库里的东西，对智能体不存在。

| 原则 | 本项目实践 |
|------|-----------|
| 一切决策版本化 | 所有架构决策、设计规范以 `.md` 文件提交到 `docs/` 和 `.agent/rules/` |
| 规范即代码 | [code-style-guide.md](.agent/rules/code-style-guide.md) 作为 `always_on` 规则，智能体每次对话自动加载 |
| 变更有迹可循 | [CHANGELOG.md](../CHANGELOG.md) 记录每个版本的重要变更 |
| 构建配置集中 | `settings.gradle.kts` + `build-logic/` 统一管理模块声明与插件 |

**黄金法则**：
- ❌ 禁止在聊天工具 / 脑中保留未提交的架构决策
- ✅ 任何影响模块边界、依赖方向、API 契约的决策，**必须**更新到 `.agent/rules/` 或 `docs/`

---

### 1. 代码库即唯一记录系统 (Repo as System of Record)
 **核心定义**：如果一个约定、设计决策或接口定义没有提交到代码库中，它对 Agent 而言就是不存在的。
 **落地实践**：
 * 禁止将核心业务流程、排期、设计细节置于外部不可版本化的文档（如未归档的 IM 对话、局域网文档）中。
 * 所有的产品需求文档 (PRD)、架构演进设计 (Design Docs) 以及任务清单，必须作为 Markdown 格式提交至 `docs/` 或 `.agent/rules/` 目录。
 * 任务执行时，Agent 需根据 `task.md` 动态更新任务状态。

### 原则一：仓库即唯一事实源 (Repo as System of Record)
 **核心定义**：对于 AI Agent 而言，不存在于 Git 仓库中的知识就是“不存在的”。Slack 讨论、口头约定、未提交的设计草案对 Agent 均不可见。
 **本项目落地**：
 * **设计文档与规范文件化**：所有的 API 设计、数据库 Scheme 变更、重构方案必须沉淀到 `docs/` 目录下（如 `docs/specs/`, `docs/designs/`）。
 * **黄金规则代码化**：代码规范不应仅仅是一份说明，必须转译为 `.editorconfig`, `detekt.yml` 或自定义 Lint 规则。
 * **实施路径双向同步**：Agent 的每一次架构修改，必须伴随对应设计文档的更新。

### 2. 导航图而非大百科全书 (Map, Not Manual)
**规则：采用“渐进式披露”原则，拒绝将几千行的开发手册灌入单一 Context，避免注意力稀释与文档失效。**

我们建立两层导航结构：
1. **全局导航入口：项目根目录 `AGENTS.md`**  
   它是一个“地图”，总行数控制在约 100 行。它负责告诉 Agent 项目的基本技术栈（KMP、Compose、Koin、Ktor、Sandwich、LiteRT），并引导 Agent 去哪里寻找详细规范。
2. **细分领域手册：`docs/agents/` 目录**
    - `docs/agents/ui-theme.md`：Ethereal Minimalism 设计系统、色彩渐变、毛玻璃效果、圆角与间距 Token 细则。
    - `docs/agents/native-cpp.md`：Gemma/LiteRT JNI 的面向对象调用、C++ 底层内存与指针生命周期管理。
    - `docs/agents/i18n-guide.md`：多语言资源键名规范与 Compose 引用方式。
    - `docs/agents/network-data.md`：Ktor 客户端配置、 Sandwich 响应处理与序列化规则。

> Agent 应在进入项目时首先且仅读取 `AGENTS.md`。仅在修改特定模块时，才通过全局导航的链接去加载详细手册。

---

## 2. 渐进式上下文管理 (Progressive Context Management)

为了防止 Agent 的上下文窗口被无关代码或过于庞大的规则文档污染（导致推理延迟和精度下降），本项目采用**渐进式披露（Progressive Disclosure）**原则。

### 2.1 目录结构与职责划分
```
.
├── AGENTS.md                       # [NEW] 根目录总入口，只保留最核心的构建命令与导航指针
└── docs/
    ├── harness-engineering-spec.md # [NEW] 本规范文档
    ├── code-style-guide.md        # [NEW] 编码与 UI 设计系统规范 (Ethereal Minimalism)
    └── agents/                     # 分域 Agent 上下文目录
        ├── ui-theme.md             # 详细的主题、动效及毛玻璃实现规范
        ├── jni-llm.md              # LiteRT-LM C++ 绑定与 JNI 调用规约
        └── data-network.md         # Ktor 与 Koin 依赖注入配置细则
```

### 2.2 渐进式引入规则
- **根目录 `AGENTS.md`**：是任何 Agent 启动时的必读文件。它**仅**包含项目结构概览、常用 Gradle 指令、以及指向 `docs/` 中具体文档的指针。
- **按需加载（On-demand Loading）**：Agent 在执行特定任务时，必须通过查看目录结构，主动且仅加载与当前任务相关的细则文档（例如，只有在修改 UI 时才加载 `ui-theme.md`）。**严禁**一次性将所有文档读入上下文。

---