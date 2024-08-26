package edu.gvsu.art.gallery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import edu.gvsu.art.gallery.ui.foundation.VideoPlayerState

@Composable
fun VideoControl(
    state: VideoPlayerState,
    modifier: Modifier = Modifier,
) {
    if (!state.isReady) {
        return
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(
            onClick = {
                state.playSwitch()
            },
        ) {
            Icon(
                painter = rememberVectorPainter(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow),
                contentDescription = null,
                tint = Color.White,
            )
        }

        Box(modifier.weight(1f)) {
            Slider(
                colors = SliderDefaults.colors(
                    thumbColor = Color.White
                ),
                valueRange = 0f..state.duration.toFloat(),
                value = state.currentPosition.toFloat(),
                onValueChange = {
                    state.seeking()
                    state.currentPosition = it.toLong()
                },
                onValueChangeFinished = {
                    state.seekTo(state.currentPosition)
                }
            )
        }
    }
}
