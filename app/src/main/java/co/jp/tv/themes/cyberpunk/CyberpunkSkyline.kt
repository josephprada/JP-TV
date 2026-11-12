package co.jp.tv.themes.cyberpunk

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import co.jp.tv.core.design.bakeToBitmap
import co.jp.tv.core.design.bakedDesignRegion
import co.jp.tv.core.design.bakedLayer
import kotlin.math.ceil
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/** Layer x of the far and mid containers (`left: -40px`). */
private const val LAYER_LEFT = -40f

/** Ground line: towers stand on `bottom: 300px`, i.e. y = 780. */
private const val GROUND_Y = 780f

/**
 * Everything behind the neon signs, in the artboard's order: sky, sun, smog, flying vehicles,
 * far megatowers with lights and holo billboards, mid blocks, cables, wet street, streaks, steam.
 * [farShift] and [midShift] are the animated parallax offsets in design px.
 */
@Composable
internal fun CyberpunkSkyline(
    city: CyberpunkCity,
    phase: CyberpunkPalette.Phase,
    time: State<Float>,
    farShift: State<Float>,
    midShift: State<Float>,
) {
    Box(Modifier.fillMaxSize()) {
        SkyLayer(phase)
        VehiclesLayer(time)
        FarLayer(city, phase, time, farShift)
        MidLayer(city, phase, midShift)
        GroundLayer(city, phase)
        SteamLayer(time)
    }
}

@Composable
private fun SkyLayer(phase: CyberpunkPalette.Phase) {
    val night = phase === CyberpunkPalette.Night
    // Night moon: 90 px at (1420, 120). Day sun: 320 px at (1300, 60).
    val sunCx = if (night) 1465f else 1460f
    val sunCy = if (night) 165f else 220f
    val sunR = if (night) 45f else 160f
    val brushes = remember(phase) {
        listOf(
            Brush.verticalGradient(*phase.sky.toTypedArray(), startY = 0f, endY = DESIGN_HEIGHT),
            Brush.radialGradient(*phase.sun.toTypedArray(), center = Offset(sunCx, sunCy), radius = sunR * FARTHEST_CORNER),
            Brush.radialGradient(0f to phase.smogA, 0.68f to phase.smogA.copy(alpha = 0f), center = Offset(450f, 680f), radius = 650f * FARTHEST_CORNER),
            Brush.radialGradient(0f to phase.smogB, 0.68f to phase.smogB.copy(alpha = 0f), center = Offset(1550f, 660f), radius = 650f * FARTHEST_CORNER),
        )
    }
    Spacer(
        Modifier
            .fillMaxSize()
            .bakedLayer {
                inDesignPx {
                    drawRect(brushes[0], size = Size(DESIGN_WIDTH, DESIGN_HEIGHT))
                    ovalGradient(sunCx, sunCy, sunR, sunR, brushes[1], phase.sunAlpha)
                    ovalGradient(450f, 680f, 650f, 210f, brushes[2])
                    ovalGradient(1550f, 660f, 650f, 220f, brushes[3])
                }
            },
    )
}

/** Design vehicles: y, duration, delay, [CyberpunkPalette.Neon] index; odd ones fly in reverse. */
private val VEHICLES = listOf(
    floatArrayOf(120f, 32f, 0f, 0f),
    floatArrayOf(190f, 44f, 9f, 1f),
    floatArrayOf(90f, 38f, 16f, 3f),
    floatArrayOf(240f, 52f, 4f, 2f),
    floatArrayOf(150f, 28f, 22f, 0f),
    floatArrayOf(210f, 46f, 30f, 1f),
)

@Composable
private fun VehiclesLayer(time: State<Float>) {
    val brushes = remember {
        VEHICLES.map { v ->
            val c = CyberpunkPalette.Neon[v[3].toInt()]
            Brush.horizontalGradient(
                0f to c.copy(alpha = 0f),
                0.7f to c.copy(alpha = 0.6f),
                1f to CyberpunkPalette.White,
                startX = 0f,
                endX = 80f,
            )
        }
    }
    Spacer(
        Modifier
            .fillMaxSize()
            .graphicsLayer()
            .drawBehind {
                val t = time.value
                inDesignPx {
                    VEHICLES.forEachIndexed { k, v ->
                        val p = CyberpunkKeyframes.progress(t, v[1], v[2])
                        val x = when {
                            p == null -> 0f
                            k % 2 == 1 -> 2200f - 2460f * p
                            else -> -260f + 2460f * p
                        }
                        val c = CyberpunkPalette.Neon[v[3].toInt()]
                        softRect(x, v[0], 80f, 3f, c, blur = 10f, corner = 2f)
                        translate(x, v[0]) {
                            drawRoundRect(brushes[k], size = Size(80f, 3f), cornerRadius = CornerRadius(2f, 2f))
                        }
                    }
                }
            },
    )
}

private class FarBrushes(val towers: List<Brush>, val boards: List<Brush>)

@Composable
private fun FarLayer(city: CyberpunkCity, phase: CyberpunkPalette.Phase, time: State<Float>, shift: State<Float>) {
    val brushes = remember(phase) {
        FarBrushes(
            towers = city.towers.map {
                Brush.verticalGradient(listOf(phase.far1, phase.far2), startY = GROUND_Y - it.h, endY = GROUND_Y)
            },
            boards = city.billboards.map { b ->
                val c = CyberpunkPalette.Neon[b.color]
                // 135deg: from top-left to bottom-right over a line of length (w + h) / sqrt(2).
                val half = (b.w + b.h) / 4f
                val cx = b.x + b.w / 2f
                val cy = b.y + b.h / 2f
                Brush.linearGradient(
                    listOf(c.copy(alpha = 0.9f), c.copy(alpha = 0.2f)),
                    start = Offset(cx - half, cy - half),
                    end = Offset(cx + half, cy + half),
                )
            },
        )
    }
    // The towers and the constant billboard halos never change: baked once. They slide with the parallax, so the
    // bitmap covers the whole row of towers (from x = -80 to the last one), not just the screen.
    Spacer(
        Modifier
            .fillMaxSize()
            .graphicsLayer { translationX = shift.value * 0.5.dp.toPx() }
            .bakedDesignRegion(FAR_LEFT, 0f, FAR_RIGHT, GROUND_Y) {
                translate(LAYER_LEFT, 0f) { drawFarTowers(city, brushes) }
            },
    )
    // The lights and holo billboards animate: their own layer, with the billboards baked once and faded per frame.
    Spacer(
        Modifier
            .fillMaxSize()
            .graphicsLayer { translationX = shift.value * 0.5.dp.toPx() }
            .drawWithCache {
                val k = 0.5.dp.toPx()
                val boards = city.billboards.mapIndexed { i, b ->
                    bakeToBitmap(ceil((b.w + 2 * BOARD_PAD) * k).toInt(), ceil((b.h + 2 * BOARD_PAD) * k).toInt()) {
                        scale(k, k, Offset.Zero) {
                            translate(BOARD_PAD - b.x, BOARD_PAD - b.y) { drawHoloBoard(b, brushes.boards[i]) }
                        }
                    }
                }
                onDrawBehind {
                    val t = time.value
                    inDesignPx {
                        translate(LAYER_LEFT, 0f) { drawFarLights(city, phase, t) }
                    }
                    city.billboards.forEachIndexed { i, b ->
                        val a = CyberpunkKeyframes.progress(t, b.dur, b.delay)?.let(CyberpunkKeyframes::holo) ?: 1f
                        drawImage(
                            boards[i],
                            topLeft = Offset((b.x - BOARD_PAD + LAYER_LEFT) * k, (b.y - BOARD_PAD) * k),
                            alpha = a,
                        )
                    }
                }
            },
    )
}

private const val FAR_LEFT = -90f
private const val MID_TOP = 270f
private const val FAR_RIGHT = 2160f

/** Design px around a billboard kept in its bitmap for the glow. */
private const val BOARD_PAD = 70f

private fun DrawScope.drawFarTowers(city: CyberpunkCity, brushes: FarBrushes) {
    city.towers.forEachIndexed { i, tower ->
        val top = GROUND_Y - tower.h
        drawRect(brushes.towers[i], topLeft = Offset(tower.x, top), size = Size(tower.w, tower.h))
        drawRect(CyberpunkPalette.FarEdge, topLeft = Offset(tower.x, top), size = Size(1f, tower.h))
    }
    city.billboards.forEach { b ->
        val c = CyberpunkPalette.Neon[b.color]
        softRect(b.x - 30f, b.y - 30f, b.w + 60f, b.h + 60f, c.copy(alpha = 0.25f), blur = 26f)
    }
}

private fun DrawScope.drawFarLights(city: CyberpunkCity, phase: CyberpunkPalette.Phase, t: Float) {
    for (l in city.farLights) {
        when (l) {
            is CyberpunkCity.Dot -> {
                val a = CyberpunkKeyframes.progress(t, l.dur, l.delay)?.let(CyberpunkKeyframes::twinkle) ?: phase.dotAlpha
                drawRect(CyberpunkPalette.Neon[l.color], Offset(l.x, DESIGN_HEIGHT - l.bottom - 4f), Size(3f, 4f), alpha = a)
            }
            is CyberpunkCity.Strip -> {
                val a = CyberpunkKeyframes.progress(t, l.dur, l.delay)?.let(CyberpunkKeyframes::flicker) ?: 0.8f
                val c = CyberpunkPalette.Neon[l.color]
                val y = DESIGN_HEIGHT - l.bottom - l.h
                softRect(l.x, y, 3f, l.h, c, blur = 26f, alpha = a)
                softRect(l.x, y, 3f, l.h, c, blur = 10f, alpha = a)
                drawRect(c, Offset(l.x, y), Size(3f, l.h), alpha = a)
            }
            is CyberpunkCity.BeaconLight -> {
                val a = CyberpunkKeyframes.progress(t, 2.2f, l.delay)?.let(CyberpunkKeyframes::blink) ?: 1f
                val y = DESIGN_HEIGHT - l.bottom - 6f
                softRect(l.x, y, 6f, 6f, CyberpunkPalette.Beacon.copy(alpha = 0.8f), blur = 10f, corner = 3f, spread = 3f, alpha = a)
                drawCircle(CyberpunkPalette.Beacon, radius = 3f, center = Offset(l.x + 3f, y + 3f), alpha = a)
            }
        }
    }
}

/** A holo billboard at full strength: glow, gradient face and scanline stripes. */
private fun DrawScope.drawHoloBoard(b: CyberpunkCity.Billboard, face: Brush) {
    val c = CyberpunkPalette.Neon[b.color]
    softRect(b.x, b.y, b.w, b.h, c.copy(alpha = 0.6f), blur = 30f, corner = 4f)
    drawRoundRect(face, topLeft = Offset(b.x, b.y), size = Size(b.w, b.h), cornerRadius = CornerRadius(4f, 4f))
    // repeating-linear-gradient(0deg): 2 px at 0.85, 2 px at 0.35, from the bottom up.
    var y = b.y + b.h - 2f
    var strong = true
    while (y > b.y - 2f) {
        val top = y.coerceAtLeast(b.y)
        val h = (y + 2f - top).coerceAtLeast(0f)
        drawRect(c, Offset(b.x, top), Size(b.w, h), alpha = if (strong) 0.85f else 0.35f)
        strong = !strong
        y -= 2f
    }
}

@Composable
private fun MidLayer(city: CyberpunkCity, phase: CyberpunkPalette.Phase, shift: State<Float>) {
    val blockBrushes = remember(phase) {
        city.blocks.map { Brush.verticalGradient(listOf(phase.mid1, phase.mid2), startY = GROUND_Y - it.h, endY = GROUND_Y) }
    }
    Spacer(
        Modifier
            .fillMaxSize()
            .graphicsLayer { translationX = shift.value * 0.5.dp.toPx() }
            // Static, but it slides with the parallax: bake the whole row of blocks, not just the screen.
            .bakedDesignRegion(FAR_LEFT, MID_TOP, FAR_RIGHT, GROUND_Y + 20f) {
                run {
                    translate(LAYER_LEFT, 0f) {
                        var w = 0
                        var r = 0
                        city.blocks.forEachIndexed { i, b ->
                            val top = GROUND_Y - b.h
                            drawRect(blockBrushes[i], Offset(b.x, top), Size(b.w, b.h))
                            // repeating-linear-gradient(0deg): 10 px clear, 8 px faint stripe, from the bottom.
                            var s = GROUND_Y - 10f
                            while (s > top) {
                                val st = (s - 8f).coerceAtLeast(top)
                                drawRect(CyberpunkPalette.MidStripe, Offset(b.x, st), Size(b.w, s - st))
                                s -= 18f
                            }
                            drawRect(CyberpunkPalette.MidEdge, Offset(b.x, top), Size(1f, b.h))
                            while (w < city.windows.size && city.windows[w].x < b.x + b.w) {
                                val win = city.windows[w]
                                drawRect(
                                    CyberpunkPalette.WindowColors[win.color],
                                    Offset(win.x, DESIGN_HEIGHT - win.bottom - 9f),
                                    Size(7f, 9f),
                                    alpha = phase.windowAlpha,
                                )
                                w++
                            }
                            while (r < city.roofLines.size && city.roofLines[r].x == b.x) {
                                val roof = city.roofLines[r]
                                val c = CyberpunkPalette.Neon[roof.color]
                                val y = DESIGN_HEIGHT - roof.bottom - 3f
                                softRect(roof.x, y, roof.w, 3f, c, blur = 28f, alpha = 0.85f)
                                softRect(roof.x, y, roof.w, 3f, c, blur = 12f, alpha = 0.85f)
                                drawRect(c, Offset(roof.x, y), Size(roof.w, 3f), alpha = 0.85f)
                                r++
                            }
                        }
                    }
                }
            },
    )
}

private class GroundBrushes(val street: Brush, val horizon: Brush, val streaks: List<Brush>)

@Composable
private fun GroundLayer(city: CyberpunkCity, phase: CyberpunkPalette.Phase) {
    val cables = remember {
        listOf(
            Path().apply { moveTo(-20f, 110f); cubicTo(500f, 210f, 1100f, 190f, 1940f, 90f) },
            Path().apply { moveTo(-20f, 170f); cubicTo(420f, 250f, 900f, 260f, 1940f, 160f) },
            Path().apply { moveTo(-20f, 60f); cubicTo(700f, 150f, 1400f, 140f, 1940f, 210f) },
            Path().apply { moveTo(-20f, 230f); cubicTo(600f, 300f, 1300f, 290f, 1940f, 250f) },
        )
    }
    val thick = remember { Stroke(width = 2.5f) }
    val thin = remember { Stroke(width = 1.6f) }
    val brushes = remember(phase) {
        GroundBrushes(
            street = Brush.verticalGradient(*phase.street.toTypedArray(), startY = GROUND_Y, endY = DESIGN_HEIGHT),
            horizon = Brush.horizontalGradient(
                0f to CyberpunkPalette.Pink.copy(alpha = 0f),
                0.3f to CyberpunkPalette.Pink.copy(alpha = 0.7f),
                0.7f to CyberpunkPalette.Cyan.copy(alpha = 0.7f),
                1f to CyberpunkPalette.Cyan.copy(alpha = 0f),
                startX = 0f,
                endX = DESIGN_WIDTH,
            ),
            streaks = city.streaks.map {
                val c = CyberpunkPalette.StreakColors[it.color]
                Brush.verticalGradient(listOf(c.copy(alpha = phase.streakAlpha), c.copy(alpha = 0f)), startY = 790f, endY = 790f + it.h)
            },
        )
    }
    Spacer(
        Modifier
            .fillMaxSize()
            .bakedLayer {
                inDesignPx {
                    cables.forEachIndexed { i, path ->
                        drawPath(path, CyberpunkPalette.Ink, style = if (i == 3) thin else thick)
                    }
                    drawRect(brushes.street, Offset(0f, GROUND_Y), Size(DESIGN_WIDTH, DESIGN_HEIGHT - GROUND_Y))
                    drawRect(brushes.horizon, Offset(0f, 779f), Size(DESIGN_WIDTH, 2f))
                    city.streaks.forEachIndexed { i, s ->
                        drawRect(brushes.streaks[i], Offset(s.x, 790f), Size(s.w, s.h))
                    }
                }
            },
    )
}

/** Design steam puffs: left, top, width, height, alpha, duration, delay. */
private val STEAM = listOf(
    floatArrayOf(300f, 660f, 260f, 200f, 0.24f, 9f, 0f),
    floatArrayOf(1130f, 680f, 240f, 190f, 0.2f, 11f, 3f),
    floatArrayOf(1620f, 670f, 260f, 200f, 0.22f, 10f, 6f),
)

@Composable
private fun SteamLayer(time: State<Float>) {
    val brushes = remember {
        STEAM.map { s ->
            val c = CyberpunkPalette.Steam
            Brush.radialGradient(
                0f to c.copy(alpha = s[4]),
                0.7f to c.copy(alpha = 0f),
                center = Offset(s[0] + s[2] / 2f, s[1] + s[3] / 2f),
                radius = s[2] / 2f * FARTHEST_CORNER,
            )
        }
    }
    Spacer(
        Modifier
            .fillMaxSize()
            .graphicsLayer()
            .drawBehind {
                val t = time.value
                inDesignPx {
                    STEAM.forEachIndexed { i, s ->
                        val p = CyberpunkKeyframes.progress(t, s[5], s[6])
                        var dy = 0f
                        var k = 1f
                        var a = 1f
                        if (p != null) {
                            val e = CyberpunkMotion.CssEaseOut.transform(p)
                            dy = -280f * e
                            k = 0.6f + 1.1f * e
                            a = if (p < 0.2f) {
                                0.4f * CyberpunkMotion.CssEaseOut.transform(p / 0.2f)
                            } else {
                                0.4f * (1f - CyberpunkMotion.CssEaseOut.transform((p - 0.2f) / 0.8f))
                            }
                        }
                        val cx = s[0] + s[2] / 2f
                        val cy = s[1] + s[3] / 2f
                        translate(0f, dy) {
                            scale(k, pivot = Offset(cx, cy)) {
                                ovalGradient(cx, cy, s[2] / 2f, s[3] / 2f, brushes[i], a)
                            }
                        }
                    }
                }
            },
    )
}
