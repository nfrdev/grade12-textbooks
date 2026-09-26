# Grade 12 Textbooks App — AI Build Prompt Package (v4)

**Scope:** Grade 12 only · Natural Science + Social Science streams · Standalone APK (no Play Store)

**Hosting:** GitHub (raw files for `catalog.json` and `version.json`)

**Changes from v3:** removed the impossible Room+DataStore atomic-transaction requirement; made Room the source of truth for catalog metadata; separated remote catalog metadata from local mutable state; added `addedAt` for deterministic Recently Added ordering; replaced fake third-party sample URLs with local/test-only fixtures; strengthened HTTPS, redirect, response-size, and catalog integrity validation; added orphan reappearance handling; clarified delete semantics; redesigned download scheduling so Phase 2a owns only the transfer engine while Phase 2b owns persistent scheduling and concurrency; and added explicit modern-Android handling for API 34+ user-initiated data transfers and API 35+ `dataSync` limits.

**Current-platform note:** Verify these statements against the current Android Developers documentation before implementation: Android 14+ user-initiated data transfer jobs, Android 15 `dataSync` foreground-service limits, and Android 16 background-work changes.

Use these prompts **one phase at a time** with an AI coding assistant (Claude Code, Cursor, Codex, Antigravity, etc.).
Each phase assumes the previous one is complete and working. Do **not** paste the entire file at once.

---

## Assistant Operating Rules (apply to every phase)

Before writing any code:

1. **Inspect the existing project first.** Read the relevant files and report the current state before proposing changes.
2. **Output a short plan first.** List the files you will create/modify and what each change does. Do not start coding on the first turn.
3. **Ask one clarifying question only when a requirement is genuinely ambiguous.** Otherwise make the smallest assumption consistent with this package and state it.
4. **Do not add libraries, screens, permissions, features, navigation routes, or network services** that are not explicitly allowed by this package or required by a platform API selected in a phase. If a platform API requires a new permission, identify it before implementation.
5. **Do not silently change architecture.** If a different mechanism is required for correctness or current-platform compliance, explain the difference and make the smallest compatible change.
6. **Do not modify unrelated files or refactor earlier phases for style.** Earlier-phase changes are allowed only when required to integrate the current phase or fix a verified defect.
7. **If a Definition of Done item cannot be satisfied as written**, stop and report the item, why it cannot be met, the minimal change required, and any Android/version limitation. Do not silently degrade behavior.
8. **Never fabricate external resources.** Do not invent real-looking textbook URLs, checksums, repository contents, publisher information, or release artifacts. Use local/mock fixtures until real values are supplied and verified.
9. **Never expose secrets.** Do not put tokens, credentials, private keys, keystore passwords, or local machine paths containing secrets in source control or logs.
10. **After completing a phase**, output a DoD checklist mapped to files/classes/tests, a git commit message, changed files, and known limitations/unverified behavior.
11. **All user-facing strings** go in `strings.xml`. No hardcoded user-facing text in composables, dialogs, notifications, or UI error messages.
12. **Run the required tests/build checks before declaring the phase complete.** Report the actual result; never claim a test passed unless it was run.

---

## App Flow (simplified)

```

APP START
│
▼
HOME
│
▼
SELECT STREAM
┌───────┴────────┐
│                 │
Natural Science  Social Science
│                 │
└───────┬─────────┘
▼
SUBJECTS
│
▼
BOOKS
│
▼
BOOK DETAILS
│
▼
DOWNLOAD
│
▼
LOCAL STORAGE
│
▼
READER
┌────┴────┐
│         │
Progress  Bookmarks
│         │
└────┬────┘
▼
MY BOOKS

```

No grade picker exists anywhere — the app is permanently Grade 12 only.

---

## Non-Negotiable Constraints

**Architecture & stack**
- Package name: `com.nfrdev.grade12textbooks`
- Kotlin + Jetpack Compose + MVVM + Clean Architecture
- Single Gradle module is acceptable; use the folder structure below
- Hilt for DI
- Room for local database
- Retrofit + OkHttp + **kotlinx.serialization** for network
- WorkManager for background downloads
- DataStore (Preferences) for user settings
- **Coil** for image loading (book covers)
- **Timber** for logging
- Gradle **version catalog** (`gradle/libs.versions.toml`) — no inline dependency versions
- Min SDK 24 · target latest stable SDK · compile latest stable SDK

**Permissions allow-list:**
- `INTERNET`
- `POST_NOTIFICATIONS` (runtime-requested on API 33+; denial must never crash the app)
- `FOREGROUND_SERVICE` only if the selected API 24–33 fallback uses a foreground service
- `FOREGROUND_SERVICE_DATA_SYNC` only if the selected API 34+ fallback still requires a `dataSync` foreground service
- `RUN_USER_INITIATED_JOBS` only if the API 34+ user-initiated data transfer path is implemented

The final manifest must contain only permissions required by the chosen implementation. Do not declare transfer-related permissions “just in case.” Verify the current Android Developers documentation for UIDT permissions before implementation.

**Forbidden permissions:**
- `WRITE_EXTERNAL_STORAGE`, `READ_EXTERNAL_STORAGE`, `MANAGE_EXTERNAL_STORAGE`
- `REQUEST_INSTALL_PACKAGES` (unless in-app APK install is added in a later phase — it is not in v1)

**Storage**
- PDFs live **only** in `context.getExternalFilesDir(null)/books/`
- Temporary partial files use `getExternalFilesDir(null)/books/{bookId}.pdf.part`
- Never request legacy external storage permissions
- Store only app-generated filenames based on validated `bookId`; never trust remote filenames

**Forbidden libraries / services**
- No Google Play Services, no In-App Updates, no Firebase, no analytics SDKs, no crash-reporting SaaS
- Standalone APK only (sideload)

**Threading & error handling**
- All network + database work on `Dispatchers.IO`
- UseCases return sealed `Result` / `Resource` types — never throw to the UI layer. Map domain/data failures to stable error codes/types; ViewModels select localized strings from `strings.xml` rather than displaying raw exception text.
- ViewModels stay thin — only call UseCases and expose `StateFlow` / `SharedFlow`
- No business logic in composables

**Identity & keys**
- `subjectId` is **not globally unique** — the same id (e.g. `"mathematics"`) exists in both streams.
- Every query, cache key, and lookup involving a subject or its books MUST use the composite key
  `(stream, subjectId)`, never `subjectId` alone. This applies to Room DAO queries, in-memory maps,
  navigation args, and any caching layer. Call this out in code comments wherever a composite key
  is used, so future phases don't regress to a single-field lookup.

**UI**
- Material 3
- All user-facing strings in `strings.xml`
- All icon-only interactive composables have a meaningful `contentDescription`; text-bearing controls use visible text and appropriate accessibility semantics without redundant descriptions.
- English-only UI for v1; strings must be externalized so Amharic can be added later

**Scope discipline**
- Do **not** invent extra screens, features, permissions, or libraries beyond what each phase lists

---

## GitHub Hosting Setup (do this first)

1. Create a **public** GitHub repository, e.g. `grade12-textbooks`
2. Add two files in the root (or a `/data` folder):
   - `catalog.json`
   - `version.json`
3. The raw URLs will look like:
```

https://raw.githubusercontent.com/nfrdev/grade12-textbooks/main/catalog.json
https://raw.githubusercontent.com/nfrdev/grade12-textbooks/main/version.json

```
4. Put the base URL (without the filename) into `BuildConfig` as `CATALOG_BASE_URL`.
   - Read the username from `local.properties` / `gradle.properties` (never commit a real username in a public repo).
   - Provide different values for `debug` (a mock/local URL is fine) and `release`.

Example `version.json`:
```json
{
  "latestVersionCode": 1,
  "latestVersionName": "1.0.0",
  "downloadUrl": "https://github.com/nfrdev/grade12-textbooks/releases/download/v1.0.0/Grade12Textbooks.apk",
  "releaseNotes": "Initial release"
}
```

Example catalog.json schema (this is the exact shape — reject anything else):

```json
{
  "schemaVersion": 1,
  "version": 3,
  "generatedAt": "2025-01-01T00:00:00Z",
  "books": [
    {
      "id": "ns-math-g12",
      "title": "Mathematics Grade 12",
      "author": null,
      "stream": "natural_science",
      "subjectId": "mathematics",
      "description": "...",
      "coverUrl": "https://...",
      "pdfUrl": "https://kehulum.com/...pdf",
      "checksumSha256": "…64 hex chars, lowercase…",
      "fileSize": 12345678,
      "language": "en",
      "curriculumYear": "2016 E.C.",
      "addedAt": "2026-09-21T00:00:00Z"
    }
  ]
}
```

Rules for the parser:

· `schemaVersion` must equal 1. Unknown → reject the entire catalog with a clear error.
· Unknown JSON fields are rejected; the schema is intentionally strict.
· `books` must exist and contain at least one valid entry for the catalog to be accepted.
· Every accepted book must have non-empty `id`, `title`, `stream`, `subjectId`, `pdfUrl`, `language`, and `checksumSha256`.
· `pdfUrl` must be HTTPS in release builds. Debug builds may use localhost/mock HTTP for tests only.
· `coverUrl`, when present, must be HTTPS in release builds.
· Reject `file://`, `content://`, `data:`, and other unsupported URL schemes.
· Redirects are allowed only when the final URL remains HTTPS in release builds.
· `checksumSha256` must match `^[a-fA-F0-9]{64}$`. Normalize to lowercase before storage or comparison.
· `fileSize`, when present, must be > 0 and must not exceed the configured maximum accepted book size.
· `addedAt` must be a valid ISO-8601 timestamp in JSON; parse it once and store it as epoch milliseconds in Room/domain.
· `id` must be unique across the whole catalog. `(stream, subjectId)` is **not** unique on its own — both streams legitimately reuse subject ids like `"mathematics"`.
· Invalid entries are skipped and logged with a reason. The catalog is rejected if zero valid books remain.
· Never fabricate a checksum from the URL, file size, or metadata.

---

## Phase 0 — Project Setup

```
Create a new Android app project called "Grade12Textbooks" using Kotlin + Jetpack Compose + MVVM/Clean Architecture.

Follow the "Assistant Operating Rules" and "Non-Negotiable Constraints" sections from the prompt package.

Requirements:
- Package name: com.nfrdev.grade12textbooks
- Min SDK 24, target + compile latest stable SDK
- Hilt, Room, Retrofit + OkHttp + kotlinx.serialization, WorkManager, DataStore, Coil, Timber
- Gradle version catalog (gradle/libs.versions.toml) with all versions pinned
- Standalone APK, no Play Store dependencies

Module / package structure (exact folders):
di/
data/local/ (dao, entity)
data/remote/ (dto, api, CatalogParser)
data/download/
data/repository/
domain/model/
domain/repository/
domain/usecase/
ui/navigation/
ui/home/
ui/stream/
ui/subjects/
ui/books/
ui/downloads/
ui/library/
ui/reader/
ui/bookmarks/
ui/settings/
ui/components/
ui/theme/
util/

Application setup:
- AppApplication class with @HiltAndroidApp
- MainActivity with @AndroidEntryPoint, setContent { AppTheme { AppNavHost() } }
- An empty NavHost skeleton with a single "home" route (do NOT build other screens yet — that's Phase 3)
- A Logger wrapper around Timber (do not call Timber directly from feature code)
- A CrashReporter interface with a NoOpCrashReporter implementation for v1

Permissions:
- Declare only the permissions required by the selected download implementation.
- `POST_NOTIFICATIONS` is declared in the manifest and requested at runtime on API 33+ when needed.

BuildConfig field:
- `CATALOG_BASE_URL` — read from Gradle property `catalog.baseUrl`.
  Debug default: "https://raw.githubusercontent.com/CHANGEME/grade12-textbooks/main/"
  Release: required to be explicitly configured.
  Release configuration must fail if the value is missing, malformed, or not HTTPS.
  Normalize the value to end with `/`; append filenames in code.

Add a hidden debug menu (only in debug builds) with actions:
- "Reset catalog cache" (clears Room catalog tables and metadata; keep unrelated user preferences)
- "Wipe downloads" (deletes files in getExternalFilesDir(null)/books/ and resets Room flags)

Home screen (initial placeholder):
- Two large Material 3 cards: "Natural Science" and "Social Science"
- No navigation wired yet (routes will be added in Phase 3)

Definition of Done:
- App builds and installs (debug)
- Home screen shows the two stream cards
- Hilt, Room, Retrofit, WorkManager, DataStore, Coil, Timber are correctly configured
- Version catalog is populated and used — no inline versions in build.gradle.kts
- Project structure matches the folders above
- Debug menu exists and both actions compile
```

---

## Phase 1a — Domain Models & Room

```
Add the domain layer and local persistence for the catalog. No networking yet.

Domain models (domain/model):
- Stream (enum: NATURAL_SCIENCE, SOCIAL_SCIENCE)
- Subject (id: String, name: String, stream: Stream, iconRes: Int?, bookCount: Int)
- Book (assembled domain model; no `grade` field, everything is Grade 12)
- Bookmark (id, bookId, page, note: String?, createdAt)
- ReadingProgress (bookId, currentPage, totalPages, updatedAt)

Persistence model: keep immutable remote catalog metadata separate from mutable local state.
- `BookEntity`: id, title, author, stream, subjectId, description, coverUrl, pdfUrl, checksumSha256, fileSize, language, curriculumYear, addedAt
- `BookLocalStateEntity`: bookId, localPath, isDownloaded, downloadedAt, lastOpenedAt, isFavorite, isOrphaned
- Domain `Book` combines the two persistence records.

Book fields (domain model):
id, title, author: String?, stream: Stream, subjectId: String,
description: String?, coverUrl: String?, pdfUrl: String,
checksumSha256: String, fileSize: Long?, language: String,
curriculumYear: String?, addedAt: Long,
localPath: String?, isDownloaded: Boolean, downloadedAt: Long?,
lastOpenedAt: Long?, isFavorite: Boolean, isOrphaned: Boolean (default false)

Note: `id` is the globally unique key. `subjectId` is only unique within a given `stream` —
both streams may contain a subject with id "mathematics". Any DAO query that filters or groups
by subject must take both `stream` and `subjectId` as parameters.

Room entities + DAOs (data/local):
- `BookEntity` + `BookDao`
  - Query subjects/books by subject MUST take `(stream, subjectId)`, never subjectId alone
- `BookLocalStateEntity` + `BookLocalStateDao` — one mutable local-state row per `bookId`
- `CatalogMetadataEntity` + `CatalogMetadataDao` — authoritative catalogVersion and lastSyncedAt
- `BookmarkEntity` + `BookmarkDao` (FK to BookEntity, onDelete = CASCADE, index on bookId)
- `ProgressEntity` + `ProgressDao` (FK to BookEntity, onDelete = CASCADE, index on bookId)
- Indices on `BookEntity.stream`, `BookEntity.subjectId`, composite `(stream, subjectId)`, and `addedAt`

DataStore keys (user preferences only):
- wifiOnly: Boolean (default true)
- themeMode: String (SYSTEM / LIGHT / DARK)
- readerScrollMode: String (HORIZONTAL / VERTICAL)
- readerZoom:<bookId>: Float
- readerNightMode:<bookId>: Boolean
- librarySortOrder: String
- disclaimerAcceptedV1: Boolean

Definition of Done:
- All models, entities, DAOs, and DataStore keys exist
- BookDao subject/book lookups take a composite (stream, subjectId) parameter — verified by a
  unit test asserting that two subjects with the same id in different streams return distinct results
- Room schema is exported to schemas/ (enable exportSchema)
- Compiles and unit tests can instantiate DAOs against an in-memory Room DB
```

---

## Phase 1b — Remote Catalog, Fallback Chain & Sample Data

```
Add the network layer, repository, and UseCases for the catalog. Depends on Phase 1a.

Remote:
- CatalogApi (Retrofit) — GET catalog.json and version.json from BuildConfig.CATALOG_BASE_URL
- CatalogDto + VersionDto with kotlinx.serialization
- CatalogParser validates:
    schemaVersion == 1
    every book has a non-empty pdfUrl
    every book has a checksumSha256 matching ^[a-fA-F0-9]{64}$, normalized to lowercase before storage
    every book `id` is unique across the whole catalog (duplicate ids: skip the later duplicate, log it)
  Invalid entries are skipped and logged (not silently dropped).

CatalogRepository behavior (strict order):
  a. Try network. Parse and fully validate the remote catalog before changing local state.
  b. If remote `version` > Room `CatalogMetadataEntity.catalogVersion`, replace the catalog atomically inside one Room transaction and update metadata in that same transaction.
  c. If remote `version` <= cached version, keep the existing Room catalog.
  d. If network fails → serve the last Room catalog.
  e. If Room has no valid catalog → load bundled `assets/catalog.json`, validate it, and write it to Room in one transaction.
  f. The bundled catalog must never overwrite a non-empty newer Room catalog.

Room is the sole source of truth for catalog version and sync timestamp. DataStore is not used for catalog versioning.

Orphan handling:
- If a book in Room is not present in a newly accepted catalog, set `isOrphaned = true`.
- Do NOT delete its file, progress, or bookmarks.
- If that book id later reappears in an accepted catalog, set `isOrphaned = false` and preserve its local file state, progress, and bookmarks.
- Orphans remain visible in v1.

UseCases (all return sealed Result types — never throw):
- GetStreamsUseCase → always returns the two fixed streams
- GetSubjectsUseCase(stream) → derives subjects from cached books, returns Subject list with bookCount
- GetBooksUseCase(stream, subjectId) → list of books, filtered by BOTH parameters
- RefreshCatalogUseCase → triggers the fallback chain above

Sample data:
- Create a deterministic sample `catalog.json` in assets and use the same fixture in tests.
- Must include at least:
    Natural Science: Mathematics, Physics, Chemistry, Biology
    Social Science: Mathematics, Economics, Geography, History
- Do NOT invent or use real-looking third-party textbook URLs. Use local/mock fixtures or `https://kehulum.com/bfile_asset/books_99/collection/grade-12-mathematics-new-curriculum--student-textbook-kehulumcom17599122086bb1.pdf...` until real URLs are supplied.
- Do NOT use placeholder hashes in tests that claim checksum verification. Generate hashes from the actual test files.
- Deliberately include a `"mathematics"` subject in both streams to exercise composite-key handling end to end.
- Include deterministic `addedAt` values so Recently Added tests are reproducible.

Definition of Done:
- Catalog fetches from the GitHub raw URL
- Fallback chain works: network → Room → assets (verified by 3 tests), and version-compare gating
  is tested so a stale bundled fallback never overwrites a newer Room cache
- All UseCases return Result types and are unit-tested
- A test confirms GetSubjectsUseCase / GetBooksUseCase never cross-contaminate the two streams'
  "mathematics" subject
- Bundled assets/catalog.json is present and valid per CatalogParser
- UI can display subjects and books offline (test via debug menu / log, full UI is Phase 3)
- Unit tests cover: parser rejection of bad checksum, parser skip of bad entry, parser skip of
  duplicate id, case-insensitive checksum handling, fallback order
```

---

## Phase 1.5 — Testing & CI

```
Add automated testing infrastructure and CI.

Unit tests (JVM):
- CatalogParser: valid, missing checksum, wrong schemaVersion, empty book list, duplicate id,
  uppercase/mixed-case checksum normalized and accepted
- Catalog fallback chain: network ok → network fail + Room hit → network fail + Room empty → assets
- All UseCases: success and failure paths return the correct Result variants
- Room: in-memory DB, DAO queries, cascade delete on BookEntity, composite (stream, subjectId) lookups

Test doubles:
- FakeCatalogRepository
- FakeDownloader (used in Phase 2 tests)
- FakePreferencesDataStore

Android tests:
- One instrumented Compose UI smoke test: Home renders two stream cards

CI (GitHub Actions, .github/workflows/android.yml):
- Runs on push and PR
- Steps: checkout, setup-java 17, gradle cache, ./gradlew testDebugUnitTest lint assembleDebug
- Does NOT attempt to sign or publish

Definition of Done:
- All unit tests pass locally via ./gradlew testDebugUnitTest
- CI workflow is present and green on the default branch
- Test doubles are available for use in later phases
```

---

## Phase 2a — Single-File Downloader Engine

```
Implement the core downloader for one book. This phase proves HTTP Range handling, storage safety,
checksum verification, atomic file promotion, and deterministic state transitions. Scheduling and
process lifecycle are NOT owned by this phase.

BookDownloader:
- Uses OkHttp.
- Writes only to `{bookId}.pdf.part` inside `getExternalFilesDir(null)/books/`.
- Generates its own destination filename from validated `bookId`; never trusts remote filenames.
- Emits per-book `Flow<DownloadState>`:
    Idle | Queued | InProgress(progress: Float?) | Verifying | Completed | Failed(reason: String) | Paused
- `progress` is nullable. Use null/indeterminate UI when the total size is unknown or unreliable.

HTTP Range behavior:
- If `.part` exists, use its byte count as bytesAlreadyDownloaded.
- Send `Range: bytes=<bytesAlreadyDownloaded>-` when bytesAlreadyDownloaded > 0.
- 206 → append only when the response range is consistent with the requested starting byte.
- 200 → server ignored Range; truncate `.part` and restart from zero.
- 416 → when expected `fileSize` is known and the part size matches it, proceed to checksum verification;
  otherwise treat it as a recoverable state rather than blindly declaring completion.
- Network disconnect/timeouts → preserve `.part` and return a retryable failure.

Integrity:
- Compute SHA-256 of the complete `.part` file.
- Compare with `book.checksumSha256` after lowercase normalization.
- On mismatch: delete `.part` and emit `Failed("Checksum mismatch")`.
- On match: atomically rename `.part` → `{bookId}.pdf`, then persist local download state.
- If `{bookId}.pdf` already exists, verify its checksum before treating it as valid. Delete and redownload if invalid.

Response safety:
- Release builds accept HTTPS only.
- Reject redirects whose final URL is not HTTPS in release builds.
- Do not accept unsupported schemes such as `file:`, `content:`, or `data:`.
- Enforce a maximum accepted download size. If catalog `fileSize` is known, reject a response that grows beyond a configured tolerance above it.
- Never trust `Content-Disposition` for the filesystem filename.

Storage precheck:
- Before a new download, inspect `getExternalFilesDir(null).usableSpace`.
- If `fileSize` is known, require usableSpace >= fileSize + 50 MB.
- If `fileSize` is unknown, require a conservative configured free-space floor.
- Fail before opening the network body when storage is insufficient.

Cancellation / pause:
- Cancellation closes network resources promptly.
- Pause preserves `.part`.
- Cancel deletes `.part`.
- Resume re-enters Range handling.

Concurrency:
- Do NOT add an application-wide Semaphore.
- This phase is safe to invoke from a scheduler, but it does not decide how many downloads may execute.

Delete semantics:
- DeleteBookUseCase deletes the PDF and partial file.
- It removes local download state and deletes progress/bookmarks for that book.
- It keeps the remote catalog entry.
- The confirmation UI must state that progress and bookmarks will also be deleted.

Platform strategy:
- Do not assume `dataSync` foreground service is the permanent API 34+ solution.
- Before Phase 2b implementation, verify the current target-SDK behavior for API 34, 35, and 36.
- Verify current Android Developers guidance for Android 14+ user-initiated data transfer jobs and Android 15 `dataSync` foreground-service limits before implementation.

Definition of Done:
- Download, pause, resume, cancel, and delete work in deterministic single-book tests.
- Checksum success and mismatch paths are tested, including mixed-case checksum input.
- 200/206/416 behavior is tested.
- Existing corrupt final PDFs are detected and repaired.
- Storage precheck works for known and unknown file sizes.
- Cancellation closes resources without corrupting `.part`.
- HTTPS and redirect validation are tested.
```

## Phase 2b — Persistent Download Scheduling & Modern Android Integration

```
Wrap the Phase 2a transfer engine in a persistent scheduler that survives app backgrounding and process
death, while respecting current Android platform rules.

Architecture:
- `DownloadScheduler` is the single abstraction used by DownloadBookUseCase / Pause / Resume / Cancel.
- `BookDownloader` remains the transfer engine and knows nothing about WorkManager or JobScheduler.
- Persist logical download state/intent in Room so the queue can be reconstructed after process death.
- The UI observes one stable per-book DownloadState Flow regardless of the scheduler implementation.

Android 14+ (API 34+):
- For a long-running download explicitly initiated by the user, prefer a user-initiated data transfer (UIDT)
  `JobScheduler` job when the platform requirements permit it.
- UIDT requires the platform-required permission(s) and a notification, and must be scheduled only from an allowed app state. Verify the current Android Developers requirements before implementation.
- If UIDT is selected, document how pause/cancel/resume map to JobScheduler semantics.
- Do not add `FOREGROUND_SERVICE_DATA_SYNC` on API 34+ merely because the API 24–33 fallback uses an FGS.

Android < 34:
- Use WorkManager for persistent scheduling.
- For long-running transfers that exceed ordinary worker execution limits, use WorkManager's supported
  foreground-worker mechanism with the required legacy foreground-service permission/type for the API level.

Android 15+ (API 35+):
- If a `dataSync` FGS fallback exists, implement the required timeout behavior. Android 15 limits `dataSync` FGS runtime for apps targeting API 35+ and provides `Service.onTimeout()` for handling the limit. Verify the exact current limits and lifecycle behavior before implementation.
- Prefer UIDT for API 34+ user-started long downloads when appropriate instead of relying on dataSync FGS.

Android 16+ (API 36+):
- Re-test the chosen scheduler. Background jobs started from an FGS remain subject to their job quotas;
  Android recommends user-initiated data transfer for long-running user-triggered transfers.

Concurrency:
- At most 2 download transfers may execute concurrently.
- Do NOT implement this as a second global Semaphore layered over scheduler concurrency.
- The scheduler layer owns the active-count limit and must not block unrelated WorkManager work.
- For WorkManager, use a dedicated bounded scheduling/execution strategy for download work OR an explicit
  Room-backed dispatcher that starts at most two download workers. Document exactly where the limit is enforced.
- For UIDT, maintain a persistent active set so at most two logical download jobs are scheduled simultaneously.

Identity:
- One logical download per book: `download:<bookId>`.
- Re-enqueueing must not create duplicates.
- Pause stops scheduled execution but preserves `.part`.
- Cancel stops scheduled execution and deletes `.part`.

Network constraints:
- WorkManager fallback respects DataStore `wifiOnly`: UNMETERED if true, CONNECTED if false.
- For UIDT, apply the closest supported network requirement and document any difference.

Recovery / retries:
- Process death resumes from `.part` using Phase 2a Range logic.
- Distinguish pause, cancel, retryable failure, and unexpected termination.
- Transient network failures use bounded exponential backoff.
- Checksum mismatch is not automatically retried without deleting the invalid bytes/file first.

Notifications:
- The active long-running transfer exposes a user-visible progress notification when required by the selected API.
- `POST_NOTIFICATIONS` denial must never crash the app; test on API 33+.
- Provide a stable notification id per book where notifications are used.

Definition of Done:
- Starting a download survives app backgrounding and process death.
- Pause/resume works after process death.
- At most 2 download transfers execute concurrently, proven by a scheduler/instrumentation test.
- No application-wide Semaphore remains in the production download path.
- Wi-Fi-only behavior is verified for the WorkManager fallback.
- API 34+ behavior is verified using the selected UIDT or documented alternative.
- Any API 35+ dataSync fallback handles timeout correctly.
- API 36 is manually tested for the selected path.
- POST_NOTIFICATIONS denial does not crash downloads.
- Duplicate logical downloads are prevented.
```

## Phase 3a — Navigation, Home, Stream, Subjects

```
Build the first half of the Compose navigation graph and its screens.

Routes (exact):
  home
  subjects/{stream}
  books/{stream}/{subjectId}

Nav args:
- Every screen that reads a nav arg (stream, subjectId, bookId in later phases) must retrieve it via
  SavedStateHandle in its ViewModel, not by reaching into NavBackStackEntry directly from the
  composable. This keeps the selected stream/subject/book correct across process death and
  configuration change.
- Screens keyed by subject must always carry BOTH stream and subjectId together — never navigate
  to a subject-scoped screen with subjectId alone.

Screens:

1. HomeScreen
   - Two large Material 3 stream cards (Natural Science / Social Science) — primary entry points; tap navigates directly to `subjects/{stream}`
   - "Continue Reading" card (most recently opened book + progress bar + subject + stream badge)
       Hidden when there is no last-opened book.
   - "Recently Added" horizontal row — sort by `addedAt DESC`, then `id ASC` as a deterministic tiebreaker, and take the first 10. Do not use `downloadedAt`.
   - Pull-to-refresh triggers RefreshCatalogUseCase
   - Empty state: if the catalog is empty (no books at all), show EmptyState with a "Refresh" CTA

2. SubjectsScreen(stream)
   - List of subjects for the selected stream
   - Each row: subject name + book count
   - Tap → books/{stream}/{subjectId} → book list (implemented in Phase 3b), passing both stream
     and subjectId
   - Empty state, loading state, error state

3. BookListScreen(stream, subjectId)  [route: books/{stream}/{subjectId}]
   - Simple list of book cards for that subject, fetched via GetBooksUseCase(stream, subjectId)
   - Tap → book details (Phase 3b)
   - Empty state, loading state, error state

Shared components (ui/components):
- BookCard, StreamCard, SubjectCard
- ProgressBar (small, inline)
- EmptyState, ErrorState, LoadingState

Snackbar contract:
- Every UseCase failure surfaces as a snackbar with the error message
- Use a single app-level SnackbarHostState provided via CompositionLocal

Back-navigation:
- System back follows the nav graph normally
- No manual back-stack rebuilding

Definition of Done:
- Full navigation Home → Subjects(stream) → BookList(stream, subjectId) works
- Nav args are read via SavedStateHandle and survive a process-death + restore test
- BookListScreen for a "mathematics" subjectId shows only the books from the stream it was
  navigated from, never the other stream's "mathematics" books
- All screens have proper empty / loading / error states
- Pull-to-refresh works on Home
- Snackbar shows UseCase errors on all three screens
- Instrumented smoke test: navigate Home → NS → Mathematics → see book list
```

---

## Phase 3b — Book Details, Downloads, Library, Settings

```
Build the second half of the navigation graph and its screens.

Routes (exact):
  book/{bookId}
  downloads
  library
  settings
  reader/{bookId}
  bookmarks/{bookId}

Top-level navigation contract:
- Use one app-level navigation scaffold.
- Bottom navigation is visible only on `home`, `downloads`, `library`, and `settings`.
- Bottom destinations are exactly: Home, Downloads, My Books, Settings.
- Hide bottom navigation on subject lists, book lists, book details, reader, and bookmarks.
- Selecting a bottom destination navigates with normal NavController behavior; do not manually rebuild the back stack.

Screens:

1. BookDetailsScreen(bookId)
   - Read Now navigates to `reader/{bookId}`
   - bookId is globally unique, so this screen needs no stream/subjectId args — read via
     SavedStateHandle as in Phase 3a
   - Cover (Coil), title, author, description, file size (formatted), language badge, curriculum year
   - Download button with live progress from the DownloadState Flow:
       Idle → "Download"
       InProgress/Verifying → progress bar + "Cancel"
       Paused → "Resume"
       Completed → "Read Now" + "Delete"
       Failed → error message + "Retry"
   - Favorite toggle (updates Book.isFavorite)
   - Delete option when downloaded (confirmation dialog)

2. DownloadsScreen
   - Active + queued downloads with progress
   - Per-item actions: pause / resume / cancel
   - Empty state: EmptyState with "Browse Subjects" CTA
   - Live progress via DownloadState Flow

3. LibraryScreen (My Books)
   - Grid of all downloaded books
   - Sort: Last Opened / Title / Subject (persisted in DataStore key librarySortOrder)
   - Filter: All / Natural Science / Social Science
   - Favorite toggle on each card
   - Empty state: EmptyState with "Browse Subjects" CTA

4. SettingsScreen
   - Wi-Fi only downloads toggle (DataStore)
   - Dark mode toggle (SYSTEM / LIGHT / DARK, persisted)
   - Storage usage summary: total size + per-book breakdown (walk books/ folder)
   - "Clear all downloads" with confirmation
   - "Check for updates" button (wired in Phase 5)
   - "Last catalog sync: X" — computed from Room `CatalogMetadataEntity.lastSyncedAt` using locale-aware formatting; do not hardcode relative-time phrases.
   - App version (BuildConfig.VERSION_NAME)

Definition of Done:
- All four screens work and are reachable from the app
- Download state is live on BookDetails and Downloads
- Delete removes the file and resets the Room flags; progress and bookmarks cascade-delete
- Sorting persists across app restarts
- Storage summary reflects reality after downloads and deletes
```

---

## Phase 4 — PDF Reader

```
Routes added in this phase:
- `reader/{bookId}`
- `bookmarks/{bookId}`

Both routes read `bookId` through SavedStateHandle in their ViewModels. `bookId` is globally unique.

Implement the Reader using Android's PdfRenderer.

Justification: PdfRenderer is built into the framework — zero extra dependencies, smallest APK,
and sufficient for page-by-page rendering. Note: it does not support encrypted/password-protected
PDFs. If PdfRenderer throws on a file, show an error state with an "Open in external viewer"
button that launches ACTION_VIEW with a `content://` URI from an app-owned AndroidX FileProvider.
Configure the provider with a narrow path rule that exposes only downloaded PDFs. Do NOT add a PDF
text-extraction library in v1.

Features:
- Page-by-page horizontal swipe OR vertical scroll, chosen by DataStore key readerScrollMode
- Pinch-to-zoom (persisted per book via readerZoom:<bookId>)
- Night mode toggle (invert colors via ColorMatrix) — persisted per book via readerNightMode:<bookId>
- Page jump input (go to page X) — clamp to [1, totalPages]
- Current page + total pages indicator always visible
- Auto-save progress to ProgressEntity on page change, debounced 500ms (cancel the pending save on the next page change)
- Bookmark button on current page → dialog with optional note → saves to BookmarkEntity
- BookmarksScreen: list of bookmarks for the current book; tap jumps to that page; swipe to delete
- On Reader open: update Book.lastOpenedAt = now
- Memory management:
    Render bitmaps at a size capped by the current view dimensions.
    Prefer a low-memory bitmap configuration compatible with PdfRenderer and visually acceptable for textbook pages;
    do not hard-require RGB_565 if it causes rendering problems.
    Cache at most 3 pages in memory: current page, plus one page on each side (current - 1, current, current + 1).
    On navigating to a new page, evict the page furthest from the current page first before rendering the new page.

Definition of Done:
- Can open any downloaded PDF
- Progress is saved on page change and restored on next open
- Bookmarks work with and without notes
- Night mode and zoom level persist per book
- Scroll mode setting is respected
- Cache eviction order (furthest-page-first, max 3 resident bitmaps) is verified by a unit test
  that simulates rapid forward/backward navigation
- No OOM on a 500+ page PDF (verified manually)
- Encrypted / malformed PDFs show the fallback "Open in external viewer" state
```

---

## Phase 4.5 — Reader & Integration Tests

```
Add tests for the reader and cross-phase integration.

Unit tests:
- Progress debounce: 5 rapid page changes result in 1 write
- Page-jump clamping to [1, totalPages]
- Zoom persistence key naming
- Bitmap cache: eviction order under rapid navigation, max 3 resident bitmaps enforced

Instrumented tests:
- Bookmark add → appears in BookmarksScreen → tap jumps to correct page
- Delete a downloaded book → its progress and bookmarks are gone (query Room after)
- End-to-end: download a small test PDF from a local asset → open reader → change page → reopen app → progress restored

Definition of Done:
- All new tests pass
- CI stays green
```

---

## Phase 5 — APK Distribution, GitHub Updates & Polish

```
Ship the standalone APK and its update mechanism.

1. UpdateChecker
   - On app start (non-blocking, launched from Application.onCreate on Dispatchers.IO):
       fetch version.json from BuildConfig.CATALOG_BASE_URL + "version.json"
   - If latestVersionCode > BuildConfig.VERSION_CODE → expose a dismissible banner/dialog with release notes
       and a button that opens a validated HTTPS `downloadUrl` in the system browser (`ACTION_VIEW`).
   - Validate `downloadUrl` before opening it: HTTPS only in release builds; reject arbitrary schemes.
   - If latestVersionCode <= current → do nothing
   - Silently fail if offline or the fetch errors (log via Logger)
   - Also expose a manual "Check for updates" from Settings

2. Catalog sync timestamp
   - Surface "Last synced: <locale-aware relative time>" in Settings
   - Do not hardcode strings like "minutes ago" — use DateUtils.getRelativeTimeSpanString

3. Signing (documentation only — do NOT generate a real keystore in the project)
   - Document in README:
       How to create a keystore with keytool
       How to configure signingConfigs in build.gradle.kts (reading from local.properties)
       That the SAME keystore must be used for every future APK, or Android will refuse to install updates
       That losing the keystore permanently breaks update installs

4. Release build config
   - Enable R8 / ProGuard + resource shrinking
   - Keep rules for Hilt, Room, Retrofit, OkHttp, WorkManager, kotlinx.serialization, Coil, Timber
   - Verify: assembleRelease builds a working minified APK

5. First-launch disclaimer
   - One-time dialog (text below), gated by DataStore key disclaimerAcceptedV1
   - If the text materially changes in a future version, bump to disclaimerAcceptedV2
   - Text:
     "This app provides convenient offline access to publicly available Grade 12 textbooks
      for personal educational use. PDFs are sourced from third-party sites. Redistribution
      of the APK or the books themselves may be restricted by the original publishers."

6. README.md must document:
   - How to build a signed release APK
   - How to update catalog.json and version.json on the GitHub repo
   - How to create a new GitHub Release and attach the APK
   - The signing key requirement (and the "do not lose it" warning)
   - How to change the GitHub username / repo name via local.properties (catalog.baseUrl)
   - Release checklist: bump versionCode/versionName → assembleRelease → create GitHub Release →
     update version.json → verify update banner appears on the old build

Definition of Done:
- Update banner appears when version.json reports a newer versionCode (test by pointing a debug build at a local mock)
- Manual "Check for updates" from Settings works
- Release APK is minified, signed, and installs cleanly over a previous signed build with the same key
- First-launch disclaimer shows once and does not reappear after restart
- README is complete and accurate
```

---

## PDF Source Notes (for the human, not the AI)

Source site used by the human operator: https://kehulum.com/textbook

**Important:** Before using any real third-party URLs, verify the site's current terms, rights-holder restrictions,
and whether linking, downloading, or private sharing is permitted for the intended use. This package does not treat
a disclaimer or a small audience as legal authorization.

Direct PDF URLs look like:

```
https://kehulum.com/bfile_asset/books_99/collection/{slug}-kehulumcom{hash}.pdf
```

After verifying permission to use the material, obtain the actual PDF URLs, download each file once, compute SHA-256 from the exact downloaded bytes, and put those verified values into `catalog.json` as lowercase hex. Do not let the AI invent URLs or hashes.

Recommended subjects for v1 (keep it small):

· Natural Science: Mathematics, Physics, Chemistry, Biology
· Social Science: Mathematics, Economics, Geography, History

After you have the real catalog.json, push it to your GitHub data repo. The app will pick it up automatically on next refresh.

**Legal / rights note:** Do not assume third-party textbook PDFs are redistributable merely because they are publicly accessible. Before distributing the app or catalog to anyone beyond your own testing, verify current site terms, publisher/rights-holder permissions, and applicable copyright rules for the intended jurisdiction and use. Do not host copies of the PDFs on your own infrastructure or present the app as an authorized distributor unless you have the necessary rights. A disclaimer does not grant those rights.

---

## How to Use This Package

1. Create the GitHub data repository and push a minimal valid catalog.json + version.json first.
2. Set your real GitHub username in local.properties as catalog.baseUrl (do not commit it).
3. Feed Phase 0 → test → Phase 1a → test → Phase 1b → ... one phase at a time.
4. Do not skip the test phases (1.5, 4.5). They exist to catch drift early.
5. After Phase 1b works and rights have been verified, replace the sample catalog with real, verified entries + correct SHA-256 hashes and update the data repository.
6. Never let the AI skip ahead or invent extra features — the Assistant Operating Rules block this, but stay vigilant.

---

## V4 Global Quality Gate

Before considering the project complete, the AI must verify all of the following:

```
1. Build correctness
   - Debug build succeeds.
   - Release build succeeds.
   - Dependency versions are controlled by the version catalog.

2. Data correctness
   - Room is the single source of truth for catalog version and sync timestamp.
   - Every navigation route used by a screen is explicitly defined in the phase that introduces it.
   - Remote catalog replacement is atomic.
   - No `(stream, subjectId)` lookup uses subjectId alone.
   - `addedAt` drives Recently Added.

3. Download correctness
   - SHA-256 is calculated from actual downloaded bytes.
   - Existing corrupt PDFs are detected.
   - Range 200/206/416 behavior is tested.
   - Partial files survive pause and process death, but are deleted on explicit cancel.
   - At most two downloads execute concurrently.

4. Android compatibility
   - API 24 fallback works.
   - API 33 notification denial works.
   - API 34+ selected transfer path is tested.
   - API 35+ dataSync timeout behavior is handled if dataSync exists.
   - API 36 selected scheduler path is manually re-tested.

5. Security
   - User-visible failures are mapped to localized resource strings; raw exceptions are not shown.
   - Release catalog/PDF endpoints are HTTPS.
   - Unsupported schemes and unsafe redirects are rejected.
   - Remote filenames are never used as filesystem paths.
   - No secrets or signing credentials enter source control.

6. Release hygiene
   - No fake textbook URLs or fake checksums remain in production catalog data.
   - README documents how the real catalog is produced and verified.
   - Legal/rights assumptions are documented as assumptions requiring human verification.
```

**Platform references used for this v4 design:** consult the current official Android Developers documentation for user-initiated data transfer jobs, foreground-service changes, Android 15 foreground-service timeout behavior, and JobScheduler `setUserInitiated()` before implementing Phase 2.

---

End of prompt package (v4)
