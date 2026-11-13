# CLAUDE.md — working on JP-TV

Read [PLAN.md](PLAN.md) for the why. This file is the how: map, commands, conventions, recipes.

## Map

- `app/src/main/java/co/jp/tv/` — all code, by feature: `apps/`, `network/`, `settings/`, `preferences/`, `wallpaper/`, `themes/`, `home/`, `core/`.
- Inside a feature: `domain/` (pure Kotlin, no Android imports), `data/` (Android adapters), `ui/` (Compose).
- `playlist/` — wallpaper media: scan of `/sdcard/JPTV/{fondos,protector}` (+ legacy `loop.mp4`/`fondo.jpg`, + USB `JPTV/`), `PlaylistScheduler` rotation rules, playlist cursor. `screensaver/` — modes, idle rules, `ScreensaverHost`, `JpScreensaverService` (DreamService).
- `AppContainer.kt` — the only file that creates adapters. New dependency? Add it here.
- `themes/ThemeRegistry.kt` — the only list of themes.
- `tools/sounds/synth.py` — generates every UI sound clip in `res/raw`.
- `design/` — the approved design (`.dc.html` artboards). Treat it as the spec for layout, sizes, colors and motion.

## Commands

| Command | Does |
| --- | --- |
| `make build` | `./gradlew :app:assembleDebug` |
| `make test` | JVM unit tests |
| `make deploy` | build, `adb install -r`, set as HOME, press HOME |
| `make shot` | screenshot from the TV to `shots/<timestamp>.png` |
| `make logs` | `adb logcat` filtered to `co.jp.tv` |
| `make screens` | off-device renders to `screens/` (app) and `screens/design/` (artboards); no TV needed |

The TV address comes from `local.properties` (`tv.host=…`). If the TV is unreachable, say so; don't guess an IP.

## Conventions

- Kotlin official style. One public composable or class per file, file named after it.
- Domain logic is written test-first (JUnit 5 + Turbine) when it has rules: ordering, hiding, latency parsing, config migration.
- UI reads only `HomeState` and calls only `HomeActions`. A theme never touches `PackageManager`, DataStore or `ConnectivityManager`.
- Colors, radii, spacing and motion come from `core/design` tokens. No literal hex in themes except their own fixed palette in `<Theme>Palette.kt`.
- Motion: entrance curve `CubicBezierEasing(0.16f, 1f, 0.3f, 1f)`. Every infinite animation stops when `paused == true`.
- Strings shown on the TV are Spanish (`res/values/strings.xml`); code, comments and identifiers are English.
- Commits: conventional commits (`feat(themes): add vinilo home`). No AI attribution lines.

## Recipes

**Add a theme**
1. Create `themes/<id>/<Id>Theme.kt` implementing `LauncherTheme` and `themes/<id>/<Id>Palette.kt`.
2. Register it in `ThemeRegistry`.
3. Optionally override `Ambient(state)` (the scene without app UI, used by the screensaver's "Ambiente del tema"); the default shows the wallpaper.
4. Match `design/project/Theme<Id>.dc.html`; verify with `make deploy && make shot`.

**Add a live wallpaper**
1. Create `wallpaper/<id>/<Id>Wallpaper.kt` implementing `Wallpaper` (`render(accent, paused)`).
2. Precompute particles once; never allocate in the draw lambda.
3. Add it to the wallpaper list in `preferences/domain/LauncherConfig.kt`.

**Add a quick-settings tile**
1. Add an entry to `settings/domain/SettingsShortcuts.kt` with the intent action and a fallback.

**Add or tune UI sounds**
1. Each theme has four clips (`move`, `select`, `back`, `control`) in `app/src/main/res/raw/snd_<theme>_<sound>.wav`: mono, 16-bit, 22050 Hz, at most 150 ms.
2. They are original and procedural. Edit that theme's function in `tools/sounds/synth.py` (pure Python, no dependencies), then run `python3 tools/sounds/synth.py` (all themes) or `python3 tools/sounds/synth.py zen hud` and commit the WAVs. The output is deterministic.
3. A theme plays the set named by `LauncherTheme.soundSet` (defaults to its `id`). `SoundAssetsTest` checks that every registered theme has its four clips.
4. Playback goes through the `UiSounds` port (`core/sound`) with the SoundPool adapter in `core/sound/data`, created in `AppContainer`. Home moves, launches and back are played centrally by `HomeViewModel` and `MainActivity`; themes only provide the set. Header controls play the control sound through `ThemeControlFocus`.

## Off-device loop (no TV)

`make screens` renders with Roborazzi + Robolectric (tests in `app/src/test/.../screens/`, fixtures in `ScreenFixtures.kt`) at 1280x720 / 213 dpi with a frozen clock and paused animations: `home-<theme>.png`, `themes`, `customize-<wallpaper>`, `customize-focus-*` (D-pad presses), `settings`, `network`, `edit`. `tools/design-shots/render.sh` renders `design/project/*.dc.html` headless (Chromium + a small shim, since the canvas runtime is not in the repo) to `screens/design/<Board>.png` at 1280x720 (`@1920` = native). `python3 tools/design-shots/compare.py` writes side-by-side sheets to `screens/compare/`. Loop: edit, `make screens-app`, open the PNG next to the artboard. Limits: no real video, app icons or blur; confirm those on the TV.

## Device safety (read before touching any TV)

- NEVER `pm disable`/`pm disable-user` a stock launcher or `FallbackHome` on any device. On the HY320 projector (192.168.1.14, Allwinner H713, Android 13-ish) this caused a reboot loop: the OEM keystone service `com.softwinner.tcorrection` crashes in system_server when the launcher list is empty, and Wi-Fi/ADB never come up long enough to undo it.
- To use JP-TV as home, only mark it preferred (`cmd package set-home-activity`) and leave every stock launcher enabled. If the firmware ignores the preference, accept it; do not force it.
- On the projector the HOME key is hard-wired: the system starts `com.jm.jmlauncher/.Launcher` (system app, category `HOME.JM`), which starts `com.htc.htclauncherhighenglishd08/com.mao.launcher.activity.BarrelActivity`. `set-home-activity` is ignored there.
- JP-TV takes over WITHOUT touching the system through the opt-in `homeoverride/` feature (default OFF). Two detection backends; the stock launchers stay enabled as the fallback and nothing is ever disabled:
  - Accessibility (`HomeOverrideService`, no window content): Home key and stock window events. It does NOT work on the projector: the system-uid OEM app `com.master.accessibility` (`/system/app/NetflixMouse`, the remote's mouse pointer) rewrites `enabled_accessibility_services` to null about 1 s after anything else is enabled. Do not disable or touch that app.
  - Usage data (`ForegroundWatcherService`, the one used on the projector): a foreground service (`specialUse`, silent channel "JP-TV inicio") that, only while the screen is on, polls `UsageStatsManager.queryEvents` every 450 ms over the last ~2 s (a reused `UsageEvents.Event`, no content, one IPC per poll) and keeps the latest ACTIVITY_RESUMED package. When it is a stock home (known list + MAIN/HOME and HOME.JM resolvers; never JP-TV, Settings, SystemUI) it asks `RedirectPolicy` and starts JP-TV with `HomeStarter`. It idles with the screen off or while the accessibility backend is bound, and stops when the toggle is off or an access is missing. `SYSTEM_ALERT_WINDOW` is what lets it start an activity from the background (Android 10+).
  - `BootReceiver` (BOOT_COMPLETED and MY_PACKAGE_REPLACED, nothing direct-boot aware) starts the watcher; on boot it also starts JP-TV 2 s later. MainActivity resume and the settings toggle re-sync it.
- Guards (`RedirectPolicy`, unit-tested, shared by both backends): the override stands down when the toggle is off, after 3 crashes or non-drawn redirects in 10 min, and for 10 min after "Abrir launcher original" in Settings. Stock windows are debounced 1.5 s and ignored for 15 s after boot.
- Enable on the projector (manual, once): Ajustes de Android > Apps > Acceso especial > "Acceso a datos de uso" > JP-TV ON, and "Mostrar sobre otras apps" > JP-TV ON (JP-TV Ajustes has a button for each); then JP-TV > Ajustes > "Usar JP-TV como inicio" > ACTIVAR. The row shows ACTIVO and "DATOS DE USO" when both accesses are on. Escape hatch: DESACTIVAR, or turn either access off. Never `pm disable` anything to get this working.

- HY320 home, the working method: the HOME key always starts the system app JmLauncher, which opens the package in the property `persist.sys.home.selectpkg` (default `com.htc.htclauncherhighenglishd08`). With `adb shell setprop persist.sys.home.selectpkg co.jp.tv`, Home and boot open JP-TV while every stock launcher stays enabled. Undo: set it back to `com.htc.htclauncherhighenglishd08`. The user runs this himself, because it is a system setting. Uninstalling JP-TV while the property points to it leaves Home on a black screen; reset the property over ADB to recover. There is no boot loop in that case.
- HY320 auto-start on power-on: at boot JmLauncher opens `getLaunchIntentForPackage(persist.sys.boot_app)` (that is why MainActivity also has a plain MAIN/LAUNCHER filter). `adb shell setprop persist.sys.boot_app co.jp.tv` makes JP-TV open by itself after boot. Undo with `setprop persist.sys.boot_app '""'`. The user runs it himself. Before uninstalling JP-TV, clear both properties.

## Verify before saying "done"

`make test` passes, `make deploy` succeeds, and a `make shot` screenshot matches the design. Report what you checked and what you couldn't.
