package co.jp.tv.themes.terminal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp

private val BannerRows = listOf(
    " ▐█▌ ████▄     ███████ ▐█   █▌",
    "  █▌ █▌  █▌      █▌     █▌ ▐█ ",
    "  █▌ ████▀  ███  █▌     ▐█ █▌ ",
    "▄ █▌ █▌          █▌      ███  ",
    "▀██▀ █▌          █▌       █   ",
)

/**
 * The block-letter banner, drawn cell by cell: VT323 has no block-element glyphs, so text would
 * fall back to another font and break the monospace alignment.
 */
@Composable
internal fun TerminalBanner(modifier: Modifier = Modifier) {
    val cellW = 6.8.dp
    val cellH = 17.dp
    Box(
        modifier
            .size(cellW * BannerRows.maxOf { it.length }, cellH * BannerRows.size)
            .drawBehind {
                val w = cellW.toPx()
                val h = cellH.toPx()
                BannerRows.forEachIndexed { row, line ->
                    line.forEachIndexed { col, ch ->
                        val x = col * w
                        val y = row * h
                        when (ch) {
                            '█' -> drawRect(TerminalPalette.Green, Offset(x, y), Size(w, h))
                            '▌' -> drawRect(TerminalPalette.Green, Offset(x, y), Size(w / 2f, h))
                            '▐' -> drawRect(TerminalPalette.Green, Offset(x + w / 2f, y), Size(w / 2f, h))
                            '▄' -> drawRect(TerminalPalette.Green, Offset(x, y + h / 2f), Size(w, h / 2f))
                            '▀' -> drawRect(TerminalPalette.Green, Offset(x, y), Size(w, h / 2f))
                        }
                    }
                }
            },
    )
}
