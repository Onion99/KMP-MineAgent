import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

// ================== 配置区 ==================
val newPackage = "org.onion.agro"
// 被替换的 Kotlin/Java 包名
val needToReplacePackage = "org.onion.agent"

// Compose Multiplatform generated resources 的包名不等同于 applicationId。
// 例如：
//   import oldappname.ui_theme.generated.resources.Res
//   import newappname.xxxxxx.generated.resources.Res
val oldComposeResourceRoot = "xxxx"
val newComposeResourceRoot = "agro"
val composeResourceModuleReplacements = mapOf(
    "composeapp" to "composeapp",
    "ui_theme" to "ui_theme",
)
val explicitResourcePackageReplacements = mapOf<String, String>(
    // "oldappname.ui_theme.generated.resources" to "newappname.xxxxxx.generated.resources",
)

val rootDir = ".\\"
val ignoreDirs = listOf(
    ".\\.fleet",
    ".\\.gitignore",
    ".\\.kotlin",
    ".\\.idea",
    ".\\build",
    ".\\.gradle",
    ".\\gradle",
    ".\\.git",
    ".\\iosApp",
    ".\\cpp",
    ".\\composeApp\\.cxx",
    ".\\composeApp\\build",
)
val ignoreDirNames = setOf(
    ".fleet",
    ".git",
    ".gradle",
    ".idea",
    ".kotlin",
    ".cxx",
    "build",
)
val ignoreRootDirNames = setOf(
    "cpp",
    "iosApp",
)
val checkFileSuffix = listOf(".kt", ".java", ".xml", ".pro")
// ============================================

data class ReplacementRule(
    val oldValue: String,
    val newValue: String,
    val description: String,
)

val rootFile = File(rootDir).canonicalFile
val ignoreFiles = ignoreDirs.map {
    File(rootFile, it).canonicalFile
}.toSet()

val packageReplacementRules = listOf(
    ReplacementRule(
        oldValue = needToReplacePackage,
        newValue = newPackage,
        description = "package",
    ),
)

val resourcePackageReplacementRules = buildList {
    composeResourceModuleReplacements.forEach { (oldModule, newModule) ->
        add(
            ReplacementRule(
                oldValue = "$oldComposeResourceRoot.$oldModule.generated.resources",
                newValue = "$newComposeResourceRoot.$newModule.generated.resources",
                description = "compose resources",
            ),
        )
    }
    explicitResourcePackageReplacements.forEach { (oldPackage, newPackage) ->
        add(
            ReplacementRule(
                oldValue = oldPackage,
                newValue = newPackage,
                description = "explicit compose resources",
            ),
        )
    }
}
    .filter { it.oldValue.isNotBlank() && it.oldValue != it.newValue }
    .distinctBy { it.oldValue }

val replacementRules = packageReplacementRules + resourcePackageReplacementRules

// --- 步骤 1: 修改文件内容 ---
println("Step 1: Replacing package content in files...")
rootFile.walkTopDown()
    .onEnter { dir -> dir.shouldEnterDirectory() }
    .filter { it.isFile && it.isTargetFile() }
    .forEach { file ->
        file.replaceContent(replacementRules)
    }
println("Step 1: Finished.\n")

// --- 步骤 2: 移动/重命名目录 ---
println("Step 2: Finding and moving directories...")
val oldPackageSegments = needToReplacePackage.split('.')
val newPackagePath = newPackage.replace('.', File.separatorChar)

val dirsToMove = linkedMapOf<File, File>()

rootFile.walkTopDown()
    .onEnter { dir -> dir.shouldEnterDirectory() }
    .filter { it.isDirectory && it.endsWithPathSegments(oldPackageSegments) }
    .forEach { oldDir ->
        val packageBaseDir = oldDir.parentBeforePathSegments(oldPackageSegments)
        dirsToMove[oldDir] = File(packageBaseDir, newPackagePath)
    }

if (dirsToMove.isEmpty()) {
    println("Warning: No directories found matching the old package path: ${oldPackageSegments.joinToString(File.separator)}")
} else {
    dirsToMove.forEach { (oldDir, newDir) ->
        try {
            println("Renaming: ${oldDir.path}")
            println("      to: ${newDir.path}")

            if (!newDir.parentFile.isDirectory) {
                newDir.parentFile.mkdirs()
            }

            Files.move(oldDir.toPath(), newDir.toPath(), StandardCopyOption.ATOMIC_MOVE)
            deleteEmptyParentDirs(oldDir.parentFile)
        } catch (e: Exception) {
            System.err.println("Error moving directory ${oldDir.path}: ${e.message}, exception -> ${e.stackTraceToString()}")
        }
    }
}
println("Step 2: Finished.\n")

// --- 辅助函数 ---

fun File.isTargetFile(): Boolean {
    return checkFileSuffix.any { name.endsWith(it) }
}

fun File.shouldEnterDirectory(): Boolean {
    if (canonicalFile == rootFile) return true
    if (ignoreFiles.contains(canonicalFile)) return false
    if (name in ignoreDirNames) return false

    val relativePath = rootFile.toPath().relativize(canonicalFile.toPath())
    val firstSegment = relativePath.firstOrNull()?.toString()
    return firstSegment !in ignoreRootDirNames
}

fun File.replaceContent(rules: List<ReplacementRule>) {
    val originalText = readText()
    val replacedText = rules.fold(originalText) { current, rule ->
        current.replace(rule.oldValue, rule.newValue)
    }.removeDuplicateImports()

    if (replacedText != originalText) {
        writeText(replacedText)
        println("  -> Replaced content in: $path")
    }
}

fun File.endsWithPathSegments(expectedSegments: List<String>): Boolean {
    val pathSegments = canonicalFile.toPath().map { it.toString() }
    return pathSegments.takeLast(expectedSegments.size) == expectedSegments
}

fun File.parentBeforePathSegments(pathSegments: List<String>): File {
    var current = canonicalFile
    repeat(pathSegments.size) {
        current = current.parentFile
    }
    return current
}

fun String.removeDuplicateImports(): String {
    if (!contains("import ")) return this

    val lineSeparator = if (contains("\r\n")) "\r\n" else "\n"
    val normalized = replace("\r\n", "\n")
    val hasTrailingLineSeparator = normalized.endsWith("\n")
    val sourceLines = normalized.removeSuffix("\n").split("\n")
    val seenImports = mutableSetOf<String>()
    val deduplicatedLines = sourceLines.filter { line ->
        val normalizedLine = line.trim()
        if (!normalizedLine.startsWith("import ")) {
            true
        } else {
            seenImports.add(normalizedLine)
        }
    }

    return buildString {
        append(deduplicatedLines.joinToString(lineSeparator))
        if (hasTrailingLineSeparator) {
            append(lineSeparator)
        }
    }
}

/**
 * 递归删除空的父目录。
 */
fun deleteEmptyParentDirs(dir: File?) {
    if (dir == null || !dir.canonicalPath.startsWith(rootFile.canonicalPath)) {
        return
    }

    if (dir.isDirectory && dir.listFiles()?.isEmpty() == true) {
        val parent = dir.parentFile
        if (dir.delete()) {
            println("  -> Deleted empty directory: ${dir.path}")
            deleteEmptyParentDirs(parent)
        }
    }
}
