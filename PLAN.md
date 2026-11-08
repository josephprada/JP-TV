# JP-TV — Build Plan

A custom Android TV launcher: glassmorphism UI, live wallpapers (galaxy, embers, video loop), a left app rail, a network panel, quick access to native Android settings, and a theme engine where each theme changes the whole layout, not just colors.

- **Design source of truth:** [`design/`](design/) (Claude Design canvas, `.dc.html` artboards) and its link: https://claude.ai/artifact/WNop8Y5u8cVwiE6KDbR1wr
- **Target devices:** a generic Amlogic box "V_BOX14" (Android 9 / API 28, 2 GB RAM, 1280×720 at 213 dpi) first, then a generic Google TV projector.
- **Goal for every decision:** smooth on 2 GB devices, and fast for an AI agent to read, change, build and verify.

---

## 1. Tech stack

| Area | Choice | Why |
| --- | --- | --- |
| Language | Kotlin 2.x | Standard for Android, concise, good for AI edits. |
| UI | Jetpack Compose + `androidx.tv:tv-material` | Declarative UI; TV focus and D-pad support built in. |
| Min / target SDK | min 24, target 35 | Covers the Android 9 box and older projectors. |
| Video wallpaper | Media3 ExoPlayer (`SurfaceView`, muted, looped) | Hardware decoding; cheapest way to play a loop. |
| Generative wallpapers | Compose `Canvas` + `withFrameNanos` | No GL dependency; the galaxy is ported from JP-Wallet's `galaxyEngine.ts`. |
| Persistence | Jetpack DataStore (Preferences) | Small typed config: theme, accent, font, brand name, app order, hidden apps. |
| DI | Manual (`AppContainer`) | No Hilt/KAPT: faster builds, nothing hidden, easy to follow. |
| Async | Coroutines + Flow | Network status, package changes and prefs as streams. |
| Build | Gradle Kotlin DSL + version catalog (`gradle/libs.versions.toml`) | One file to bump versions. |
| Tests | JUnit 5, Turbine, kotlinx-coroutines-test; Roborazzi for screenshots (phase 7) | Domain and ViewModel logic test-first. |
| CI | GitHub Actions: build, test, upload debug APK | Every push yields an installable APK. |

Fonts are bundled under `res/font` (Anton, Geist, Geist Mono, plus the ones each theme needs; all OFL). Nothing loads from the network at runtime.

## 2. Architecture

A single `:app` module, organized as **screaming architecture** (folders are named after what the launcher does) with **hexagonal** boundaries inside each feature. One module keeps Gradle fast on a weak machine and keeps the whole map visible to an agent.

```
app/src/main/java/co/jp/tv/
├── JpTvApp.kt                 # Application: builds AppContainer
├── AppContainer.kt            # manual DI: wires ports to adapters
├── MainActivity.kt            # HOME activity; hosts the active theme
│
├── apps/                      # installed apps: list, launch, order, hide
│   ├── domain/                # TvApp, AppCatalog (port), AppLayout rules
│   ├── data/                  # PackageManagerAppCatalog (adapter), package-change receiver
│   └── ui/                    # EditAppsScreen
├── network/                   # Wi-Fi/Ethernet status, latency
│   ├── domain/                # NetworkStatus, NetworkMonitor (port)
│   ├── data/                  # ConnectivityNetworkMonitor, PingProbe
│   └── ui/                    # NetworkChip, NetworkPanel
├── settings/                  # quick tiles to native Android settings
│   ├── domain/                # SettingsShortcut list (intent actions)
│   └── ui/                    # QuickSettingsScreen
├── preferences/               # LauncherConfig (brand, accent, font, theme, wallpaper)
│   ├── domain/                # LauncherConfig, ConfigStore (port)
│   └── data/                  # DataStoreConfigStore
├── wallpaper/                 # live wallpapers
│   ├── Wallpaper.kt           # contract: @Composable render(accent, paused)
│   ├── galaxy/                # GalaxyWallpaper (JP-Wallet port)
│   ├── embers/                # EmbersWallpaper ("Brasas")
│   └── video/                 # VideoWallpaper (ExoPlayer loop) + StaticImageWallpaper
├── themes/                    # theme engine
│   ├── LauncherTheme.kt       # contract (below)
│   ├── ThemeRegistry.kt       # the list of themes; the one place to register a theme
│   ├── jptv/                  # default: glass rail, galaxy, customizable accent/font
│   ├── pixel/  terminal/  cartelera/  zen/  hud/  vinilo/  lofi/
│   └── shared/                # HomeState, HomeActions, focus helpers
├── home/                      # HomeViewModel: merges apps + network + clock + config into HomeState
└── core/
    ├── design/                # JP tokens, glass modifiers, typography, motion curves
    └── util/                  # clock ticker, intents, low-end device detection
```

**Dependency rule:** `ui → domain ← data`. Domain has no Android imports (pure Kotlin), so it is unit-testable on the JVM. Adapters live in `data/`. `AppContainer` is the only place that knows the concrete classes.

### The theme contract

Every theme receives the same state and actions and draws its own home screen. That is why Pixel can use a carousel, HUD a wheel and Zen a hidden menu.

```kotlin
interface LauncherTheme {
    val id: String                 // "jptv", "pixel", ...
    val displayName: String
    val supportsAccent: Boolean    // the JP-TV theme does; fixed-palette themes don't
    @Composable fun Home(state: HomeState, actions: HomeActions)
}

data class HomeState(
    val brand: String,
    val apps: List<TvApp>,         // visible apps, already ordered
    val focusedIndex: Int,
    val network: NetworkStatus,
    val now: LocalDateTime,
    val config: LauncherConfig,
)

interface HomeActions {
    fun launch(app: TvApp)
    fun focus(index: Int)
    fun openNetwork()
    fun openSettings()
    fun openThemes()
}
```

Adding a theme = one folder under `themes/` + one line in `ThemeRegistry`. Nothing else changes.

### Performance budget (2 GB box, 720p)

- The wallpaper pauses whenever an app is in the foreground (`Lifecycle.ON_STOP`) or a full-screen panel is open.
- Galaxy: about 230 stars, up to 4 meteors, drawn in a single `Canvas` with no allocation per frame (precomputed arrays). Frame-rate cap option: 60 / 30 fps.
- Glass blur: real `RenderEffect` blur exists only on API 31+. Android 9 gets a "fake glass" (semi-transparent fill, inner highlight, border), chosen automatically.
- Video wallpaper: 720p H.264, muted, one decoder, released on stop.
- "Low-end mode" (auto when RAM ≤ 2 GB): fewer stars, no orb blur, 30 fps.
- Cold start target: under 1.5 s to the first frame on the box.

## 3. Native integrations

| Feature | How |
| --- | --- |
| Be the HOME screen | `intent-filter` with `MAIN` + `HOME` + `DEFAULT` + `LEANBACK_LAUNCHER`; set as default with `adb shell cmd package set-home-activity co.jp.tv/.MainActivity`. |
| List and launch apps | `PackageManager.queryIntentActivities` for `LEANBACK_LAUNCHER` and `LAUNCHER`; banner, falling back to the icon; `startActivity(getLeanbackLaunchIntentForPackage)`. |
| React to installs | `LauncherApps.Callback` (or a `PACKAGE_ADDED/REMOVED` receiver). |
| Network panel | `ConnectivityManager.NetworkCallback`, `WifiManager.connectionInfo` (SSID needs the location permission on 8.1+; fall back to "Wi-Fi"), `LinkProperties` for IP, gateway and DNS, latency via a TCP connect probe. |
| Quick settings tiles | `Settings.ACTION_WIFI_SETTINGS`, `ACTION_BLUETOOTH_SETTINGS`, `ACTION_DISPLAY_SETTINGS`, `ACTION_SOUND_SETTINGS`, `ACTION_APPLICATION_SETTINGS`, `ACTION_INTERNAL_STORAGE_SETTINGS`, `ACTION_DATE_SETTINGS`, `ACTION_SETTINGS`, each with a fallback to `ACTION_SETTINGS`. |
| Video wallpaper file | `ACTION_OPEN_DOCUMENT` when available, else a built-in file list of `/sdcard/JPTV/`. |

## 4. Roadmap

Each phase ends with an APK installed on the box and a screenshot that matches the design.

| Phase | Deliverable |
| --- | --- |
| 0. Setup | JDK 17 + Android cmdline-tools installed in user space (no sudo), Gradle wrapper, CI, empty HOME activity on the box. |
| 1. MVP launcher | Left rail with real apps, D-pad focus, launch, clock, set as HOME. The JP-TV theme without effects. |
| 2. Look and feel | Glass components, JP tokens, galaxy wallpaper (JP-Wallet port), embers, staggered entrance, breathing focus. |
| 3. Network + settings | Network chip and panel with live data; quick settings tiles. |
| 4. Edit apps | Reorder, hide and show, persisted in DataStore. |
| 5. Customize | Brand name, accent (6 presets plus custom), title font, wallpaper (Galaxia, Brasas, my video, still image). |
| 6. Themes | Pixel, Terminal, Cartelera, Zen, HUD, Vinilo, Lo-fi, one per PR, each matching its artboard. |
| 7. Polish | Low-end mode, screenshot tests, cold-start profiling, baseline profile. |
| 8. Projector | Test on the second device; adjust density and safe areas; release build signed. |

## 5. Workflow (optimized for an AI agent)

- `make` targets wrap everything: `make build`, `make test`, `make deploy` (build, install, set as HOME), `make shot` (pull a screenshot from the TV into `shots/`), `make logs`.
- **Verification loop:** change → `make deploy` → `make shot` → compare with the artboard in `design/`. No guessing about how it looks on the TV.
- `CLAUDE.md` holds the map, the commands, the conventions and recipes ("add a theme", "add a wallpaper", "add a setting").
- Small files with one responsibility each; theme and wallpaper code never reaches into data adapters, only into `HomeState`.
- Conventional commits; one phase or theme per PR; CI must pass.
- The device IP lives in `local.properties` (`tv.host=192.168.1.4`), never hard-coded.

## 6. Risks and decisions to watch

- **Android 9 has no real blur.** The fake-glass fallback is designed in; the look on API 31+ will be richer.
- **The V_BOX14 firmware is untrusted** (preinstalled suspicious packages, root ADB). JP-TV stores no accounts or personal data, and the app needs no dangerous permissions beyond optional location for the SSID.
- **Google TV projectors may block third-party HOME screens.** Plan: disable the stock launcher over ADB, as already done on the box.
- **App banners vary in quality.** Fall back to the icon on a tinted tile.
