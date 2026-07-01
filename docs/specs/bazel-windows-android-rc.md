# Bazel Windows/Android RC 配置边界

## 背景

项目通过 `composeApp:buildAndroidNativeLib` 调用 `cpp/lite-rt-lm` 工作区内的 Bazel 目标：

```text
//kotlin/java/com/google/ai/edge/litertlm/jni:litertlm_jni
```

Gradle 任务会额外传入仓库根目录的 `.bazelrc.user`，用于设置本机 Bazel 输出目录、Visual Studio 工具链路径，以及 Windows host 编译参数。

## 约束

`/utf-8` 是 MSVC 风格编译参数，不能通过全局 `build --copt` 或 `build --cxxopt` 注入。Android 交叉编译使用 NDK `clang.exe`，该驱动不会把 `/utf-8` 当作编码参数处理，而是按输入文件解析，导致如下错误：

```text
clang: error: no such file or directory: '/utf-8'
```

因此 `.bazelrc.user` 必须遵循以下边界：

- Android target 编译不能收到 `/utf-8`。
- Windows host 工具编译可以通过 `--config=win_host` 收到 `/utf-8`。
- Windows desktop target 编译必须显式启用 `--config=msvc_target_utf8`，不能依赖全局 target copts。

## 当前配置

`.bazelrc.user` 中保留 Visual Studio 路径，并将 UTF-8 参数拆到显式配置：

```text
build:msvc_target_utf8 --copt=/utf-8
build:msvc_target_utf8 --cxxopt=/utf-8
build:msvc_target_utf8 --host_copt=/utf-8
build:msvc_target_utf8 --host_cxxopt=/utf-8
build:win_host --host_copt=/utf-8
build:win_host --host_cxxopt=/utf-8
```

`composeApp/build.gradle.kts` 的 Windows desktop 原生库任务显式传入 `--config=msvc_target_utf8`。Android 原生库任务在 Windows 主机上只传入 `--config=win_host`，从而只影响 host 工具链，不污染 Android NDK target 编译。

## 验证

Windows 上执行 Android 原生库构建时需要 Java 21+：

```powershell
$env:JAVA_HOME='D:\MyApp\Code\Android\android-studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :composeApp:buildAndroidNativeLib --no-configuration-cache
```

期望结果：

- Bazel `android_arm64` target 编译不再出现 `clang: error: no such file or directory: '/utf-8'`。
- 构建产物输出到 `cpp/libs/liblitertlm_jni.so`。
- Android 运行时产物同步到 `composeApp/src/androidMain/jniLibs/arm64-v8a/liblitertlm_jni.so`。
