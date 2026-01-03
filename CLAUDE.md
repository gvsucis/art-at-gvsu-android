# CLAUDE.md

## Build and Development

- `./gradlew assembleDebug` will compile the debug version of the app
- For fast feedback, run single tests i.e. `./gradlew :artgalleryclient:testDebugUnitTest --tests edu.gvsu.art.client.ArtworkTest` replacing the module - `:artgalleryclient` - and Java package accordingly
- `make test` will run all tests via Fastlane.

## Project Architecture

Art At GVSU is an Android art gallery application for browsing the online collection, search, and tours split into several gradle modules

### Key Gradle Modules

- artgalleryclient: Core application with an HTTP client, SQLite database, and data modelling
- app: Android UI built in Jetpack Compose

## Code Style

- When naming accessors, prefer "artworks" over `getArtworks` unless there's a parameter, in which case use "get"
- Prefer explicit named parameters when passing arguments to Jetpack Compose functions over positional arguments.
