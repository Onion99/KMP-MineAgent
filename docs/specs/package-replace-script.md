# 包名替换脚本说明

## 目标

`package_replace.kts` 用于项目包名、包域名和 Compose Multiplatform 资源导入迁移。

脚本覆盖两类替换：

- Kotlin/Java/XML/ProGuard 文件中的普通包名，例如 `org.onion.agent` -> `org.onion.agro`。
- Compose generated resources 导入前缀，例如 `xxxxxx.ui_theme.generated.resources.Res` -> `agro.ui_theme.generated.resources.Res`。

## Compose 资源导入

Compose Multiplatform 生成的 `Res` 包名不等同于 `applicationId`，通常由 `rootProject.name` 和模块名参与生成。因此包名迁移时，不能只替换 `org.onion.agent` 这类应用包名前缀。

脚本通过以下配置处理资源包：

- `oldComposeResourceRoot`：旧资源根包名前缀，例如 `xxxxxa`。
- `newComposeResourceRoot`：新资源根包名前缀，例如 `xxxxxb`。
- `composeResourceModuleReplacements`：旧模块名到新模块名的映射，例如 `"ui_theme" to "ui_theme"`。
- `explicitResourcePackageReplacements`：用于处理非标准迁移，例如 `"oldappname.ui_theme.generated.resources" to "newappname.xxxxxx.generated.resources"`。

替换以 `*.generated.resources` 包前缀为单位执行，因此 `Res` 和具体资源条目导入都会被同步更新。

## 遍历边界

脚本只处理 `.kt`、`.java`、`.xml`、`.pro` 文件，并跳过所有模块级 `build`、`.gradle`、`.git`、`.idea`、`.kotlin`、`.cxx` 等目录，避免修改生成产物。

脚本会清理替换后产生的重复 `import` 行，支持在部分文件已手动迁移后重复执行。
