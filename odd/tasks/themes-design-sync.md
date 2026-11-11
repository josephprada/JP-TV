# themes-design-sync

Objective: bring every home theme in line with the updated Claude Design project "JPTV Launcher", add the four themes that exist only in the design, apply the day/night cycle where the design defines it, and let the user choose a 24-hour or 12-hour (AM/PM) clock.
Branch: `feat/themes-design-sync`. Delivery strategy: `ask-on-risk`. Forecast: well over 400 authored lines (11 themes), so the branch will need slicing before any PR.
Test runner: `ANDROID_HOME="$LOCALAPPDATA/Android/Sdk" ./gradlew :app:testDebugUnitTest` (JUnit 5 + Turbine). Compile check: `ANDROID_HOME="$LOCALAPPDATA/Android/Sdk" ./gradlew :app:compileDebugKotlin`.

## Specs

Design source for every spec: `design/project/*.dc.html`, extracted on 2026-11-11 from the export `design/JPTV Launcher.html`. Each file is one 1920x1080 artboard: static markup plus a `<script type="text/x-dc">` block holding its logic. `ThemeOceano.dc.html` contains a single line of about 410 KB; read it with Grep or bounded ranges, never whole.

- **S1 — Themes match the design.** User: "quiero que me ayudes a ajustar los themes segun el diseño que tenemos en claude desing jptv launcher, ya que he realizado algunos cambios, en algunos themes quiero que lo dejes talcual esta en el diseño". Also: "Match how it looks, using this codebase's own components and conventions."
  - Each existing theme (`pixel`, `terminal`, `cartelera`, `zen`, `hud`, `vinilo`, `lofi`, `jptv`) matches its artboard in layout, sizes, colors, typography and motion.
  - Assumption: "tal cual está en el diseño" applies to every theme, since the user did not name which ones.
  - Acceptance: a side-by-side read of the artboard and the composable shows no unexplained difference; every deliberate deviation is reported with its reason.
- **S2 — New themes.** The design adds four artboards with no code yet: `ThemeGotham`, `ThemeMultiverso`, `ThemeCyberpunk`, `ThemeOceano`. Each becomes a theme following the "Add a theme" recipe in `CLAUDE.md` and is registered in `ThemeRegistry`.
  - Acceptance: the theme is selectable from the Themes screen, reads only `HomeState`, calls only `HomeActions`, is D-pad navigable with the shared helpers, and reaches Settings like the existing fixed themes do.
- **S3 — Day/night cycle.** User: "apliques correctamente los ciclos de dia noche segun la hora para los themes correspondientes".
  - The design defines the cycle in exactly three themes: `ThemeLofi`, `ThemeCyberpunk`, `ThemeOceano`. Its rule, verbatim: `const night = m === 'Noche' || (m === 'Auto' && (h >= 18 || h < 6));`
  - The app always behaves as `Auto`: night when the local hour is `>= 18` or `< 6`, day otherwise. The design's `momento` prop ("Auto", "Día", "Noche") is a designer preview control, not a user setting (assumption).
  - Each of the three themes renders the day and the night variant exactly as its artboard defines them, and switches when the hour crosses 06:00 or 18:00 without a restart.
  - Acceptance: a unit test pins the rule at hours 5, 6, 17, 18 and 0.
- **S4 — Clock format.** User: "tambien quiero que la hora pueda ser en horario militar o en am, pm".
  - A persisted preference chooses 24-hour ("horario militar", the current behavior and the default) or 12-hour with AM/PM.
  - Every place that shows the time honors it: all themes, the JP-TV clock card and the Customize preview.
  - Assumption: the design has no control for this, so it is added to the Personalizar screen as one more option card with two chips, following the existing cards.
  - Acceptance: unit tests cover 00:05, 09:40, 12:00, 12:30 and 21:40 in both formats; the preference survives a DataStore round trip and an install with no saved value reads as 24-hour.
- **S5 — Conventions.** Every change follows `CLAUDE.md`: literal hex only in `<Theme>Palette.kt`, tokens from `core/design`, entrance curve `CubicBezierEasing(0.16f, 1f, 0.3f, 1f)`, Spanish strings in resources, and every infinite animation stops when `paused == true` by not creating the transition (the `ZenRipples` early-return pattern), not by hiding the draw.

Out of scope unless the user asks: the non-theme screens (`Network`, `Edit`, `Settings`, and `Customize` beyond the S4 card).

## Tasks

- [~] T1 — S3, S4 — Foundation: `DayPhase` rule, clock format preference, formatter, Customize card, tests. Route: delegated (user instruction: "delega la implementacion a los subagentes"), runs alone first because T2-T9 depend on it. Commit: e3b2ec7 (written, not compiled or tested)
- [~] T2 — S1, S3, S5 — `lofi` with day/night. Route: delegated, parallel. Commit: fe3736a (written, not compiled or tested)
- [~] T3 — S2, S3, S5 — `cyberpunk` (new) with day/night. Route: delegated, parallel. Commit: 21f5a35 (written, not compiled or tested)
- [~] T4 — S2, S3, S5 — `oceano` (new) with day/night. Route: delegated, parallel. Commit: 0b18369 (written, not compiled or tested)
- [~] T5 — S2, S5 — `gotham` (new). Route: delegated, parallel. Commit: 012de03 (written, not compiled or tested)
- [~] T6 — S2, S5 — `multiverso` (new). Route: delegated, parallel. Commit: 08b33e8 (written, not compiled or tested)
- [~] T7 — S1, S5 — `pixel`, `terminal`, `hud`. Route: delegated, parallel. Commit: 19e2963 (written, not compiled or tested)
- [~] T8 — S1, S5 — `cartelera`, `vinilo`, `zen`. Route: delegated, parallel. Commit: 86d65dc (written, not compiled or tested)
- [~] T9 — S1, S5 — `jptv` home and the Themes selector screen. Route: delegated, parallel. Commit: f4ecdd2 (written, not compiled or tested)
- [~] T10 — S2 — Register the four new themes in `ThemeRegistry`; seam check (full unit suite, `assembleDebug`). Route: inline (parent owns the shared file). Commit: 16503a2, 6dd1cd6 (written, not compiled or tested)

Parallel-writer rules for T2-T9: one tree, disjoint surfaces, the parent owns git. No writer edits `ThemeRegistry.kt`, `res/values/strings.xml`, `core/design/*` or `themes/shared/*`. New strings go in `res/values/strings_theme_<id>.xml`. No font downloads: use the fonts already in `res/font`, and report any design font that is missing.

## Log

- L1 (2026-11-11, user, verbatim): "quiero que me ayudes a ajustar los themes segun el diseño que tenemos en claude desing jptv launcher,  ya que he realizado algunos cambios, en algunos themes quiero que lo dejes talcual esta en el diseño y apliques correctamente los ciclos de dia noche segun la hora para los themes correspondientes, tambien quiero que la hora pueda ser en horario militar o en am, pm, delega la implementacion a los subagentes con los modelos mas presisos sonnet, haiku o opus en las versiones 5.5, tu solo orquesta, Implement this design in this codebase: https://claude.ai/code/artifact/ede47d55-c8ee-4dc4-8d9b-2f6c32fc43cd Read the design's files from that link. Match how it looks, using this codebase's own components and conventions."
- L2 (2026-11-11): the artifact could not be read by tools; the user exported it to `design/JPTV Launcher.html`. Its 17 artboards were unpacked and written over `design/project/` (13 updated, 4 new).
- L3 (2026-11-11): code map. The clock has one chokepoint, `core/util/ClockFormat.time()`, hardcoded 24h. `themes/jptv/ClockCard.kt:75` splits the string on ":" and `preferences/ui/CustomizeScreen.kt:343-358` hardcodes "21:40". `DataStoreConfigStore` has no migration; a new field needs a key, a write and a defaulted read, and must be copied in `HomeViewModel.applyCustomize` (`:197-207`) or Customize drops it. No day/night logic exists anywhere. cartelera, hud, lofi and vinilo keep their infinite transition running while paused.
- L6 (2026-11-11): T1 written by a delegated Opus writer, uncommitted, 17 files, +194/-26. Unverified: RED/GREEN, compile and suite were not observed (L5); the parent spot-checked the `ClockFormat` diff by reading. API for T2-T9: `DayPhase.of(state.now).isNight` (`themes/shared/DayPhase.kt`); `ClockFormat.HourFormat { H24, H12 }`; `ClockFormat.parts(now, format): TimeParts(hours, minutes, meridiem?)`; `ClockFormat.time(now, format)` gives "21:40" or "9:40 PM"; the preference is `state.config.hourFormat` (default H24). Open item for T9: `JpTvTheme.kt` must pass `hourFormat = state.config.hourFormat` to `ClockCard`, otherwise the JP-TV home clock stays 24-hour. cartelera, pixel, terminal and vinilo gained an `hourFormat` parameter on their private clock composables.
- L5 (2026-11-11): the build cannot run on this machine. Gradle needs `JAVA_TOOL_OPTIONS="-Djdk.net.unixdomain.tmpdir=C:/Users/j.prada/.gradle"` to get past "Unable to establish loopback connection", and then fails to resolve `com.android.application` 8.7.3 because the proxy answers 403 for `dl.google.com` (Maven Central and the Gradle plugin portal answer 200). No Android artifacts are cached, and WSL Ubuntu has no JDK, SDK or cache. No baseline test run exists; RED/GREEN, compile and suite results are unobserved until the user restores access or builds elsewhere.
- L4 (2026-11-11): environment. No `local.properties`, so there is no `tv.host`: `make deploy` and `make shot` cannot run, and visual verification on the TV is pending. The SDK is at `%LOCALAPPDATA%\Android\Sdk`.

- L7 (2026-11-12): all tasks written and committed on `feat/themes-design-sync`; marked [~] = written but NOT compiled or run (L5). Pending on a machine with Google Maven access: `make test`, `make build`, then `make deploy`/`make shot` against each artboard. Known risks: Compose API signatures, cartelera `cameraDistance`/`rotationY` sign, hud perspective grid omitted (computed off-screen), fonts not bundled (Special Elite, Fredoka, Share Tech Mono, Chakra Petch, DM Mono). Unused strings to remove: `themes_use`, `theme_zen_press_ok`, `theme_pixel_launch`, `theme_lofi_status`.
