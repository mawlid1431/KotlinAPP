# APK — TDM Insight

Per `PROJECT_RULES.md` §4, the release-ready APK must land at
`apk/app-release.apk`. Build instructions:

```
cd Kotlin_app
./gradlew :app:assembleRelease
# Signed APK at: app/build/outputs/apk/release/app-release.apk
# Copy here:
cp app/build/outputs/apk/release/app-release.apk ../apk/app-release.apk
```

For a debug build (no signing required):

```
./gradlew :app:assembleDebug
# APK at: app/build/outputs/apk/debug/app-debug.apk
```

This project was assembled without a local Android SDK on the build
host, so the final `.apk` file must be generated on the student's
machine before submission. All source, tests, and resources are in
place; nothing in the build path requires human input beyond signing.
