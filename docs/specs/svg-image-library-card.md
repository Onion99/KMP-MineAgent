# SVG 图像生成资源库卡片

> 日期: 2026-07-22
> 范围: `composeApp` Library 入口与 ChatViewModel 本地 LiteRT LLM 会话创建

## 1. 目标

将 Library 页原先的 Data Crystal 通用分析卡片替换为专门面向 SVG 图像生成的入口。用户点击该卡片后直接进入 Chat，并为当前 LLM 会话创建注入专用 `systemInstruction`，让模型稳定输出可解析的 SVG 图像 JSON。

## 2. 入口行为

- `LibraryScreen` 中使用 `SvgImageCard` 展示 SVG 图像生成入口。
- 点击卡片会调用 `ChatViewModel.startSvgImageConversation()`，随后通过 `onOpenChat()` 导航到 Chat 页。
- 该方法不会覆盖用户在设置页维护的全局 `systemPrompt`，只为当前会话设置临时的 `systemInstruction` override。
- 普通新建会话仍走 `ChatViewModel.startNewConversation()`，并恢复使用全局 `systemPrompt`。

## 3. 会话创建约束

SVG 图像模式通过 `LmEngine.createConversation()` 创建会话时传入:

- `systemInstruction = SVG_IMAGE_SYSTEM_INSTRUCTION`
- `toolsDescriptionJsonString = agentTools.getToolsDescriptionJson()`
- `enableConversationConstrainedDecoding = true`
- 当前 UI 中配置的 `temperature`、`topP`、`topK`

如果用户先点击 SVG 卡片但 LLM 引擎尚未初始化，`ChatViewModel` 会保留 `activeSystemInstructionOverride`。后续 `initLLM()` 初始化引擎时会使用该 override 创建专用会话。

## 4. 输出 JSON 结构

专用 `systemInstruction` 要求模型只输出一个 JSON object，不允许 Markdown 包裹或额外说明。结构如下:

```json
{
  "type": "svg_image",
  "version": 1,
  "title": "short image title",
  "description": "one sentence summary",
  "canvas": {
    "width": 1024,
    "height": 1024,
    "viewBox": "0 0 1024 1024"
  },
  "style": {
    "palette": ["#RRGGBB"],
    "background": "transparent|solid|gradient",
    "keywords": ["flat", "line-art"]
  },
  "svg": "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1024\" height=\"1024\" viewBox=\"0 0 1024 1024\">...</svg>",
  "usageNotes": ["short note"]
}
```

## 5. 安全边界

SVG 输出必须自包含，禁止:

- `<script>` 与事件处理器。
- 外部链接、远程图片和网络资源。
- `foreignObject`。

模型应优先使用 SVG 原生矢量元素、`path`、渐变、mask 和必要的文本元素。

## 6. 当前限制

- 当前未新增数据库字段保存会话模式；重新打开历史 SVG 会话时会按普通会话重建原生 LLM 上下文。
- 当前只约束模型输出 JSON，并未新增前端 JSON 解析或 SVG 渲染预览。
