# 项目工程规范

> **版本**：1.0.0
> **更新时间**：2026-06-12


## 🧭 核心规范落地指南

### 1. 仓库即记录系统（Repo as System of Record）

> 不在仓库里的东西，对智能体不存在

**核心实践：**
1. 一切架构决策、设计取舍必须以 Markdown 提交到 `docs/`和所有的 API 设计、数据库 Scheme 变更、重构方案必须沉淀到 `docs/` 目录下（如 `docs/specs/`, `docs/designs/`）
2. 黄金规则代码化：代码规范不应仅仅是一份说明，必须转译为 `.editorconfig`, `detekt.yml` 或自定义 Lint 规则
3. 实施路径双向同步：Agent 的每一次架构修改，必须伴随对应设计文档的更新
4. 变更有迹可循, [CHANGELOG.md](../CHANGELOG.md) 记录每个版本的重要变更


### 2. 渐进式上下文管理 (Progressive Context Management)

为了防止 Agent 的上下文窗口被无关代码或过于庞大的规则文档污染（导致推理延迟和精度下降），本项目采用**渐进式披露（Progressive Disclosure）**原则。

### 2.1 目录结构与职责划分

我们建立两层导航结构：
1. **全局导航入口：`AGENTS.md`**  
   它是一个“地图”。它负责告诉 Agent 项目的基本技术栈（KMP、Compose、Koin、Ktor、Sandwich、LiteRT），并引导 Agent 去哪里寻找详细规范。
2. **细分领域手册：`docs/agents/` 目录**
    - `docs/agents/ui-theme.md`：Ethereal Minimalism 设计系统、色彩渐变、毛玻璃效果、圆角与间距 Token 细则。
    - `docs/agents/native-cpp.md`：Gemma/LiteRT JNI 的面向对象调用、C++ 底层内存与指针生命周期管理。
    - `docs/agents/i18n-guide.md`：多语言资源键名规范与 Compose 引用方式。
    - `docs/agents/network-data.md`：Ktor 客户端配置、 Sandwich 响应处理与序列化规则。


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

> 按需加载（On-demand Loading）**：Agent 在执行特定任务时，必须通过查看目录结构，主动且仅加载与当前任务相关的细则文档（例如，只有在修改 UI 时才加载 `ui-theme.md`）。**严禁**一次性将所有文档读入上下文。
