package edu.gvsu.art.gallery.ui.artwork.ar

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.log10
import kotlin.math.pow

/**
 * DEBUG-ONLY placement tuner for AR models. This file is intentionally NOT wired into the
 * production build — nothing here is referenced unless you follow the re-enable steps in
 * `docs/ar-debug-placement.md`.
 *
 * Purpose: overlay live scale / rotation / translate sliders on an under-test model so it can
 * be positioned by hand against its painting, then logged (`AR_BAKE …`) and baked into the GLB
 * with `ar_models/script/refit_glb_scale.py`.
 */

/** Half-range (meters) of each translate slider; wide enough for floor-standing dioramas. */
internal const val DEBUG_T_RANGE = 1.0f

/** Live placement driven by the debug sliders; maps 1:1 to `refit_glb_scale.py` args. */
internal data class DebugPlacement(
    val scale: Float = 1f,
    val tx: Float = 0f,
    val ty: Float = 0f,
    val tz: Float = 0f,
    val rotX: Float = -90f,
)

/**
 * Live scale / rotX / translate sliders for the model in view. The logged line (`AR_BAKE …`)
 * is grep-able from logcat and its values drop straight into `refit_glb_scale.py`
 * (`<scale> <tx> <ty> <tz> <rotx_deg>`). Scale uses a log slider so the whole 0.001–25 range
 * is reachable; rotX snaps to 15° steps.
 */
@Composable
internal fun DebugPlacementPanel(
    placement: DebugPlacement,
    artworkId: String?,
    onChange: (DebugPlacement) -> Unit,
    modifier: Modifier = Modifier,
) {
    // scale <-> log slider: t in [0,1] -> scale = 10^(-3 + 4.4t)  => 0.001 .. ~25
    fun scaleToT(s: Float) = ((log10(s) + 3f) / 4.4f).coerceIn(0f, 1f)
    fun tToScale(t: Float) = 10f.pow(-3f + 4.4f * t)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xCC000000))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "id=${artworkId ?: "—"}  scale=${"%.4f".format(placement.scale)}  rotX=${"%.0f".format(placement.rotX)}\n" +
                "t=(${"%.3f".format(placement.tx)}, ${"%.3f".format(placement.ty)}, ${"%.3f".format(placement.tz)})",
            color = Color.White,
        )
        SliderRow("scale", scaleToT(placement.scale)) { onChange(placement.copy(scale = tToScale(it))) }
        // translate: t in [0,1] -> meters in [-R, R]
        SliderRow("tx", (placement.tx + DEBUG_T_RANGE) / (2 * DEBUG_T_RANGE)) { onChange(placement.copy(tx = it * 2 * DEBUG_T_RANGE - DEBUG_T_RANGE)) }
        SliderRow("ty", (placement.ty + DEBUG_T_RANGE) / (2 * DEBUG_T_RANGE)) { onChange(placement.copy(ty = it * 2 * DEBUG_T_RANGE - DEBUG_T_RANGE)) }
        SliderRow("tz", (placement.tz + DEBUG_T_RANGE) / (2 * DEBUG_T_RANGE)) { onChange(placement.copy(tz = it * 2 * DEBUG_T_RANGE - DEBUG_T_RANGE)) }
        // rotX: t in [0,1] -> degrees in [-180, 180], snapped to 15° steps for clean values
        SliderRow("rotX", (placement.rotX + 180f) / 360f) {
            onChange(placement.copy(rotX = (Math.round((it * 360f - 180f) / 15f) * 15).toFloat()))
        }
        Button(onClick = {
            Log.i(
                "AR_BAKE",
                "id=$artworkId scale=${placement.scale} tx=${placement.tx} ty=${placement.ty} tz=${placement.tz} rotx=${placement.rotX}",
            )
        }) { Text("Log values (Bake)") }
    }
}

@Composable
private fun SliderRow(label: String, value: Float, onValue: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, modifier = Modifier.padding(end = 8.dp))
        Slider(value = value.coerceIn(0f, 1f), onValueChange = onValue, modifier = Modifier.fillMaxWidth())
    }
}
