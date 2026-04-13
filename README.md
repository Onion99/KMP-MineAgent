#### Option 2: Build from Source

```bash
# Clone the repository
git clone https://github.com/Onion99/KMP-MineAgent.git
cd KMP-MineAgent

# Install Protobuf https://github.com/protocolbuffers/protobuf/releases
# add path to system environment D:\MyApp\Code\protoc-25.9-win64\bin
protoc --version

# https://rustup.rs  downAndRun rustup-init.exe
rustc -V

# https://github.com/google/flatbuffers
flatc

# Build for Desktop
./gradlew :composeApp:run

# Build for Android
./gradlew :composeApp:assembleDebug
```


