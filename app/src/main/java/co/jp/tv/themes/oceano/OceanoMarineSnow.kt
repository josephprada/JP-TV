package co.jp.tv.themes.oceano

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.random.Random

/** 150 drifting specks of marine snow (`sw-snow`: 26 px right, 70 px down, 8-18 s alternate). */
@Composable
internal fun OceanoMarineSnow(clock: OceanoClock, modifier: Modifier = Modifier) {
    // x, y, radius, alpha, period, delay.
    val specks = remember {
        val random = Random(3)
        FloatArray(SPECKS * 6).also { a ->
            for (i in 0 until SPECKS) {
                a[i * 6] = random.nextFloat() * OceanoDesign.WIDTH
                a[i * 6 + 1] = random.nextFloat() * OceanoDesign.HEIGHT
                a[i * 6 + 2] = 0.5f + random.nextInt(3) * 0.5f
                a[i * 6 + 3] = 0.25f + random.nextFloat() * 0.44f
                a[i * 6 + 4] = 8.2f + random.nextFloat() * 9.8f
                a[i * 6 + 5] = random.nextFloat() * 8f
            }
        }
    }
    val count = if (clock.lite) SPECKS / 2 else SPECKS
    Canvas(modifier.fillMaxSize().graphicsLayer()) {
        val t = clock.seconds
        inDesignSpace {
            for (i in 0 until count) {
                val p = OceanoMotion.alternate(t, specks[i * 6 + 4], specks[i * 6 + 5])
                drawCircle(
                    OceanoPalette.Snow,
                    radius = specks[i * 6 + 2],
                    center = Offset(specks[i * 6] + 26f * p, specks[i * 6 + 1] + 70f * p),
                    alpha = specks[i * 6 + 3],
                )
            }
        }
    }
}

private const val SPECKS = 150
