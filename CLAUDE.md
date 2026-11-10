# Trees of Paris — agent guide

Android app showing the trees of Paris (Paris Open Data `les-arbres`) on an OpenStreetMap map.
UI language: **French**. Read `docs/adr/` for decisions and `docs/roadmap/` for ideas and plans.

## Modules

| Module | Kind | Contains | May depend on |
|--------|------|----------|---------------|
| `:app` | Android app | Hilt app, `MainActivity`, Navigation 3 host, bottom bar, wiring of implementations | everything |
| `:core:domain` | **pure JVM** | `Tree`, `GeoPoint`, `GeoBounds`, repository interfaces, use cases | coroutines, `javax.inject` only |
| `:core:data` | Android lib | Room (`TreeDatabase` from asset, `UserDatabase` for favorites), entities, mappers, repository impls | `:core:domain` |
| `:core:designsystem` | Android lib | `TreesTheme`, reusable components | — |
| `:core:map` | Android lib | provider-neutral map contract (`MapProvider`, `TreeMapView`, `MapCamera`, `CameraRequest`, `MapMarker`, `MapConfig`) | `:core:domain` |
| `:core:map-maplibre` | Android lib | MapLibre Native implementation of `MapProvider`: OpenFreeMap vector tiles, « Herbier » style (ADR 0005) | `:core:map` |
| `:feature:map` / `:feature:favorites` / `:feature:about` | Android lib | screen, scoped ViewModel, UI models, UI mappers | `:core:domain`, `:core:designsystem`, `:core:map` |
| `build-logic` | included build | convention plugins (`trees.*`) | — |
| `data/` | Python | dataset download + `trees.db` builder | — |
| `website/` | static site | arbresdeparis.fr landing page and web map (`maps/`, MapLibre GL JS), deployed to GitHub Pages (see `website/README.md`) | — |

## Rules (enforced)

- `:core:domain` has **no Android dependencies**.
- Features **never** depend on `:core:data` or `:core:map-maplibre` (`checkModuleBoundaries` fails the build).
- `org.maplibre` imports are allowed **only** in `:core:map-maplibre` (Detekt `ForbiddenImport`).
- Firebase (Crashlytics only) lives **only** in `:app`, behind `CrashReportingRepository` (ADR 0007).
  `app/google-services.json` is git-ignored; builds without it send nothing.
- New modules use the convention plugins: `trees.jvm.library`, `trees.android.library`,
  `trees.android.compose`, `trees.android.hilt`, `trees.android.feature`, `trees.android.application`.
  Add versions to `gradle/libs.versions.toml`, never inline.

## Architecture conventions

- Screen → scoped `@HiltViewModel` → use case → repository interface (domain) → implementation (data).
  ViewModels never touch repositories directly.
- One model per layer: Room entity (`*Entity`), domain model, UI model (`*UiModel`/`*Ui`). Mappers live at
  the boundary: entity↔domain in `:core:data`, domain→UI in each feature (`mapper/` package).
- ViewModels expose `StateFlow<XxxUiState>` and `fun onXxx(...)` event handlers. Composables are split into a
  `XxxRoute` (gets the ViewModel) and a stateless `XxxScreen` (previewable, testable).
- Map camera: `initialCamera` is applied once, `CameraRequest` (with a unique id) for programmatic moves,
  gestures only report through `onCameraChange`.
- The web map (`website/maps/`) mirrors the app: `style.js` copies `HerbierMapStyle.kt` and the pin colours, and
  `build_web_map.py` copies `formatArea`/`formatPlace`. Change both sides together.
- The tree schema is owned by the Room entities. `data/scripts/update_trees.py` builds `trees.db` from
  Room's exported schema JSON in `core/data/schemas/`. Never hand-edit the schema in two places.

## Commands

```bash
./gradlew assembleDebug                 # build the app
./gradlew test                          # all unit tests (JVM + Robolectric)
./gradlew :feature:map:testDebugUnitTest
./gradlew detekt spotlessCheck checkModuleBoundaries
./gradlew spotlessApply                 # auto-format
./gradlew check                         # everything CI runs on Kotlin code
python3 -I -m pytest data/tests         # data script tests
python3 data/scripts/update_trees.py    # refresh trees.db (see data/README.md)
python3 data/scripts/build_web_map.py             # web map tree files from trees.db (git-ignored)
python3 -m http.server --directory website 8000   # preview the website (map at /maps/)
```

## Workflow

- Conventional Commits (`feat(map): ...`, `fix(data): ...`), enforced by the Lefthook `commit-msg` hook.
  Scopes: module names without prefix (`domain`, `data`, `designsystem`, `map`, `favorites`, `about`, `app`),
  plus `website`, `build`, `ci`, `deps`, `release` (`chore(release): v0.2.0`, see `RELEASING.md`).
- TDD: write the failing test first. JUnit 4 + MockK + Turbine + coroutines-test; Robolectric for Android
  and Compose tests (`config/robolectric/robolectric.properties` pins the SDK).
- Toolchain: JDK 25. Bytecode target 17 (Android) / 21 (JVM).
- Android-specific guidance lives in `.claude/skills/` (from android/skills).
