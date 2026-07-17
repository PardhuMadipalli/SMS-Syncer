# SMS Syncer Agent Guide

## Project conventions
- Read the root [README.md](README.md) before making changes. It is the only project README; do not create README files in subdirectories.
- Update the root README whenever a change affects user-visible behavior, configuration, architecture, or developer workflow.
- This is a Kotlin Android app using XML layouts, View Binding, and Material Components. Do not introduce Jetpack Compose unless the requested work explicitly requires it.

## Build and device validation
- Build the debug APK from the repository root with `./gradlew assembleDebug`.
- Run unit tests with `./gradlew test` when they cover the changed behavior. Run `./gradlew connectedDebugAndroidTest` only with a connected, explicitly selected test device.
- The debug APK is `app/build/outputs/apk/debug/app-debug.apk`; the application ID is `com.pardhu.smssyncer`.
- For ADB deployment, always select the intended device explicitly: `adb -s <serial> install -r app/build/outputs/apk/debug/app-debug.apk`.

## Security and privacy
- Treat SMS bodies, sender details, ntfy topics, encryption passwords, and signing material as sensitive. Do not log, commit, or include them in documentation or error messages.
- Keep the existing HTTPS-only, input-sanitization, encrypted-preference, and permission-validation protections intact unless the request explicitly changes them.
- `app/signing.properties`, keystores, APKs, AABs, and build outputs are local artifacts and must not be committed.

## Android notification channels
- Notification channel IDs persist on installed devices. When replacing or removing a channel ID, migrate existing installs by explicitly deleting the obsolete channel after creating its replacement.
