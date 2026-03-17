
plugins {
    // kotlin-dsl 插件。这个插件的作用就是将 src/main/kotlin 目录下的 .gradle.kts 文件编译成真正的、可以被其他模块引用的 Gradle 插件
    `kotlin-dsl`
    // 统一Kotlin版本,避免因kotlin版本不导致的重冲突”
    alias(libs.plugins.kotlinJvm)
}

group = "com.onion.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.compose.compiler.plugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.kotlin.serializationPlugin)
}