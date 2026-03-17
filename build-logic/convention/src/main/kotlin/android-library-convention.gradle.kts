plugins {
    id("com.android.library")
}

android {
    commonConfiguration(this)

    defaultConfig {
        lint.targetSdk = 35
        minSdk = 24
    }

    sourceSets {
        named("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res")
        }
    }
}
// 添加toml中的android-desugar-jdk-libs,确保在toml文件中存在
addDesugarDependencies()