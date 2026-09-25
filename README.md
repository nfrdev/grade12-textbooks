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
