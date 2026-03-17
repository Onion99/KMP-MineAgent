import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isDirectory

// ================== 配置区 ==================
val newPackage = "org.onion.agent"
// 被替换的包名
val needToReplacePackage = "org.onion.diffusion"
val rootDir = ".\\"
val ignoreDirs = listOf(".\\.fleet",".\\.gitignore",".\\.kotlin", ".\\.idea",
    ".\\build", ".\\.gradle", ".\\gradle", ".\\.git", ".\\iosApp", ".\\cpp",".\\composeApp\\.cxx",".\\composeApp\\build")
val checkFileSuffix = listOf(".kt", ".java", ".xml"/*, ".kts"*/, ".pro") // 增加了更多可能包含包名的文件类型
// ============================================

val rootFile = File(rootDir).canonicalFile
val ignoreFiles = ignoreDirs.map {
    File(rootFile, it).canonicalFile
}

// --- 步骤 1: 修改文件内容 ---
println("Step 1: Replacing package content in files...")
rootFile.walkTopDown()
    .onEnter { dir -> !ignoreFiles.contains(dir.canonicalFile) } // 避免进入忽略的目录
    .filter { it.isFile && it.isTargetFile() }
    .forEach { file ->
        file.replacePackageContent()
    }
println("Step 1: Finished.\n")


// --- 步骤 2: 移动/重命名目录 ---
println("Step 2: Finding and moving directories...")
val oldPackagePath = needToReplacePackage.replace('.', File.separatorChar) // "org\onion\gpt"
val newPackagePath = newPackage.replace('.', File.separatorChar) // "org\onion\diffusion"

val dirsToMove = mutableMapOf<File, File>()

// 使用更健壮的方式查找所有匹配旧包名路径的目录
rootFile.walkTopDown()
    .onEnter { dir -> !ignoreFiles.contains(dir.canonicalFile) }
    .filter { it.isDirectory && it.canonicalPath.endsWith(oldPackagePath) }
    .forEach { oldDir ->
        // 计算新目录的路径
        // oldDir.parentFile.path 是 ...\org\onion
        // newPackagePath 是 org\onion\diffusion
        val newDir = File(oldDir.parentFile, newPackagePath.substringAfterLast(File.separatorChar))
        dirsToMove[oldDir] = newDir
    }

if (dirsToMove.isEmpty()) {
    println("Warning: No directories found matching the old package path: $oldPackagePath")
} else {
    dirsToMove.forEach { (oldDir, newDir) ->
        try {
            println("Renaming: ${oldDir.path}")
            println("      to: ${newDir.path}")

            // 确保新目录的父目录存在
            if(newDir.parentFile.isDirectory.not()){
                newDir.parentFile.mkdirs()
            }

            // 直接移动/重命名，这是原子操作，比逐个文件移动更高效、更安全
            Files.move(oldDir.toPath(), newDir.toPath(), StandardCopyOption.ATOMIC_MOVE)

            // 清理旧的、可能变空的父目录
            deleteEmptyParentDirs(oldDir.parentFile)

        } catch (e: Exception) {
            System.err.println("Error moving directory ${oldDir.path}: ${e.message},exception -> ${e.stackTraceToString()}")
        }
    }
}
println("Step 2: Finished.\n")


// --- 辅助函数 ---

fun File.isTargetFile(): Boolean {
    return checkFileSuffix.any { this.name.endsWith(it) }
}

fun File.replacePackageContent() {
    val originalText = this.readText()
    if (originalText.contains(needToReplacePackage)) {
        val newText = originalText.replace(needToReplacePackage, newPackage)
        this.writeText(newText)
        println("  -> Replaced content in: ${this.path}")
    }
}

/**
 * 递归删除空的父目录
 */
fun deleteEmptyParentDirs(dir: File?) {
    if (dir == null || !dir.canonicalPath.startsWith(rootFile.canonicalPath)) {
        return // 安全检查，防止删除到项目根目录之外
    }

    if (dir.isDirectory && (dir.listFiles()?.isEmpty() == true)) {
        val parent = dir.parentFile
        if (dir.delete()) {
            println("  -> Deleted empty directory: ${dir.path}")
            deleteEmptyParentDirs(parent) // 递归检查上一级
        }
    }
}