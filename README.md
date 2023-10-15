# Art at GVSU Android

[![CalVer 2023.08.1008][img_version]][url_version]

## Getting Started

1. Download the latest version of Android Studio
2. Add an emulator or set up a physical device via USB debugging
3. Hit "Run 'app'" in the top toolbar.

[img_version]: https://img.shields.io/static/v1.svg?label=CalVer&message=2023.08.1008&color=blue
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
