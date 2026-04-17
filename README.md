#### Option 2: Build from Source

```bash
# Clone the repository
git clone https://github.com/Onion99/KMP-MineAgent.git
cd KMP-MineAgent

# Install Protobuf https://github.com/protocolbuffers/protobuf/releases
# add path to system environment D:\MyApp\Code\protoc-25.9-win64\bin
protoc --version

# rust https://rustup.rs  downAndRun rustup-init.exe
rustc -V

# flatbuffers https://github.com/google/flatbuffers
flatc

# Bazel winget install Bazel.Bazelisk / brew install bazelisk https://github.com/bazelbuild/bazel/releases
# Bazelisk  https://github.com/bazelbuild/bazelisk
bazel
# Install Visual Studio 2022 https://visualstudio.microsoft.com/zh-hans/visual-cpp-build-tools/ → select C++ developer

# Build for Desktop
./gradlew :composeApp:run

# Build for Android
./gradlew :composeApp:assembleDebug
```


