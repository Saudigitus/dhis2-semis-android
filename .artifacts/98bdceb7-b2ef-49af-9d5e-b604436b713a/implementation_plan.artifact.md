# Fix Unresolved Reference 'atomic' in CoroutineTracker

The `atomicfu` version `0.33.0` currently used in the project is incompatible with Kotlin `2.0.21` (it requires Kotlin `2.2.0` or newer). This results in the `Unresolved reference 'atomic'` error during compilation.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Administrator/Documents/Saudigitus/dhis2-semis-android/gradle/libs.versions.toml)
- Downgrade `atomicfu` version from `0.33.0` to `0.26.0`, which is compatible with Kotlin `2.0.21`.

#### [MODIFY] [gradle.properties](file:///C:/Users/Administrator/Documents/Saudigitus/dhis2-semis-android/gradle.properties)
- Add `kotlinx.atomicfu.enableJvmIrTransformation=true` to ensure proper IR transformation for JVM targets when using Kotlin 2.0+.

## Verification Plan

### Automated Tests
- Run the failing compilation task:
  ```bash
  ./gradlew :commonskmm:compileDebugKotlinAndroid
  ```

### Manual Verification
- Verify that the `CoroutineTracker.kt` file no longer shows compilation errors in the IDE.
