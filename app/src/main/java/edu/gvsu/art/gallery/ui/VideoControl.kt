package edu.gvsu.art.gallery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
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
        modifier = modifier
            .then(
                Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 48.dp)
            ),
        horizontalArrangement = Arrangement.Center,
    ) {
        IconButton(
            onClick = {
                state.playSwitch()
            },
        ) {
            Icon(
                painter = rememberVectorPainter(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
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
