# JP-TV

A custom launcher for Android TV and Google TV: glass UI, live wallpapers (galaxy, embers or your own video loop), a left app rail, network status at a glance, quick access to native Android settings, and switchable themes that reshape the whole home screen.

**Status:** phases 0–7 on `main` (launcher, network, edit apps, customize, eight themes, low-end polish). Projector verification is pending — see [docs/phase-8-projector.md](docs/phase-8-projector.md). Plan: [PLAN.md](PLAN.md).

## Themes

JP-TV (default, customizable accent, font and name) · Pixel · Terminal · Cartelera · Zen · HUD · Vinilo · Lo-fi

## Design

The approved design lives in [`design/`](design/). Each `.dc.html` file is one screen.

## Quick start (once phase 0 lands)

```bash
make deploy   # build, install on the TV in local.properties, set as home
```

## License

Personal project by Joseph Prada. Bundled fonts are under the SIL Open Font License.
