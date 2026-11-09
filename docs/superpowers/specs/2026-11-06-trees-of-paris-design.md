# Trees of Paris — Design Spec

Date: 2026-11-06
Status: Draft, awaiting review

## 1. Purpose

A playground project for trying AI tools in a real "vibe coding" workflow. The product is an Android app that
shows the trees of Paris (Paris Open Data, dataset `les-arbres`) on an OpenStreetMap map. Success means both a
working MVP and a well-set-up project: clean architecture, multi-module Gradle, quality tooling, CI and
agent-friendly documentation.

## 2. Scope

### MVP app (UI in French)

Bottom navigation with three tabs:

- **Carte**: OSM map restricted to the dataset's coverage area: Paris plus the sites the City of Paris manages in
  the neighbouring departments (cemeteries of Pantin, Saint-Ouen, Thiais, Ivry, Bagneux, etc.). Asks for location
  permission and centres on the user when they are inside the coverage area. Shows trees for the visible area. Pinch-zoom and pan. Tapping a tree opens a detail bottom sheet
  with a favorite toggle.
- **Favoris**: list of favorited trees. Favorites are fully local.
- **À propos**: app information, data source credit (Paris Open Data, ODbL), OpenStreetMap attribution and the
  open-source libraries used.

### Out of scope (YAGNI)

Accounts, remote sync, search, offline tile packaging, tree photos, iOS, tablet-specific layouts.

## 3. Assumptions

- The map library is osmdroid (raster OSM tiles, no API key). The provider must be swappable (see §6).
  _Since 2026-11-09: MapLibre Native with OpenFreeMap vector tiles, `:core:map-maplibre` (ADR 0005)._
- The tree data is pre-packaged in the APK as a Room database built by a Python script.
- The dataset holds about 220k trees (2026-11: 220,339). About 195k are in Paris and the two Bois; about 25k are
  in Hauts-de-Seine, Seine-Saint-Denis and Val-de-Marne (sites managed by the city). **All of them are kept.** The
  map loads by bounding box and caps the number of markers.
- Coverage area (`GeoBounds.COVERAGE`): the extent of all trees plus a small margin,
  S 48.735 / W 2.200 / N 48.920 / E 2.480 (data extent 2026-11: S 48.7423, W 2.2102, N 48.9122, E 2.4698).
- OSM's tile usage policy is acceptable for a playground. A published app would need its own tile source.
- Dataset field names will be verified against the live dataset during implementation.

## 4. Module layout

```
app                      Hilt wiring, navigation host, bottom bar
core:domain (JVM)        Tree, GeoPoint, GeoBounds, use cases, repository interfaces
core:data                Room DB, entities, mappers, repository implementations
core:designsystem        theme, typography, reusable components
core:map                 provider-neutral map contract (Compose)
core:map-osmdroid        osmdroid implementation of the contract
feature:map             map screen, ViewModel, UI models, mappers
feature:favorites       favorites screen, ViewModel
feature:about           about screen
build-logic              convention plugins
data/                    Python update script, raw data (git-ignored), tests
```

Rules:

- `core:domain` is a pure JVM module with no Android dependencies.
- Features depend on `core:domain`, `core:designsystem` and `core:map`, never on `core:data` or
  `core:map-osmdroid`. Only `app` wires implementations through Hilt.
- Nothing outside `core:map-osmdroid` imports `org.osmdroid`. This is enforced by a Gradle dependency rule and
  noted in `CLAUDE.md`.
- `GeoPoint` and `GeoBounds` live in `core:domain`. UI-only map types (`MapMarker`, `MapCamera`, `MapConfig`)
  live in `core:map`.

## 5. Architecture

Clean architecture with distinct models per layer and mappers at each boundary:

| Layer    | Models                                | Location                            |
|----------|---------------------------------------|-------------------------------------|
| DTO      | raw record produced by the data script | `data/`, `core:data` if needed      |
| Entity   | `TreeEntity`, `FavoriteEntity`         | `core:data`                         |
| Domain   | `Tree`, `GeoPoint`, `GeoBounds`        | `core:domain`                       |
| UI       | `TreeUiModel`, `TreeDetailUi`          | each feature module                 |

- Mappers: entity↔domain in `core:data`, domain↔UI in each feature.
- Use cases: `GetTreesInBounds`, `GetTree`, `ObserveFavorites`, `ToggleFavorite`, `ObserveIsFavorite`.
- Repository interfaces are in `core:domain`. Implementations are in `core:data`.
- Each screen has a scoped `@HiltViewModel` exposing `StateFlow<UiState>` and handling UI events. ViewModels
  call use cases only, never repositories.
- Favorites are stored separately from the packaged tree data (a user-data table or database), so a data
  refresh never erases them.
- Navigation uses Jetpack Compose Navigation with type-safe routes.

## 6. Map abstraction

`core:map` exposes a declarative, state-driven contract:

- Types: `MapCamera`, `MapMarker`, `MapConfig` (bounds restriction, min/max zoom), `MapEvent`.
- One composable, `TreeMapView(camera, markers, config, onMarkerClick, onCameraChange)`.
- The ViewModel holds camera and markers as plain data. `core:map-osmdroid` implements the composable with
  `AndroidView` + `MapView` and enforces bounds and zoom limits.
- Swapping providers means adding a new module that implements the same contract and changing one Hilt/wiring
  binding. Feature code is unchanged.
- Contract types stay minimal and grow only when a feature needs them.

### Map data flow

1. Camera changes update the visible bounds in the ViewModel.
2. The bounds are debounced and passed to `GetTreesInBounds`, which queries Room with a bounding-box query
   (index or R-tree) and a result cap.
3. Results map to `MapMarker`s. Below a minimum zoom the screen shows "Zoomez pour voir les arbres" instead of
   markers.
4. Tapping a marker loads the tree and opens the detail bottom sheet with a favorite toggle.

Location: the permission is requested with the Compose permission APIs. The map centres on the user only if
they are inside the coverage area. If permission is denied or the user is outside it, the map shows the centre of
Paris (`GeoPoint.PARIS_CENTER`, 48.8566 N / 2.3522 E).

## 7. Data pipeline

- `data/scripts/update_trees.py`: Python 3, standard library only.
  1. Downloads the full export from the Paris Open Data API into `data/raw/` (git-ignored) and records a
     checksum.
  2. Cleans records: keeps every tree, including those managed outside the city. Drops only rows with missing or
     invalid coordinates, or outside the coverage area (reported, so a new far-away site is noticed and the bounds
     updated). Normalizes fields (0 height/circumference = unknown, common name falls back to the genus).
  3. Writes a SQLite file matching the Room schema exactly, with a spatial index, to
     `core/data/src/main/assets/trees.db`. Room loads it with `createFromAsset`.
- Tests: pytest, using a small fixture sample.
- `data/README.md` documents how to refresh the data.
- Optional later: a scheduled GitHub Action that runs the script and opens a PR when the data changes.

## 8. Design system

`core:designsystem` provides a Material 3 theme (colour scheme with a green tree-inspired palette, typography,
shapes), reusable components (screen scaffold, empty state, loading and error states, list item, section header,
bottom sheet wrapper) and preview annotations. Features use only these components for common UI.

## 9. Tooling and workflow

- **Build**: Gradle Kotlin DSL, version catalog, `build-logic` convention plugins, configuration cache and
  build cache enabled, KSP instead of kapt, JVM modules wherever possible.
- **Static analysis**: Detekt (with the Compose rules plugin) and Spotless/ktlint for formatting.
- **Git hooks**: Lefthook runs Detekt and format checks on pre-commit and a Conventional Commits check on
  commit-msg.
- **CI (GitHub Actions)**: on pull requests run Detekt, formatting check, lint, unit tests and assemble with
  Gradle caching. Also a PR title check (Conventional Commits), Dependabot and dependency review.
- **Agent setup**: relevant skills from https://github.com/android/skills are placed under `.claude/skills/`.
  `CLAUDE.md` / `AGENTS.md` document the architecture rules, module boundaries, commands and commit
  conventions.
- **Other practices**: `.editorconfig`, `.gitignore`, CODEOWNERS-free to start, LICENSE, a README with setup
  steps, and an ADR folder (`docs/adr/`) starting with the decisions in this spec.

## 10. Testing

- Frameworks: JUnit, MockK, Turbine, kotlinx-coroutines-test.
- Domain use cases and mappers: plain JVM unit tests.
- ViewModels: tests with fake repositories.
- Room DAOs: Robolectric (or instrumented) tests, including the bounding-box query.
- Data script: pytest.
- Compose UI tests are added only for the critical flows, after the MVP works.

## 11. Verification limits

The local Android SDK is at `ANDROID_HOME=~/Library/Android/sdk` (set in `~/.zshrc`). It has platform
`android-37.0`, build-tools `36.0.0`, platform-tools, an emulator and system images. It has no `cmdline-tools`
(so no `sdkmanager`). Consequences:

- `compileSdk`/`targetSdk` are set to the installed platform (37) to avoid downloads. `minSdk` is chosen
  separately (see the plan).
- The Gradle build, Detekt and unit tests run locally. AGP may auto-download missing SDK components if the
  licenses in `$ANDROID_HOME/licenses` are accepted. If it can't, we install `cmdline-tools`.
- The Gradle toolchain is pinned to **JDK 25** (the machine default; CI uses Temurin 25). This needs recent
  versions of Gradle (9.1+), Kotlin (2.2.20+) and AGP. Bytecode targets stay lower than the toolchain
  (Java/Kotlin target 17 for Android modules, 21 for the JVM domain module, set in the convention plugins) so
  the output stays compatible with Android.
- JDK 25 is a risk for tools that lag behind new JDKs (Detekt, Spotless/ktlint, Robolectric, KSP/Hilt). The first
  plan step is a minimal build that exercises each tool on JDK 25. If one fails and has no fix, only that part
  falls back to JDK 21 (installed locally via Homebrew), and the change is recorded in an ADR.
- A non-interactive shell does not read `~/.zshrc`, so the repo also provides `local.properties` (git-ignored)
  with `sdk.dir`, and the README documents this.
- CI is the second check.

## 12. Risks

- **Marker volume**: 200k trees could overload osmdroid. Mitigated by the bounding-box query, the result cap and
  the minimum zoom. Clustering is a follow-up if needed.
- **Tile policy**: OSM's public tile servers are not meant for heavy production use. Fine for a playground.
- **Dataset schema changes**: the script validates expected fields and fails loudly.
