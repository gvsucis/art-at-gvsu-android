# Art at GVSU Android

![Tests](https://github.com/gvsucis/art-at-gvsu-ios/actions/workflows/ci.yml/badge.svg) [![CalVer 2025.07.1023-beta][img_version]][url_version]

## Download

<a href="https://play.google.com/store/apps/details?id=edu.gvsu.artmuseum">
  <img src="./site/play-badge.png" width="150px" />
</a>

## Getting Started

1. Download the latest version of Android Studio
1. Copy over the project properties and replace the `art_gallery_base_url` key with the correct value
    ```sh
    cp project-sample.properties project.properties
    ```
1. Add an emulator or set up a physical device via USB debugging
1. Hit "Run 'app'" in the top toolbar.

[img_version]: https://img.shields.io/static/v1.svg?label=CalVer&message=2025.07.1023-beta&color=blue
[url_version]: https://github.com/gvsucis/art-at-gvsu-android

## Directory Structure

```
.
├── app
│   └── src
│       ├── main
│       └── edu.gvsu.art.gallery
│           ├── di # Dependency Injection
│           ├── extensions # Kotlin extensions for Android and Jetpack Compose
│           |── lib # Helpers for networking and media
│           └── ui # Jetpack Compose UI components
├── artgalleryclient
│   └── src
│       └── edu.gvsu.art.client
│           ├── api # Collective Access HTTP Client and JSON data access objects
│           ├── common # Helpers and extensions
│           └── repository # Adapters for network and database objects
```

## Release

### Beta

The beta version of the app will automatically build on new commits to the main branch. Beta builds are available via Firebase App Distribution.

### Play Store

Play Store releases are handled by the GitHub Action called "Deploy Production."

To create a new release, first add a changelog for the next version by running the following command

```shell
make changelog
```

Which will produce the following output

```
$ make changelog
Added changelog for next build (1009)
./fastlane/metadata/android/en-US/changelogs/1009.txt
```

Edit the file with a description of the important changes made since the last release. On commit, add the `[skip ci]` GitHub tag to the commit message to avoid
rebuilding a beta version of the app that has not changed.
