#!/usr/bin/env python3
"""Procedurally synthesizes the launcher's UI sounds: 12 themes x 4 sounds, all original.

Pure Python (standard library only). Output: mono, 16-bit, 22050 Hz WAV files of at most 150 ms,
peak-normalized and faded in and out (no clicks), written to app/src/main/res/raw/snd_<theme>_<sound>.wav.
The synthesis is deterministic (fixed noise seed), so rerunning produces identical files.

    python3 tools/sounds/synth.py            # regenerate everything
    python3 tools/sounds/synth.py zen hud    # only some themes

Sounds per theme: move (focus moves), select (OK / launch), back, control (a header control gets focus).
"""
import math
import random
import struct
import sys
import wave
from pathlib import Path

SR = 22050
MAX_MS = 150
FADE_IN_MS = 2.0
FADE_OUT_MS = 12.0
PEAK = 0.85
OUT = Path(__file__).resolve().parents[2] / "app/src/main/res/raw"
TAU = 2 * math.pi


# ---------------------------------------------------------------- building blocks

def n_samples(ms):
    return int(SR * ms / 1000)


def silence(ms):
    return [0.0] * n_samples(ms)


def wave_value(shape, phase, duty=0.5):
    """One sample of an oscillator at [phase] in cycles."""
    p = phase % 1.0
    if shape == "sine":
        return math.sin(TAU * p)
    if shape == "square":
        return 1.0 if p < duty else -1.0
    if shape == "tri":
        return 4 * abs(p - 0.5) - 1
    if shape == "saw":
        return 2 * p - 1
    raise ValueError(shape)


def osc(freq, ms, shape="sine", duty=0.5, freq_end=None, vibrato=0.0, vib_hz=18.0):
    """Oscillator with an optional exponential glide from [freq] to [freq_end]."""
    n = n_samples(ms)
    out = []
    phase = 0.0
    for i in range(n):
        t = i / max(n - 1, 1)
        f = freq if freq_end is None else freq * (freq_end / freq) ** t
        if vibrato:
            f *= 1 + vibrato * math.sin(TAU * vib_hz * i / SR)
        phase += f / SR
        out.append(wave_value(shape, phase, duty))
    return out


def noise(ms, seed=1):
    rng = random.Random(seed)
    return [rng.uniform(-1, 1) for _ in range(n_samples(ms))]


def env(samples, attack_ms=1.0, decay_ms=30.0, curve=1.0):
    """Linear attack, exponential decay with time constant [decay_ms]."""
    a = max(n_samples(attack_ms), 1)
    out = []
    for i, s in enumerate(samples):
        g = i / a if i < a else math.exp(-curve * (i - a) / SR / (decay_ms / 1000))
        out.append(s * min(g, 1.0))
    return out


def lowpass(samples, cutoff):
    a = 1 - math.exp(-TAU * cutoff / SR)
    y = 0.0
    out = []
    for s in samples:
        y += a * (s - y)
        out.append(y)
    return out


def highpass(samples, cutoff):
    low = lowpass(samples, cutoff)
    return [s - l for s, l in zip(samples, low)]


def bandpass(samples, low, high):
    return lowpass(highpass(samples, low), high)


def crush(samples, hold, bits):
    """Sample-and-hold downsampling plus bit reduction: the cyberpunk glitch."""
    levels = 2 ** bits
    out = []
    held = 0.0
    for i, s in enumerate(samples):
        if i % hold == 0:
            held = round(s * levels) / levels
        out.append(held)
    return out


def tremolo(samples, hz, depth):
    return [s * (1 - depth + depth * 0.5 * (1 + math.sin(TAU * hz * i / SR))) for i, s in enumerate(samples)]


def gain(samples, g):
    return [s * g for s in samples]


def mix(*tracks, offsets=None):
    """Sums tracks; [offsets] are start times in ms."""
    offsets = offsets or [0] * len(tracks)
    length = max(n_samples(o) + len(t) for t, o in zip(tracks, offsets))
    out = [0.0] * length
    for t, o in zip(tracks, offsets):
        start = n_samples(o)
        for i, s in enumerate(t):
            out[start + i] += s
    return out


def seq(*tracks):
    out = []
    for t in tracks:
        out.extend(t)
    return out


def bell(freq, ms, decay_ms, partials=((1.0, 1.0), (2.76, 0.4), (5.4, 0.15))):
    """Inharmonic partials: glass or bell."""
    parts = [gain(env(osc(freq * r, ms), 0.5, decay_ms / (1 + 0.6 * i)), a) for i, (r, a) in enumerate(partials)]
    return mix(*parts)


def finish(samples, level):
    """Trim to the maximum length, fade, normalize to [PEAK] * [level]."""
    samples = samples[: n_samples(MAX_MS)]
    fi, fo = n_samples(FADE_IN_MS), n_samples(FADE_OUT_MS)
    n = len(samples)
    out = []
    for i, s in enumerate(samples):
        g = 1.0
        if i < fi:
            g = i / fi
        if i >= n - fo:
            g = min(g, (n - 1 - i) / fo)
        out.append(s * g)
    top = max((abs(s) for s in out), default=1.0) or 1.0
    k = PEAK * level / top
    return [max(-1.0, min(1.0, s * k)) for s in out]


def write_wav(path, samples):
    with wave.open(str(path), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes(b"".join(struct.pack("<h", int(round(s * 32767))) for s in samples))


# ---------------------------------------------------------------- the twelve palettes
# Every entry returns (samples, level): level is the loudness relative to the palette's loudest sound.

def jptv():
    """Soft glassy ticks."""
    move = bell(2600, 60, 14, ((1, 1), (2.76, 0.35)))
    select = mix(bell(1760, 110, 30), bell(2640, 110, 36), offsets=[0, 38])
    back = mix(bell(2200, 90, 24), bell(1480, 90, 28), offsets=[0, 34])
    control = bell(3200, 50, 10, ((1, 1), (2.76, 0.25)))
    return {"move": (move, 0.55), "select": (select, 0.85), "back": (back, 0.7), "control": (control, 0.5)}


def pixel():
    """8-bit square-wave blips."""
    def blip(f, ms, duty=0.5, f2=None):
        return env(osc(f, ms, "square", duty, f2), 0.5, ms * 0.7, 0.8)
    move = blip(988, 34, 0.25)
    select = seq(blip(659, 34), blip(880, 34), blip(1319, 56, 0.25))
    back = seq(blip(523, 38, 0.5), blip(349, 60, 0.5))
    control = gain(env(osc(1568, 30, "tri"), 0.5, 20), 1.0)
    return {"move": (move, 0.55), "select": (select, 0.8), "back": (back, 0.7), "control": (control, 0.5)}


def terminal():
    """Keyboard clicks and a teletype beep."""
    def click(seed, body=1800, ms=22):
        hiss = env(highpass(noise(ms, seed), 1500), 0.2, 3.2)
        thock = env(osc(body, ms), 0.2, 4.5)
        return mix(gain(hiss, 1.0), gain(thock, 0.5))
    def beep(f, ms):
        return env(osc(f, ms, "square", 0.5), 1, ms * 0.8, 0.7)
    move = click(11)
    select = mix(click(12, 1400, 26), gain(beep(1000, 80), 0.5), offsets=[0, 20])
    back = mix(click(13, 900, 26), gain(beep(620, 70), 0.5), offsets=[0, 20])
    control = click(14, 2600, 14)
    return {"move": (move, 0.5), "select": (select, 0.85), "back": (back, 0.75), "control": (control, 0.4)}


def cartelera():
    """A soft projector click and a theatre chime."""
    def clack(seed, f=140, ms=45):
        thump = env(osc(f, ms, "sine", freq_end=f * 0.6), 0.5, 11)
        tick = env(bandpass(noise(ms, seed), 800, 4000), 0.2, 5)
        return mix(thump, gain(tick, 0.5))
    move = clack(21)
    select = mix(clack(22, 120, 50), bell(880, 140, 42, ((1, 1), (2, 0.5), (3, 0.2))), offsets=[0, 24])
    back = clack(23, 100, 55)
    control = gain(clack(24, 180, 30), 1.0)
    return {"move": (move, 0.6), "select": (select, 0.85), "back": (back, 0.65), "control": (control, 0.45)}


def zen():
    """Wood block and a soft bell."""
    def wood(f, ms=70):
        return mix(
            env(osc(f * 1.35, ms, freq_end=f), 0.8, 13),
            gain(env(osc(f * 2.9, ms, freq_end=f * 2.7), 0.5, 7), 0.3),
        )
    move = wood(640, 55)
    select = mix(wood(520, 60), bell(528, 140, 55, ((1, 1), (2.0, 0.3), (3.01, 0.12))), offsets=[0, 8])
    back = wood(420, 70)
    control = wood(820, 40)
    return {"move": (move, 0.55), "select": (select, 0.8), "back": (back, 0.65), "control": (control, 0.45)}


def hud():
    """Sci-fi chirps."""
    def chirp(f0, f1, ms, vib=0.0):
        return env(osc(f0, ms, "sine", freq_end=f1, vibrato=vib), 1.5, ms * 0.6, 0.9)
    move = chirp(1100, 1900, 38)
    select = mix(chirp(1200, 2400, 70, 0.01), chirp(1800, 3000, 70), offsets=[0, 32])
    back = chirp(1900, 700, 85)
    control = gain(env(osc(2200, 45, "sine", vibrato=0.03, vib_hz=40), 1, 25), 1.0)
    return {"move": (move, 0.5), "select": (select, 0.8), "back": (back, 0.65), "control": (control, 0.45)}


def vinilo():
    """Vinyl tick and a needle drop."""
    def crackle(seed, ms, hp=900, decay=7):
        return env(highpass(noise(ms, seed), hp), 0.2, decay)
    def thump(f, ms):
        return env(osc(f, ms, freq_end=f * 0.7), 0.5, 20)
    move = mix(crackle(31, 30), gain(thump(110, 30), 0.35))
    select = mix(thump(70, 120), gain(crackle(32, 120, 700, 28), 0.7), gain(crackle(33, 40), 0.5), offsets=[0, 0, 70])
    back = mix(gain(crackle(34, 40, 600, 9), 1.0), gain(thump(85, 40), 0.4))
    control = crackle(35, 18, 1500, 4)
    return {"move": (move, 0.5), "select": (select, 0.85), "back": (back, 0.65), "control": (control, 0.4)}


def lofi():
    """Muted felt keys with a little tape wobble."""
    def key(f, ms, decay=45):
        raw = mix(osc(f, ms, "tri"), gain(osc(f * 2, ms, "sine"), 0.25))
        return lowpass(env(raw, 6, decay), 1500)
    move = tremolo(key(392, 90, 40), 7, 0.15)
    select = seq(key(392, 60, 30), key(523, 90, 50))
    back = key(294, 100, 45)
    control = key(440, 55, 25)
    return {"move": (move, 0.55), "select": (select, 0.8), "back": (back, 0.65), "control": (control, 0.45)}


def gotham():
    """Low, deep thuds."""
    def thud(f, ms, decay, seed):
        body = env(osc(f * 1.6, ms, freq_end=f), 1, decay)
        rumble = env(lowpass(noise(ms, seed), 260), 1, decay * 0.7)
        return mix(body, gain(rumble, 0.4))
    move = thud(72, 100, 22, 41)
    select = mix(thud(52, 150, 40, 42), gain(thud(104, 100, 26, 43), 0.4))
    back = thud(58, 120, 30, 44)
    control = thud(95, 70, 16, 45)
    return {"move": (move, 0.65), "select": (select, 0.9), "back": (back, 0.75), "control": (control, 0.5)}


def multiverso():
    """Shimmering whooshes."""
    def whoosh(ms, rising=True, seed=51, shimmer=1500):
        n = n_samples(ms)
        body = noise(ms, seed)
        swept = []
        # A band that glides up (or down) across the sound.
        phase = 0.0
        for i, s in enumerate(body):
            t = i / max(n - 1, 1)
            f = 600 * (6 ** (t if rising else 1 - t))
            phase += f / SR
            swept.append(s * (0.55 + 0.45 * math.sin(TAU * phase)))
        swept = lowpass(swept, 5000)
        shape = [math.sin(math.pi * i / max(n - 1, 1)) ** 1.5 for i in range(n)]
        air = [s * g for s, g in zip(swept, shape)]
        glitter = tremolo(bell(shimmer, ms, ms * 0.6, ((1, 1), (1.5, 0.5), (2.01, 0.4))), 24, 0.6)
        glitter = [s * g for s, g in zip(glitter, shape)]
        return mix(air, gain(glitter[: len(air)], 0.45))
    move = whoosh(75, True, 51, 2200)
    select = whoosh(140, True, 52, 1500)
    back = whoosh(110, False, 53, 1800)
    control = whoosh(45, True, 54, 3000)
    return {"move": (move, 0.5), "select": (select, 0.8), "back": (back, 0.65), "control": (control, 0.4)}


def cyberpunk():
    """Bit-crushed synth glitches."""
    def stab(f, ms, hold=3, bits=4, shape="saw", f_end=None):
        return crush(env(osc(f, ms, shape, freq_end=f_end), 0.5, ms * 0.6, 0.8), hold, bits)
    move = stab(440, 40, 3, 4)
    chord = mix(stab(220, 130, 2, 5), stab(330, 130, 2, 5), gain(stab(222, 130, 2, 5), 0.8))
    select = chord
    back = stab(330, 90, 4, 3, "saw", f_end=110)
    stutter = seq(stab(1760, 14, 1, 5, "square"), silence(8), stab(1760, 14, 1, 5, "square"))
    return {"move": (move, 0.55), "select": (select, 0.85), "back": (back, 0.7), "control": (stutter, 0.45)}


def oceano():
    """Bubble pops."""
    def bubble(f0, f1, ms):
        return env(osc(f0, ms, "sine", freq_end=f1), 1, ms * 0.35, 1.1)
    move = bubble(420, 1100, 48)
    select = seq(bubble(380, 1000, 50), bubble(520, 1500, 60))
    back = bubble(1000, 330, 80)
    control = bubble(900, 2200, 30)
    return {"move": (move, 0.55), "select": (select, 0.8), "back": (back, 0.65), "control": (control, 0.45)}


THEMES = {
    "jptv": jptv, "pixel": pixel, "terminal": terminal, "cartelera": cartelera,
    "zen": zen, "hud": hud, "vinilo": vinilo, "lofi": lofi,
    "gotham": gotham, "multiverso": multiverso, "cyberpunk": cyberpunk, "oceano": oceano,
}


def main(argv):
    names = argv or list(THEMES)
    unknown = [n for n in names if n not in THEMES]
    if unknown:
        sys.exit(f"unknown theme(s): {', '.join(unknown)}; known: {', '.join(THEMES)}")
    OUT.mkdir(parents=True, exist_ok=True)
    for name in names:
        for sound, (samples, level) in THEMES[name]().items():
            data = finish(samples, level)
            path = OUT / f"snd_{name}_{sound}.wav"
            write_wav(path, data)
            print(f"{path.name:28s} {len(data) * 1000 // SR:4d} ms {path.stat().st_size:6d} B")


if __name__ == "__main__":
    main(sys.argv[1:])
