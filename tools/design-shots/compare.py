#!/usr/bin/env python3
"""Side-by-side sheets (app render | design artboard) in screens/compare/. Needs Pillow."""
from pathlib import Path
from PIL import Image

PAIRS = {
    "home-jptv": "Main", "home-pixel": "ThemePixel", "home-terminal": "ThemeTerminal",
    "home-cartelera": "ThemeCartelera", "home-zen": "ThemeZen", "home-hud": "ThemeHUD",
    "home-vinilo": "ThemeVinilo", "home-lofi": "ThemeLofi", "themes": "Themes",
    "home-gotham": "ThemeGotham", "home-multiverso": "ThemeMultiverso",
    "home-cyberpunk": "ThemeCyberpunk", "home-oceano": "ThemeOceano",
    "home-lofi-day": "ThemeLofi-day", "home-cyberpunk-day": "ThemeCyberpunk-day",
    "home-oceano-day": "ThemeOceano-day",
    "customize-embers": "Customize", "settings": "Settings", "network": "Network", "edit": "Edit",
}
root = Path(__file__).resolve().parents[2] / "screens"
out = root / "compare"
out.mkdir(exist_ok=True)
for app, design in PAIRS.items():
    a, d = root / f"{app}.png", root / "design" / f"{design}.png"
    if not (a.exists() and d.exists()):
        continue
    ia, idd = Image.open(a).convert("RGB"), Image.open(d).convert("RGB")
    sheet = Image.new("RGB", (ia.width + idd.width + 8, max(ia.height, idd.height)), (255, 0, 255))
    sheet.paste(ia, (0, 0))
    sheet.paste(idd, (ia.width + 8, 0))
    sheet.save(out / f"{app}.png")
    print(out / f"{app}.png")
