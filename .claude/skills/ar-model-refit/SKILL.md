---
name: ar-model-refit
description: Refit a gallery AR 3D model so it renders correctly on Android (SceneView/gltfio/Filament). Converts the source USDZ to a GLB, bundles it for on-device placement tuning, bakes the tuned scale/rotation/position into the GLB, compresses it, verifies, and cleans up. Use when adding a new `featured_ar` model to the Art At GVSU Android app or fixing one that renders lying-down / mis-sized / dark. Pass the object id (and optionally a slug) as args.
---

# AR model refit (Android)

Android/Filament renders a GLB with its transform **baked into the file** — unlike iOS/RealityKit,
which applies the "stand upright" −90° X rotation at runtime. A server GLB that lacks that baked
wrapper renders lying down or mis-sized. This skill takes a model from source USDZ to a
correctly-placed, uploaded Android `ar_3d_file`.

**On-device tuning requires the user + a plugged-in Pixel.** Do the mechanical steps yourself;
hand off to the user for the actual slider tuning, then resume.

## Layout

- Pipeline scripts + sources + deliverables: `~/dev/gvsucis/ar_models/` (see its `README.md`).
  - `usdz/<id>_<slug>.usdz` — working source USDZs; `script/` — refit tools; `converted/glb/` — deliverables.
- Android app: `~/dev/gvsucis/art-at-gvsu-android/`.
  - Tuner code: `app/src/main/java/edu/gvsu/art/gallery/ui/artwork/ar/ARDebugPlacement.kt` (dormant).
  - Re-enable + full detail: `docs/ar-debug-placement.md` — read this before wiring the tuner.

## Steps

1. **Convert** the source USDZ to a raw GLB (do NOT hand-edit; this reproduces the server GLB):
   ```bash
   cd ~/dev/gvsucis/ar_models
   mkdir -p /tmp/ar_in /tmp/ar_out && cp usdz/<id>_*.usdz /tmp/ar_in/
   /Applications/Blender.app/Contents/MacOS/Blender --background --factory-startup \
       --python script/convert_usdz_to_glb.py -- /tmp/ar_in /tmp/ar_out
   ```
   Inspect the result (mesh/anim/material counts, geometry-vs-texture byte split) to anticipate
   heaviness. Whole-diorama models with 200+ meshes or 200+ skin joints can ANR on load — plan to
   compress (step 6).

2. **Bundle** for on-device testing: `cp /tmp/ar_out/<file>.glb art-at-gvsu-android/app/src/main/assets/ar/<id>.glb`.

3. **Enable the tuner** by re-wiring `ARExperienceScreen.kt` per `docs/ar-debug-placement.md`
   (BUNDLED_MODELS entry, tuner state, double-tap catcher, panel, and the **single** ModelNode
   whose transform is driven by the sliders). Then build + install:
   ```bash
   cd ~/dev/gvsucis/art-at-gvsu-android && ./gradlew :app:installDebug -q
   adb shell monkey -p edu.gvsu.artmuseum -c android.intent.category.LAUNCHER 1
   ```

4. **Hand off to the user** to tune on-device: walk to the painting, double-tap for the panel,
   dial rotX (snaps 15°; start −90) / scale / tx-ty-tz, then tap "Log values (Bake)". Pull it:
   ```bash
   adb logcat -d | grep AR_BAKE   # id=<id> scale=… tx=… ty=… tz=… rotx=…
   ```

5. **Bake** the tuned transform into the GLB — transform-only, so meshes/materials/animation
   (incl. `KHR_materials_clearcoat`, which Filament renders fine) are untouched:
   ```bash
   python3 ~/dev/gvsucis/ar_models/script/refit_glb_scale.py \
       /tmp/ar_out/<file>.glb ~/dev/gvsucis/ar_models/converted/glb/<id>_<slug>.glb \
       <scale> <tx> <ty> <tz> <rotx>
   ```
   (Only use `glb_json_refit.py` instead if the model also needs material fixes, e.g. Sketchfab
   spec-gloss — see `ar_models/README.md`.)

6. **Compress** if over ~10 MB — geometry only, with **meshopt** (NOT Draco, which broke rendering
   in gltfio). Keep all mesh-destroying passes off (these are skinned + per-node-animated):
   ```bash
   npx @gltf-transform/cli optimize in.glb out.glb --compress meshopt \
       --flatten false --join false --instance false --palette false --simplify false \
       --texture-compress false
   ```

7. **Validate**: re-bundle the baked GLB and confirm it renders correctly **with the tuner off**
   (identity transform reproduces the tuned placement because it's now baked in).

8. **Upload** (the user does this in CollectiveAccess) as the object's `ar_3d_file`, then **verify
   it's live** by SHA-matching the server file against the local deliverable:
   ```bash
   url=$(curl -s "https://artgallery.gvsu.edu/admin/service.php/simple/objectDetail?id=<id>&noCache=1" \
         | python3 -c "import sys,json;print(json.load(sys.stdin)['ar_3d_file'])")
   curl -s "$url" -o /tmp/live.glb
   shasum -a256 /tmp/live.glb ~/dev/gvsucis/ar_models/converted/glb/<id>_<slug>.glb
   ```

9. **Clean up**: revert the tuner wiring in `ARExperienceScreen.kt`, empty `BUNDLED_MODELS`, delete
   the bundled `assets/ar/*.glb`, and confirm a clean build. Commit the deliverable notes.

## Notes

- **iOS needs nothing** for these — the USDZ already works on RealityKit; only the Android GLB was
  broken (missing the baked −90X). Scale/rotation/material do NOT transfer between platforms.
- **Dark model on Android** is a separate, already-fixed issue: `ARSceneView` sets
  `lightEstimationMode = DISABLED` so lit materials render on a constant IBL. Don't force materials
  unlit for that reason.
- **Known gap**: rigidly-parented (non-skinned) animated parts freeze in the GLB (Blender's USD
  importer drops their time-sampled transforms). Fix with `script/convert_usdz_xformfix.py` if a
  part is static that shouldn't be (e.g. a squirrel/beak). iOS is unaffected.
