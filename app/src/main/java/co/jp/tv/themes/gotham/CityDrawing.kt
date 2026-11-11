package co.jp.tv.themes.gotham

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.min

/** Window grid of a block: `repeating-linear-gradient` 9px clear + 7px lit across, 11 + 11 up. */
private const val GridAlpha = 0.03f
private const val WindowWidth = 7f
private const val WindowHeight = 11f
private const val WindowAlpha = 0.8f

/** Approximates `box-shadow: 0 0 6px rgba(255,214,107,0.45)` with one soft halo. */
private const val WindowGlowSpread = 3f
private const val WindowGlowAlpha = 0.45f * WindowAlpha * 0.35f

/** `box-shadow: 0 0 10px 3px rgba(255,59,48,0.8)` around the 6px beacon. */
private const val BeaconRadius = 3f
private const val BeaconHaloRadius = 16f
private const val BeaconDimAlpha = 0.1f

/** Halo brush centred at the origin; translated per beacon. */
private val BeaconHalo: Brush = Brush.radialGradient(
    0f to GothamPalette.Beacon.copy(alpha = 0.8f),
    (BeaconRadius + 3f) / BeaconHaloRadius to GothamPalette.Beacon.copy(alpha = 0.8f),
    1f to GothamPalette.Beacon.copy(alpha = 0f),
    center = Offset.Zero,
    radius = BeaconHaloRadius,
)

/** Draws one precomputed skyline layer; expects a design-px (1920x1080) coordinate space. */
internal fun DrawScope.drawCityLayer(shapes: List<CityShape>) {
    for (i in shapes.indices) {
        when (val s = shapes[i]) {
            is CityShape.Block -> drawBlock(s)
            is CityShape.Fill -> drawFill(s)
            is CityShape.Spike -> drawPath(s.path, s.color)
            is CityShape.Window -> drawWindow(s)
        }
    }
}

private fun DrawScope.drawBlock(b: CityShape.Block) {
    drawRect(b.body, topLeft = Offset(b.left, b.top), size = Size(b.width, b.height))
    if (b.grid) {
        var x = 9f
        while (x < b.width) {
            drawRect(
                GothamPalette.Grid,
                topLeft = Offset(b.left + x, b.top),
                size = Size(min(WindowWidth, b.width - x), b.height),
                alpha = GridAlpha,
            )
            x += 16f
        }
        // 0deg gradients start at the bottom edge.
        val foot = b.top + b.height
        var y = 11f
        while (y < b.height) {
            val h = min(11f, b.height - y)
            drawRect(
                GothamPalette.Grid,
                topLeft = Offset(b.left, foot - y - h),
                size = Size(b.width, h),
                alpha = GridAlpha,
            )
            y += 22f
        }
    }
    drawRect(b.edge, topLeft = Offset(b.left, b.top), size = Size(1f, b.height))
}

private fun DrawScope.drawFill(f: CityShape.Fill) {
    if (f.corner > 0f) {
        drawRoundRect(
            f.color,
            topLeft = Offset(f.left, f.top),
            size = Size(f.width, f.height),
            cornerRadius = CornerRadius(f.corner),
        )
    } else {
        drawRect(f.color, topLeft = Offset(f.left, f.top), size = Size(f.width, f.height))
    }
    if (f.edgeAlpha > 0f) {
        drawRect(GothamPalette.Edge, topLeft = Offset(f.left, f.top), size = Size(1f, f.height), alpha = f.edgeAlpha)
    }
    if (f.topEdgeAlpha > 0f) {
        drawRect(GothamPalette.Edge, topLeft = Offset(f.left, f.top), size = Size(f.width, 1f), alpha = f.topEdgeAlpha)
    }
}

private fun DrawScope.drawWindow(w: CityShape.Window) {
    if (w.glow) {
        drawRoundRect(
            GothamPalette.WindowGlow,
            topLeft = Offset(w.left - WindowGlowSpread, w.top - WindowGlowSpread),
            size = Size(WindowWidth + 2 * WindowGlowSpread, WindowHeight + 2 * WindowGlowSpread),
            cornerRadius = CornerRadius(WindowGlowSpread),
            alpha = WindowGlowAlpha,
        )
    }
    drawRect(w.color, topLeft = Offset(w.left, w.top), size = Size(WindowWidth, WindowHeight), alpha = WindowAlpha)
}

/** Aircraft lights: `ng-blink 2.4s <delay> steps(1) infinite` (on for the first half, 0.1 after). */
internal fun DrawScope.drawBeacons(beacons: List<CityBeacon>, timeMs: Long) {
    for (i in beacons.indices) {
        val b = beacons[i]
        val p = GothamMotion.loop(timeMs, GothamMotion.BlinkMs, b.delayMs)
        val alpha = if (p < 0.5f) 1f else BeaconDimAlpha
        translate(b.centerX, b.centerY) {
            drawCircle(BeaconHalo, radius = BeaconHaloRadius, center = Offset.Zero, alpha = alpha)
            drawCircle(GothamPalette.Beacon, radius = BeaconRadius, center = Offset.Zero, alpha = alpha)
        }
    }
}
