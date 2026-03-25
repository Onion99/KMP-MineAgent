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

add_subdirectory(../lite-rt-lm lite-rt-lm-build)

add_library(
    litertlm_jni
    SHARED
    ../lite-rt-lm/kotlin/java/com/google/ai/edge/litertlm/jni/litertlm.cc
)

target_link_libraries(
    litertlm_jni
    PRIVATE
        "-Wl,--allow-multiple-definition"
        "-Wl,--start-group"
        "-Wl,--whole-archive"
        ${_LITERTLM_ODML_EXPANDED_LIST}
        "-Wl,--no-whole-archive"
        ${_LITERTLM_CORE_EXPANDED_LIST}
        libpng_lib
        kissfft_lib
        miniaudio_lib
        minizip_lib
        minja_lib
        zlib_lib
        antlr_lib
        LiteRTLM::nlohmann_json::nlohmann_json
        opencl_headers_lib
        "-lz"
        "-lrt"
        "-lpthread"
        "-ldl"
        "-Wl,--end-group"
)
