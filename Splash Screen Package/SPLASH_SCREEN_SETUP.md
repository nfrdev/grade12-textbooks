# Splash Screen Setup — Grade 12 Textbooks

Uses `androidx.core:core-splashscreen`, the correct approach since min SDK is 24
(the native platform SplashScreen API is API 31+ only; this library backports the
same behavior down to 24, and defers to the real platform API automatically on 31+).

## 1. Dependency (`gradle/libs.versions.toml` + module `build.gradle.kts`)

```toml
# libs.versions.toml
[versions]
coreSplashscreen = "1.0.1"

[libraries]
androidx-core-splashscreen = { group = "androidx.core", name = "core-splashscreen", version.ref = "coreSplashscreen" }
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(libs.androidx.core.splashscreen)
}
```

## 2. Assets

Copy `drawable/splash_icon.png` (attached) into `app/src/main/res/drawable/`.

This is a **flat, single-layer, transparent-background** mark by design — the
platform splash spec wants a simple icon, not the full gradient launcher icon.
It reuses the same book+cap silhouette so the transition from splash → launcher
→ in-app UI feels like one continuous brand, just simplified for the moment
it's on screen.

## 3. Theme

`res/values/themes.xml` — add a splash theme:

```xml
<resources>
    <style name="Theme.App.Starting" parent="Theme.SplashScreen">
        <item name="windowSplashScreenBackground">#0B3D5C</item>
        <item name="windowSplashScreenAnimatedIcon">@drawable/splash_icon</item>
        <!-- icon is static art, not an AnimatedVectorDrawable, so no icon animation duration needed -->
        <item name="postSplashScreenTheme">@style/Theme.App</item>
    </style>
</resources>
```

`AndroidManifest.xml` — point the launch activity at the splash theme instead
of the app's normal theme:

```xml
<activity
    android:name=".MainActivity"
    android:theme="@style/Theme.App.Starting"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

## 4. MainActivity

Call `installSplashScreen()` **before** `super.onCreate()` and before
`setContent { }`:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Optional: keep the splash on screen until the first catalog read
        // (Room cache or bundled fallback) resolves, so Home never flashes
        // an empty state right after splash.
        var isReady = false
        splashScreen.setKeepOnScreenCondition { !isReady }

        setContent {
            AppTheme {
                AppNavHost(onFirstFrameReady = { isReady = true })
            }
        }
    }
}
```

Only use `setKeepOnScreenCondition` for something genuinely fast (catalog
read from Room/assets is milliseconds). Do **not** hold the splash open for
the network catalog refresh — that can take seconds on a bad connection and
would make the app feel frozen. The network refresh should happen after Home
is already visible, with its own loading state.

## Why not a custom Compose splash screen instead?

You could build a full-screen Composable that shows first and then navigates
to Home. The platform API is preferred here because:
- No flash-of-blank-window between process start and your first frame
- Handles the app-cold-start icon zoom/fade transition for free
- Matches OS-level conventions users already expect on Android 12+
- Less code, no manual timer/navigation logic to get wrong

## Definition of Done
- Cold start shows the dark blue background + gold book/cap icon before Home appears
- No blank white/black frame between process start and splash appearing
- Splash dismisses as soon as the first catalog read resolves (not tied to network)
- Works identically in behavior from API 24 through the latest SDK (compat vs. native path)
