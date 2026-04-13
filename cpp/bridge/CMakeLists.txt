cmake_minimum_required(VERSION 3.25)
# 设置最长路径,防止长路径在windows平台上报错
set(CMAKE_OBJECT_PATH_MAX 260)
set(CMAKE_OBJECT_NAME_MAX 260)
project(llm_loader)

set(KISSFFT_TEST OFF CACHE BOOL "" FORCE)
set(KISSFFT_TOOLS OFF CACHE BOOL "" FORCE)
set(KISSFFT_USE_OPENMP OFF CACHE BOOL "" FORCE)
set(KISSFFT_PKGCONFIG OFF CACHE BOOL "" FORCE)
set(BUILD_TESTING OFF CACHE BOOL "" FORCE)

# 直接指向真正的构建包，而非顶层 orchestrator（后者使用 ExternalProject，
# 不会将 LiteRTLM::* targets 暴露给父 CMake 作用域）。
set(LITERTLM_PROJECT_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../lite-rt-lm"
    CACHE PATH "LiteRT-LM: Absolute path to the repository root")

# Android 交叉编译修复：protobuf_external 需要一个能在宿主机（x86_64）运行的
# protoc 可执行文件来进行代码生成，但交叉编译工具链无法产生宿主机二进制。
# 通过设置 LITERTLM_HOST_PROTOC，内层包会跳过 protoc 的交叉编译，直接使用
# 宿主机上已安装的 protoc。
if(ANDROID)
    if(NOT DEFINED LITERTLM_HOST_PROTOC)
        # 优先使用用户通过 -DLITERTLM_HOST_PROTOC=... 显式指定的路径，
        # 否则在宿主机 PATH 中自动查找。
        find_program(_HOST_PROTOC_EXE
            NAMES protoc
            # Android NDK 工具链将 find_program 限制在 sysroot 中；
            # NO_CMAKE_FIND_ROOT_PATH 强制搜索宿主机 PATH。
            NO_CMAKE_FIND_ROOT_PATH
            DOC   "Host protoc used for .proto code-generation during Android cross-compilation"
        )
        if(_HOST_PROTOC_EXE)
            message(STATUS "[bridge] Android build: using host protoc at ${_HOST_PROTOC_EXE}")
            get_filename_component(_HOST_PROTOC_BIN_DIR "${_HOST_PROTOC_EXE}" DIRECTORY)
            set(LITERTLM_HOST_PROTOC     "${_HOST_PROTOC_EXE}"    CACHE INTERNAL "Host protoc for Android cross-compilation")
            set(LITERTLM_HOST_PROTOC_BIN_DIR "${_HOST_PROTOC_BIN_DIR}" CACHE INTERNAL "Host protoc bin dir")
        else()
            message(WARNING
                "[bridge] Android cross-compilation: 'protoc' not found on PATH.\n"
                "  Install protobuf on your host machine (e.g. 'choco install protoc' on Windows,\n"
                "  'brew install protobuf' on macOS, or 'apt install -y protobuf-compiler' on Linux)\n"
                "  OR pass -DLITERTLM_HOST_PROTOC=/path/to/protoc to CMake.\n"
                "  Without a host protoc, .proto code-generation will fail."
            )
        endif()
    endif()

    if(NOT DEFINED LITERTLM_HOST_FLATC)
        # Find host flatc for Android cross-compilation
        find_program(_HOST_FLATC_EXE
            NAMES flatc
            NO_CMAKE_FIND_ROOT_PATH
            DOC   "Host flatc used for .fbs code-generation during Android cross-compilation"
        )
        if(_HOST_FLATC_EXE)
            message(STATUS "[bridge] Android build: using host flatc at ${_HOST_FLATC_EXE}")
            get_filename_component(_HOST_FLATC_BIN_DIR "${_HOST_FLATC_EXE}" DIRECTORY)
            set(LITERTLM_HOST_FLATC     "${_HOST_FLATC_EXE}"    CACHE INTERNAL "Host flatc for Android cross-compilation")
            set(LITERTLM_HOST_FLATC_BIN_DIR "${_HOST_FLATC_BIN_DIR}" CACHE INTERNAL "Host flatc bin dir")
        else()
            message(WARNING
                "[bridge] Android cross-compilation: 'flatc' not found on PATH.\n"
                "  Install flatbuffers on your host machine (e.g. 'vcpkg install flatbuffers' or download from GitHub)\n"
                "  OR pass -DLITERTLM_HOST_FLATC=/path/to/flatc to CMake.\n"
                "  Without a host flatc, .fbs code-generation will fail."
            )
        endif()
    endif()
endif()

# Forward toolchain file and cross-compilation args to ExternalProject_Add in litert_lm dependencies
if(DEFINED CMAKE_TOOLCHAIN_FILE)
    set(LITERTLM_TOOLCHAIN_FILE "-DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}")
endif()
if(ANDROID)
    set(LITERTLM_TOOLCHAIN_ARGS
        "-DANDROID_ABI=${ANDROID_ABI}"
        "-DANDROID_PLATFORM=${ANDROID_PLATFORM}"
        "-DANDROID_NDK=${ANDROID_NDK}"
        "-DCMAKE_MAKE_PROGRAM=${CMAKE_MAKE_PROGRAM}"
        "-DCMAKE_SYSTEM_NAME=${CMAKE_SYSTEM_NAME}"
        "-DCMAKE_SYSTEM_VERSION=${CMAKE_SYSTEM_VERSION}"
    )
endif()

add_subdirectory(../lite-rt-lm/cmake/packages/litert_lm lite-rt-lm-build)

add_library(
    litertlm_jni
    SHARED
    ../lite-rt-lm/kotlin/java/com/google/ai/edge/litertlm/jni/litertlm.cc
)

target_include_directories(
    litertlm_jni
    PRIVATE
        ${LITERTLM_ROOT_DIR}
        ${LITERTLM_INCLUDE_PATHS}
        ${THIRD_PARTY_DIR}/json/include
)

target_link_libraries(
    litertlm_jni
    PRIVATE
        LiteRTLM::Runtime::Engine::Lib
        LiteRTLM::Runtime::Conversation
        LiteRTLM::Runtime::Conversation::IoTypes
        LiteRTLM::Runtime::Engine::Interface
        LiteRTLM::Runtime::Engine::Settings
        LiteRTLM::Runtime::Engine::IoTypes
        LiteRTLM::nlohmann_json::nlohmann_json
        LITERTLM_DEPS
        log
)
