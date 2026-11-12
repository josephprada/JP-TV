package co.jp.tv.preferences.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpFonts
import co.jp.tv.playlist.domain.MediaItem
import co.jp.tv.playlist.domain.MediaKind
import co.jp.tv.playlist.domain.MediaSource
import co.jp.tv.playlist.domain.PlaylistOrder
import co.jp.tv.playlist.domain.PlaylistSettings
import co.jp.tv.playlist.domain.RotationInterval
import co.jp.tv.playlist.ui.LocalMediaThumbnails
import co.jp.tv.playlist.ui.labelRes

/**
 * "LISTA" of a playlist wallpaper: a toggle chip for every file found (with a preview picture when it can
 * be made), how often to move on and in what order. Every chip is a D-pad stop, left to right and top to bottom.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PlaylistSection(
    kind: MediaKind,
    found: List<MediaItem>,
    loaded: Boolean,
    settings: PlaylistSettings,
    accent: Color,
    onChange: (PlaylistSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val selectedCount = found.count { settings.isSelected(found, it.id) }
        SectionLabel(stringResource(R.string.customize_playlist, selectedCount, found.size))
        if (found.isEmpty()) {
            if (loaded) {
                BasicText(
                    stringResource(if (kind == MediaKind.VIDEO) R.string.playlist_empty_video else R.string.playlist_empty_image),
                    style = TextStyle(color = JpColors.Muted, fontSize = 9.sp, fontFamily = JpFonts.Body),
                )
            }
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                found.forEach { item ->
                    MediaChip(
                        item = item,
                        selected = settings.isSelected(found, item.id),
                        accent = accent,
                        onClick = { onChange(settings.toggle(found, item.id)) },
                    )
                }
            }
        }
        SectionLabel(stringResource(R.string.customize_playlist_interval))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            RotationInterval.entries.forEach { interval ->
                Chip(
                    label = stringResource(interval.labelRes()),
                    selected = settings.interval == interval,
                    accent = accent,
                    onClick = { onChange(settings.copy(interval = interval)) },
                )
            }
        }
        SectionLabel(stringResource(R.string.customize_playlist_order))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PlaylistOrder.entries.forEach { order ->
                Chip(
                    label = stringResource(order.labelRes()),
                    selected = settings.order == order,
                    accent = accent,
                    onClick = { onChange(settings.copy(order = order)) },
                )
            }
        }
        BasicText(
            stringResource(R.string.customize_playlist_hint),
            style = TextStyle(color = JpColors.Faint, fontSize = 8.sp, fontFamily = JpFonts.Body),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    BasicText(text, style = TextStyle(color = JpColors.Faint, fontSize = 6.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em))
}

/** One file of the list: a small preview, its name and a check when it is part of the rotation. */
@Composable
private fun MediaChip(item: MediaItem, selected: Boolean, accent: Color, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val active = selected || focused
    val thumbnails = LocalMediaThumbnails.current
    val thumbnail by produceState<ImageBitmap?>(null, item.id) { value = thumbnails.load(item) }
    val shape = RoundedCornerShape(11.dp)
    Row(
        Modifier
            .background(if (active) Color(0xB3050506) else JpColors.Paper.copy(alpha = 0.06f), shape)
            .border(
                if (focused) 1.5.dp else 1.dp,
                if (active) accent.copy(alpha = if (focused) 1f else 0.7f) else Color.Transparent,
                shape,
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick)
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            Modifier.size(width = 36.dp, height = 21.dp).clip(RoundedCornerShape(5.dp)).background(JpColors.Paper.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            val image = thumbnail
            if (image != null) {
                Image(image, contentDescription = null, modifier = Modifier.size(width = 36.dp, height = 21.dp), contentScale = ContentScale.Crop)
            } else {
                BasicText(
                    if (item.kind == MediaKind.VIDEO) "▶" else "▣",
                    style = TextStyle(color = JpColors.Faint, fontSize = 8.sp, fontFamily = JpFonts.Mono),
                )
            }
        }
        BasicText(
            text = item.name + if (item.source == MediaSource.USB) " · USB" else "",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(color = if (active) JpColors.Paper else JpColors.Muted, fontSize = 9.sp, fontFamily = JpFonts.Body),
            modifier = Modifier.widthIn(max = 104.dp),
        )
        BasicText(
            if (selected) "✓" else "",
            style = TextStyle(color = accent, fontSize = 9.sp, fontFamily = JpFonts.Body),
            modifier = Modifier.padding(end = 3.dp),
        )
    }
}
