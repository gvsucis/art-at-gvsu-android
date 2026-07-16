# AR debug placement tuner

A developer-only tool for positioning a new AR model against its painting **on-device**, then
baking those numbers into the GLB. It is **not compiled into any user-facing path** — the UI
lives in [`ARDebugPlacement.kt`](../app/src/main/java/edu/gvsu/art/gallery/ui/artwork/ar/ARDebugPlacement.kt)
but nothing references it until you wire it in per the steps below.

## Why it exists

Filament (SceneView/gltfio) renders a GLB with the transform **baked into the file**. Unlike
iOS/RealityKit — which applies the "stand upright" −90° X rotation at runtime — Android must bake
scale, position, and the −90X into the GLB itself. A model whose server GLB lacks that wrapper
renders lying down / mis-sized (this is exactly why 7288, 10507, and 26699 "never worked" on
Android until they were re-baked). The tuner lets you find the right numbers live instead of
guessing-and-rebuilding.

The pipeline scripts and conventions live in `ar_models/` (see its `README.md`).

## Workflow

1. **Convert** the source USDZ to a raw GLB:
   `Blender --background --factory-startup --python ar_models/script/convert_usdz_to_glb.py -- <usdz_dir> <glb_out_dir>`
2. **Bundle** it: drop the GLB in `app/src/main/assets/ar/<id>.glb`.
3. **Enable the tuner** (code changes below), build, install, open the AR experience.
4. Walk up to the painting. **Double-tap** the screen to toggle the tuner panel.
5. Drag **rotX** (snaps 15°; start at −90), **scale** (log slider), and **tx/ty/tz** (±1 m) until
   the model sits correctly against the painting.
6. Tap **"Log values (Bake)"**. It emits a line to logcat:
   `adb logcat -d | grep AR_BAKE` → `id=<id> scale=… tx=… ty=… tz=… rotx=…`
7. **Bake** the transform into the GLB (transform-only — leaves meshes/materials/animation
   untouched, so the look you approved is preserved):
   ```bash
   python3 ar_models/script/refit_glb_scale.py \
       <raw>.glb ar_models/converted/glb/<id>_<slug>.glb \
       <scale> <tx> <ty> <tz> <rotx>
   ```
   (For a model that also needs material fixes — e.g. Sketchfab spec-gloss — use
   `glb_json_refit.py` instead; see the `ar_models/README.md`.)
8. If the baked GLB is over ~10 MB, compress **geometry only** with meshopt (NOT Draco, which
   broke rendering in gltfio):
   ```bash
   npx @gltf-transform/cli optimize in.glb out.glb --compress meshopt \
       --flatten false --join false --instance false --palette false --simplify false \
       --texture-compress false
   ```
9. Re-bundle the baked GLB and verify it renders correctly **with the tuner disabled** (identity
   transform). Then upload it as the object's `ar_3d_file` and **revert the code changes below**.

## Enabling it in code

The tuner needs a bit of wiring in
[`ARExperienceScreen.kt`](../app/src/main/java/edu/gvsu/art/gallery/ui/artwork/ar/ARExperienceScreen.kt).
It was removed from production for cleanliness; re-add it while tuning, then revert.

**1. A `BUNDLED_MODELS` map** (top of the file) — loads a local asset instead of the remote model:

```kotlin
// artwork id -> bundled GLB under assets/ar/. Loads instead of the remote ar_3d_file so a
// refit model can be gut-checked on-device before it's uploaded. Remove once uploaded.
private val BUNDLED_MODELS = mapOf(
    "7288" to "ar/7288.glb",
)
```

**2. Tuner state** in `ARExperienceContent`, inside the outer `Box`:

```kotlin
var debug by remember { mutableStateOf(DebugPlacement()) }
var tunedId by remember { mutableStateOf<String?>(null) }
var panelVisible by remember { mutableStateOf(false) }
```

**3. Pass the placement** to `ArtworkAROverlay` (add the two args to the call):

```kotlin
ArtworkAROverlay(
    artwork = artwork,
    augmentedImage = image,
    mediaCache = mediaCache,
    modelLoader = modelLoader,
    debug = if (panelVisible) debug else null,
    onTuning = { tunedId = it },
)
```

**4. A double-tap catcher + the panel**, inside the same `Box` (after the `ARSceneView`):

```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(onDoubleTap = { panelVisible = !panelVisible })
        },
)
// ...and near the bottom of the Box:
if (panelVisible) {
    DebugPlacementPanel(
        placement = debug,
        artworkId = tunedId,
        onChange = { debug = it },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .statusBarsPadding(),
    )
}
```

**5. In `ArtworkAROverlay`**, accept the placement, load the bundled asset, and drive the
`ModelNode` transform from it. Add the params:

```kotlin
debug: DebugPlacement? = null,
onTuning: (String) -> Unit = {},
```

then only tune bundled (under-test) models — never touch working server models:

```kotlin
val debugActive = debug != null && BUNDLED_MODELS.containsKey(artwork.id)
```

In the `LaunchedEffect`, before the remote-model branch, report tuning + load the bundled asset:

```kotlin
if (debugActive) onTuning(artwork.id)
val bundledAsset = BUNDLED_MODELS[artwork.id]
if (bundledAsset != null) {
    modelInstance = runCatching { modelLoader.loadModelInstance(bundledAsset) }.getOrNull()
    return@LaunchedEffect
}
```

Finally, make the `ModelNode` drive its transform from the sliders. **Keep it a single call
site** — an `if/else` with two `ModelNode(...)` calls rebuilds the node on every panel toggle,
recreating a heavy model on the main thread and freezing the app:

```kotlin
val d = debug?.takeIf { debugActive }
ModelNode(
    modelInstance = instance,
    autoAnimate = true,
    animationLoop = true,
    position = if (d != null) Position(d.tx, d.ty, d.tz) else Position(),
    rotation = if (d != null) Rotation(x = d.rotX) else Rotation(),
    scale = if (d != null) Scale(d.scale) else Scale(1.0f),
)
```

(Requires imports: `io.github.sceneview.math.Position`, `…Scale`,
`androidx.compose.foundation.gestures.detectTapGestures`,
`androidx.compose.ui.input.pointer.pointerInput`.)

## Notes

- **Axis convention** (Android/ARCore image anchor): `+X` = right, `+Y` = out toward the viewer,
  `+Z` = down the image. So "down" is `+tz`. iOS uses different axes — see
  `ar_models/README.md` for the cross-platform mapping.
- **Placement is per-platform.** Scale, rotation, and material do **not** transfer between the
  Android GLB and the iOS USDZ; only re-derive/re-tune each.
- Always confirm the final baked GLB renders correctly **with the tuner off** before shipping.
