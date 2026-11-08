# Phase 8 — Projector checklist

The V_BOX14 (`tv.host` in `local.properties`) is the primary verification device. The Google TV projector was not available during this pass.

## Phase 7 notes (box)

- Low-end galaxy: half stars, 30 fps, no orb glow (`DeviceProfile`).
- Cold start (`am start -W` debug APK on the box): ~10 s after force-stop; theme fonts are lazy-loaded so only Anton/Geist hit the first frame. Release + baseline profile remain follow-ups.
- Roborazzi / screenshot CI: deferred (API 28 + AGP cost); visual gate stays `make shot` vs `design/`.

## When the projector is on the network

1. Set `tv.host=<projector-ip>` in `local.properties` (do not commit that file).
2. `adb connect <ip>:5555` then `make deploy && make shot`.
3. Walk through: HOME default, app list, network panel, edit apps, themes picker, customize, each theme once.

## Checks specific to Google TV / projectors

- [ ] Third-party HOME is allowed (some firmwares block it; disable stock launcher over ADB if needed).
- [ ] Density / safe areas: rail and clock stay inside overscan; no clipped pills.
- [ ] Package visibility: `<queries>` already declares LEANBACK_LAUNCHER / LAUNCHER (Android 11+).
- [ ] Storage paths `/sdcard/JPTV/fondo.jpg` and `loop.mp4` readable (or asset fallback for the still image).
- [ ] Low-end heuristics: if RAM ≤ ~2 GB, galaxy uses half stars, 30 fps, no orb glow.
- [ ] Signed release build when shipping beyond debug.

## Density notes

Target first device is 1280×720 @ ~213 dpi. If the projector reports a different density, prefer dp from design (`design px / 2`) already used in themes; only tweak paddings if shots show clipping.
