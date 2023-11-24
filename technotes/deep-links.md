# Deep Links

Deep Links are handled via the [Jetpack Compose framework](https://developer.android.com/training/app-links/deep-linking) in conjunction with an Android [intent filter](https://developer.android.com/guide/topics/manifest/intent-filter-element) on the Main Activity.

There are a few links that the Gallery app will respond to for artists and artworks. They can be tested by
either opening up a link on the device or by running the following command via adb.


Artist example

- <https://artgallery.gvsu.edu/Detail/entities/637>
- adb command
  ```
  adb shell am start -W -a android.intent.action.VIEW -d https://artgallery.gvsu.edu/Detail/entities/637
  ```

Artwork

- <https://artgallery.gvsu.edu/Detail/objects/3818>
- adb command
  ```
  adb shell am start -W -a android.intent.action.VIEW -d https://artgallery.gvsu.edu/Detail/objects/3818
  ```



