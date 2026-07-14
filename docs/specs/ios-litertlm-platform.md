# iOS LiteRT LM Native Bridge

Date: 2026-07-14

## Scope

This note records the iOS native bridge for `LiteRtLmJni`.

## Current Behavior

- `composeApp` no longer registers the old Kotlin/Native cinterop for `sdloader.def`.
- iOS framework builds no longer link the legacy `stable-diffusion.cpp` static libraries from Gradle.
- `composeApp/src/nativeInterop/cinterop/litertlm.def` maps the LiteRT LM C API from `cpp/lite-rt-lm/c/engine.h`.
- iOS framework linking expects an aggregate native library named `liblitertlm_c_api.a` or `liblitertlm_c_api.dylib` in both `cpp/libs/ios-device` and `cpp/libs/ios-simulator`.
- `validateIosLiteRtLmNativeLibs` runs on macOS before iOS link tasks and fails early if the aggregate library is missing.
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
- This change does not build the `liblitertlm_c_api` aggregate library. That native packaging step must produce the expected device and simulator artifacts before iOS linking can succeed.
