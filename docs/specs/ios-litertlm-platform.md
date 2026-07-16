# iOS LiteRT LM Native Bridge

Date: 2026-07-15

## Scope

This note records the iOS native bridge for `LiteRtLmJni`.

## Current Behavior

- `composeApp` no longer registers the old Kotlin/Native cinterop for `sdloader.def`.
- iOS framework builds no longer link the legacy `stable-diffusion.cpp` static libraries from Gradle.
- `composeApp/src/nativeInterop/cinterop/litertlm.def` maps the LiteRT LM C API from `cpp/lite-rt-lm/c/engine.h`.
- Kotlin/Native cinterop must include `cpp/lite-rt-lm/c` directly because `litertlm.def` declares `headers = engine.h`. Including only `cpp/lite-rt-lm` makes CI/Xcode cinterop fail with `fatal error: 'engine.h' file not found`.
- iOS framework linking expects a native library named `liblitertlm_c_api.a` or `liblitertlm_c_api.dylib` in both `cpp/libs/ios-device` and `cpp/libs/ios-simulator`.
- `//c:engine` is a Bazel `cc_library`; its direct output `bazel-bin/c/libengine.a` is a thin archive containing the C API object but not the C++/Abseil/proto/Rust transitive archives required by Kotlin/Native.
- `BuildIosLiteRtLmNativeArchiveTask` must build the configured Bazel C/ObjC, `cc_proto_library`, `rust_library`, and `rust_static_library` dependency labels, collect configured `.a` and `.rlib` outputs from `deps(//c:engine)`, and merge them with `/usr/bin/libtool -static` into the final `liblitertlm_c_api.a` copied under `cpp/libs/ios-*`.
- The dependency build query must not include `rust_proc_macro` labels. Rust proc-macros are exec tools; building them as top-level iOS targets can compile crates such as `cxxbridge_macro` for `aarch64-apple-ios` instead of the macOS host.
- The LiteRT LM Rust graph pulls `darling_core`, which derives and parses `syn` AST types. The upstream submodule lockfile keeps `syn`'s `extra-traits`, `full`, `visit`, and `visit-mut` features behind desktop-only generated selects, so iOS `aarch64-apple-ios` builds can fail with missing `Debug`, `Eq`, `Hash`, `Parse`, or AST field errors.
- Root Gradle must not edit `cpp/lite-rt-lm/Cargo.toml`, `Cargo.lock`, `cargo-bazel-lock.json`, or `WORKSPACE` as a local workaround. Instead, `BuildIosLiteRtLmNativeArchiveTask` runs `bazel sync --only=crate_index`, fetches `@crate_index__syn-2.0.114//:syn`, resolves Bazel `output_base`, and patches both `external/crate_index/BUILD.syn-2.0.114.bazel` and the actual per-crate repo `external/crate_index__syn-2.0.114/BUILD.bazel` with `syn` feature selects for the active `aarch64-apple-ios` and `aarch64-apple-ios-sim` triples before invoking the iOS Bazel build. The patch must be scoped to the generated `crate_features = select({ ... })` block; `target_compatible_with` selects may contain the same platform keys and must not receive Rust feature strings. If an iOS platform select already exists in `crate_features`, Gradle must add only the missing feature strings inside that existing list instead of inserting a duplicate dictionary key.
- `BuildIosLiteRtLmNativeArchiveTask` must set `HERMETIC_PYTHON_VERSION=3.12` for Bazel `sync`, `build`, and `cquery` calls through both the process environment and `--repo_env`. XLA no longer provides a Python 3.9 requirements lock, so relying on the repository rule default can fail before compilation starts.
- Bazel `cquery --output=label` may append a configuration suffix such as ` (ccfbe96)`; Gradle must strip that suffix before passing labels back to `bazel build`.
- iOS Kotlin/Native linker options include `-lc++` because the C API archive contains C++ objects even though the cinterop surface is a C header.
- iOS Kotlin/Native linker options also include `libLiteRt.dylib`, `libGemmaModelConstraintProvider.dylib`, and `libLiteRtMetalAccelerator.dylib` from `cpp/lite-rt-lm/prebuilt/ios_arm64` or `cpp/lite-rt-lm/prebuilt/ios_sim_arm64`. These remain dynamic prebuilt LiteRT dependencies and are not merged into `liblitertlm_c_api.a`.
- iOS Kotlin/Native linker options include `AVFoundation` because the LiteRT LM audio preprocessing path pulls in miniaudio objects that reference `AVAudioSession`.
- The active iOS target matrix is `iosArm64` and `iosSimulatorArm64`; `iosX64` is intentionally not registered.
- `iosArm64Main` and `iosSimulatorArm64Main` must explicitly depend on the shared `iosMain` source set so `src/iosMain/kotlin` actual declarations are visible to Kotlin/Native expect/actual matching.
- `validateIosLiteRtLmNativeLibs` runs on macOS before iOS link tasks and fails early if the required native archive or required prebuilt LiteRT dylibs are missing.
- iOS native build fixes must be applied in the root Gradle wiring and project docs unless the submodule source itself is intentionally being upgraded. Do not patch `cpp/lite-rt-lm/WORKSPACE` or generated lock files as a local-only workaround because submodule sync will discard or conflict with those edits.
- `buildIosLiteRtLmNativeLibs` builds the device arm64 archive and simulator arm64 archive directly; it does not build `ios_x86_64` or merge simulator archives with `lipo`.
- GitHub Actions iOS builds install Bazelisk, restore the Bazel disk cache, and rewrite the repository-root `.bazelrc.user` before invoking Gradle. The iOS Bazel tasks must not inherit local developer paths such as `G:/_b` or Windows-only `BAZEL_VC` values.
- GitHub Actions Bazel cache keys must include `composeApp/build.gradle.kts`, `cpp/lite-rt-lm/Cargo.toml`, `Cargo.lock`, `cargo-bazel-lock.json`, and `PATCH.*`; otherwise Rust crate feature, generated-repo patching, or patch changes can restore a disk cache keyed only on C++/Bazel files.
- `LiteRtLmJni` now has an iOS `actual` implementation that calls the LiteRT LM C API for engine creation, conversation creation, synchronous message sending, streaming message sending, cancellation, and release.
- iOS model selection uses FileKit and returns the selected `.litertlm` path.
- `sendLmMessageAsync` uses a Kotlin/Native `StableRef` as callback state and disposes it on final/error stream callbacks.

## Native Contract

- The native library must export the `litert_lm_*` symbols declared by `cpp/lite-rt-lm/c/engine.h`.
- `LiteRtLmJni.ios.kt` stores native pointers as `Long` handles to keep the common API aligned with Android and Desktop.
- `LmEngine` remains responsible for deleting engine handles through `deleteLmEngine`.
- `LmConversation` remains responsible for deleting conversation handles through `deleteLmConversation`.
- `SamplerConfig` is mapped to the C API `LiteRtLmSamplerParams` using `kLiteRtLmSamplerTypeTopP`, matching the JNI implementation.

## Current Limitations

- The LiteRT LM C API does not expose the common API's `mainBackendNumThreads`, `audioBackendNumThreads`, `channelsJsonString`, `overwritePromptTemplate`, or `visualTokenBudget` inputs. The iOS bridge currently ignores those fields.
- Intel iOS Simulator is not supported by the current target matrix. Reintroducing it requires adding `iosX64`, an `ios_x86_64` Bazel build, and an explicit simulator archive merge step.
