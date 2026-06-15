## 国际化 (i18n) 机械约束
**原则**：所有面向用户的界面文本必须支持多语言。

- **禁止项**：
  - 禁止在 Composable 函数和 ViewModel 中硬编码任何中/英文字符串常量。
- **正确示范**：
  - 必须使用 `compose.components.resources`。
  - 所有字符串声明在 `composeApp/src/commonMain/composeResources/values/strings.xml`（及其他语言对应的 values 目录）。
  - UI 中引用统一使用 `stringResource(Res.string.key_name)`。