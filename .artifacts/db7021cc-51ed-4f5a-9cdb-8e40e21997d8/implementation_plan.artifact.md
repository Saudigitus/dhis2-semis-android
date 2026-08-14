# Fix Unresolved Reference: androidx in build.gradle.kts

The project is failing to sync because the `androidx.room` and `ksp` plugins (and related Room libraries) are referenced in the `semis-core/data` module but are missing from the `gradle/libs.versions.toml` version catalog.

## Proposed Changes

### [gradle](file:///C:/Users/Administrator/Documents/Saudigitus/dhis2-semis-android/gradle)

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Administrator/Documents/Saudigitus/dhis2-semis-android/gradle/libs.versions.toml)
- Add `room` and `ksp` versions.
- Add Room library definitions (`room-runtime`, `room-ktx`, `room-compiler`).
- Add `androidx-room` and `ksp` plugin definitions.

## Verification Plan

### Automated Tests
- Run Gradle Sync to verify that all references are resolved.
- Build the `semis-core:data` module using `./gradlew :semis-core:data:assembleDebug`.

### Manual Verification
- Verify that the Room database in `semis-core/data` compiles correctly with KSP.
