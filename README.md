# Grade 12 Textbooks

Phase 0 Android project scaffold for the standalone Grade 12 Textbooks app.

The release catalog endpoint is supplied through the uncommitted Gradle property
`catalog.baseUrl`. Copy `local.properties.example` to `local.properties` for local
debug configuration. Real textbook URLs and checksums must be verified before use.

## Download scheduling

Downloads use persistent WorkManager unique work named `download:<bookId>` with a
Room-backed dispatcher that permits at most two active transfers. This implementation
does not declare UIDT or foreground-service permissions. UIDT is reserved for a later
platform-specific implementation; the current cross-version path remains WorkManager.

## Release checklist

1. Configure `catalog.baseUrl` in uncommitted `local.properties`.
2. Configure release signing from local properties; never commit a keystore or passwords.
3. Bump `versionCode` and `versionName`, then run `./gradlew assembleRelease`.
4. Create a GitHub Release and attach the signed APK.
5. Update `version.json` with the release metadata and verified HTTPS APK URL.

The same signing keystore must be retained for every future APK update. Losing it
prevents Android from accepting later updates over the installed APK.
