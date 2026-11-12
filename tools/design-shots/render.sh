#!/usr/bin/env bash
# Renders design/project/*.dc.html to screens/design/<name>.png (1280x720, comparable with the
# Roborazzi renders) and <name>@1920.png (native artboard size) with a headless Chromium.
# Needs a Chromium: $CHROME, or the Playwright one under ~/.cache/ms-playwright, or one on PATH.
set -euo pipefail
cd "$(dirname "$0")/../.."
OUT=screens/design
TMP="$OUT/.tmp"
mkdir -p "$OUT" "$TMP"

find_chrome() {
  [ -n "${CHROME:-}" ] && { echo "$CHROME"; return; }
  for c in "$HOME"/.cache/ms-playwright/chromium_headless_shell-*/chrome-headless-shell-linux64/chrome-headless-shell \
           "$HOME"/.cache/ms-playwright/chromium-*/chrome-linux64/chrome; do
    [ -x "$c" ] && { echo "$c"; return; }
  done
  for c in chromium chromium-browser google-chrome google-chrome-stable brave-browser; do
    command -v "$c" >/dev/null && { command -v "$c"; return; }
  done
  return 1
}
CHROME_BIN=$(find_chrome) || { echo "No headless Chromium found (set CHROME=/path/to/chrome)"; exit 1; }
FONT_DIR="file://$PWD/app/src/main/res/font"

# Day/night themes read the wall clock ("Auto"); pin them to the fixture moments instead:
# Noche = 21:40 (the default render) and Día = 10:30 (<name>-day).
render() { # <source> <out name> <momento>
  local f=$1 name=$2 momento=$3 page="$TMP/$2.html"
  # The artboards load ./support.js; swap in the shim and point it at the bundled fonts.
  sed -e "s#<script src=\"./support.js\"></script>#<script>window.__FONT_DIR__='$FONT_DIR'</script><script src=\"../../../tools/design-shots/dc-shim.js\"></script>#" \
      -e "s#props.momento ?? 'Auto'#props.momento ?? '$momento'#" "$f" > "$page"
  for scale in 1:"$name@1920" 0.66667:"$name"; do
    dsf=${scale%%:*}; out=${scale#*:}
    "$CHROME_BIN" --headless --no-sandbox --disable-gpu --hide-scrollbars --allow-file-access-from-files \
      --force-device-scale-factor="$dsf" --window-size=1920,1080 --virtual-time-budget=6000 \
      --screenshot="$PWD/$OUT/$out.png" "file://$PWD/$page" >/dev/null 2>&1
  done
  echo "design/$name"
}

for f in design/project/*.dc.html; do
  name=$(basename "$f" .dc.html)
  render "$f" "$name" Noche
  case "$name" in ThemeLofi|ThemeCyberpunk|ThemeOceano) render "$f" "$name-day" Día ;; esac
done
rm -rf "$TMP"
