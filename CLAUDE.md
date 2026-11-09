# CLAUDE.md — working on JP-TV

Read [PLAN.md](PLAN.md) for the why. This file is the how: map, commands, conventions, recipes.

## Map

- `app/src/main/java/co/jp/tv/` — all code, by feature: `apps/`, `network/`, `settings/`, `preferences/`, `wallpaper/`, `themes/`, `home/`, `core/`.
- Inside a feature: `domain/` (pure Kotlin, no Android imports), `data/` (Android adapters), `ui/` (Compose).
- `AppContainer.kt` — the only file that creates adapters. New dependency? Add it here.
- `themes/ThemeRegistry.kt` — the only list of themes.
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
3. Match `design/project/Theme<Id>.dc.html`; verify with `make deploy && make shot`.

**Add a live wallpaper**
1. Create `wallpaper/<id>/<Id>Wallpaper.kt` implementing `Wallpaper` (`render(accent, paused)`).
2. Precompute particles once; never allocate in the draw lambda.
3. Add it to the wallpaper list in `preferences/domain/LauncherConfig.kt`.

**Add a quick-settings tile**
1. Add an entry to `settings/domain/SettingsShortcuts.kt` with the intent action and a fallback.

## Off-device loop (no TV)

`make screens` renders with Roborazzi + Robolectric (tests in `app/src/test/.../screens/`, fixtures in `ScreenFixtures.kt`) at 1280x720 / 213 dpi with a frozen clock and paused animations: `home-<theme>.png`, `themes`, `customize-<wallpaper>`, `customize-focus-*` (D-pad presses), `settings`, `network`, `edit`. `tools/design-shots/render.sh` renders `design/project/*.dc.html` headless (Chromium + a small shim, since the canvas runtime is not in the repo) to `screens/design/<Board>.png` at 1280x720 (`@1920` = native). `python3 tools/design-shots/compare.py` writes side-by-side sheets to `screens/compare/`. Loop: edit, `make screens-app`, open the PNG next to the artboard. Limits: no real video, app icons or blur; confirm those on the TV.

## Verify before saying "done"

`make test` passes, `make deploy` succeeds, and a `make shot` screenshot matches the design. Report what you checked and what you couldn't.
