# Trees of Paris Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the Trees of Paris Android app (map, favorites, about), its data pipeline and the full project tooling.

**Architecture:** Multi-module clean architecture. A pure-JVM `core:domain` defines models, repository interfaces and use cases. `core:data` implements them with Room, using a pre-packaged tree database. Features use scoped Hilt ViewModels that call use cases only. The map is a provider-neutral contract (`core:map`) with an osmdroid implementation (`core:map-osmdroid`), wired in `app`.

**Tech Stack:** Kotlin, Jetpack Compose + Navigation, Hilt (KSP), Room, osmdroid, Detekt, Spotless/ktlint, Lefthook, JUnit, MockK, Turbine, Robolectric, Python 3 + pytest, GitHub Actions. Gradle Kotlin DSL, JDK 25 toolchain.

**Spec:** `docs/superpowers/specs/2026-11-06-trees-of-paris-design.md`

## Global Constraints

- Root package `com.treesofparis`; module names exactly as in spec §4 (`:app`, `:core:domain`, `:core:data`, `:core:designsystem`, `:core:map`, `:core:map-osmdroid`, `:feature:map`, `:feature:favorites`, `:feature:about`, `build-logic`).
- UI strings in French; tab labels "Carte", "Favoris", "À propos"; empty-zoom message "Zoomez pour voir les arbres".
- `core:domain` is a pure JVM module with no Android dependencies. Nothing outside `core:map-osmdroid` imports `org.osmdroid`. Features never depend on `core:data` or `core:map-osmdroid`.
- Toolchain JDK 25; bytecode target 17 for Android modules, 21 for JVM modules. Versions are the latest stable at implementation time, checked against JDK 25 (Gradle 9.1+, Kotlin 2.2.20+). `compileSdk`/`targetSdk` 37, `minSdk` 26.
- Version catalog, convention plugins in `build-logic`, configuration cache and build cache on, KSP not kapt.
- Favorites are stored separately from the packaged tree DB and survive data refreshes.
- Conventional Commits for every commit. Work on a feature branch, not `main`.
- All dataset trees are kept, including the ~25k the City manages outside Paris. Map restriction and the "user is in the area" check use `GeoBounds.COVERAGE = GeoBounds(south = 48.735, west = 2.200, north = 48.920, east = 2.480)`. The default camera is `GeoPoint.PARIS_CENTER = GeoPoint(48.8566, 2.3522)`.
- Marker cap `MAX_MARKERS = 1500`; minimum tree zoom `MIN_TREE_ZOOM = 16.0`; camera debounce 300 ms. Markers are drawn with osmdroid's fast point overlay, not one `Marker` object each.
- The Room entities are the single source of truth for the tree schema: the data script builds `trees.db` from Room's exported schema JSON, never from a hand-written schema.
- The tree data has a version (`PRAGMA user_version`, written by the script to `core/data/trees-db-version.txt`). `TreeDatabase.version` reads it at build time, and a higher version makes Room re-copy the asset (destructive fallback on the tree DB only).
- Feature module boundaries are enforced by a Gradle check in `build-logic` (features may not depend on `:core:data` or `:core:map-osmdroid`) and by Detekt `ForbiddenImport` for `org.osmdroid`.
- "© OpenStreetMap contributors" is visible on the map itself (osmdroid copyright overlay) as well as on À propos.

## Review Focus

- Location permission denied, or user located outside the coverage area: map shows the centre of Paris, no crash (Task 9). A user in a Paris-managed cemetery outside the city (e.g. Pantin) is inside the area and gets centred.
- Favorited tree id no longer in the refreshed tree DB: favorites list skips it, no crash (Task 6).
- Tree records with missing optional fields (no species, height, circumference): detail shows a French placeholder, not "null" (Task 9).
- Data script run twice, or with duplicate ids / `0,0` / out-of-Paris coordinates: idempotent output, bad rows dropped, count reported (Task 5).
- Rotation or process recreation on the map screen keeps the camera position (Task 9).
- Height or circumference of `0` in the dataset means unknown: stored as NULL and shown as a placeholder, not "0 m" (Task 6).
- An app update with newer tree data replaces the old trees on existing installs and keeps favorites (Task 5).

---

### Task 1: Gradle skeleton and JDK 25 check

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`, `gradle.properties`, `.gitignore`, `build-logic/**` (convention plugins `trees.android.library`, `trees.android.compose`, `trees.android.hilt`, `trees.jvm.library`, `trees.android.application`), a `build.gradle.kts` per module with one placeholder class each.

**Interfaces:**
- Produces: all modules from spec §4 compiling, with the convention plugin ids above applied.

- [ ] **Step 1:** Generate the Gradle wrapper (9.1+), write `settings.gradle.kts` including every module, and the version catalog.
- [ ] **Step 2:** Implement the convention plugins: toolchain 25, targets per Global Constraints, Compose, Hilt+KSP, Room schema export where relevant.
- [ ] **Step 3:** Add a placeholder test per module type (JVM, Android library with Robolectric, Hilt) so each tool runs on JDK 25.
- [ ] **Step 4:** Run `./gradlew assembleDebug testDebugUnitTest`. Expected: BUILD SUCCESSFUL. If a tool fails on JDK 25 with no fix, fall back to JDK 21 for that tool only and record `docs/adr/0001-jdk-toolchain.md`.
- [ ] **Step 5:** Commit `build: add multi-module gradle skeleton`.

_Done. Later tasks also add: a `trees.android.feature` convention plugin (library + compose + hilt + `:core:domain`, `:core:designsystem`, `:core:map`, hilt-navigation-compose, lifecycle), Robolectric SDK pinned for every module via convention, `javax.inject` on `:core:domain`, and the module-boundary check `checkModuleBoundaries` wired into `check` (Task 2)._

### Task 2: Quality tooling and git hooks

**Files:**
- Create: `config/detekt/detekt.yml`, `.editorconfig`, `lefthook.yml`, `scripts/commit-msg-check.sh`, `scripts/commit-msg-check_test.sh`
- Modify: root `build.gradle.kts`, convention plugins (Detekt + Spotless on every module)

- [ ] **Step 1:** Write `commit-msg-check_test.sh` asserting: `feat: add map` passes, `feat(map)!: x` passes, `added stuff` fails, an empty message fails.
- [ ] **Step 2:** Run it; expect FAIL. Implement `commit-msg-check.sh` (Conventional Commits 1.0.0 regex; types feat, fix, docs, style, refactor, perf, test, build, ci, chore, revert). Re-run; expect PASS.
- [ ] **Step 3:** Configure Detekt (Compose rules plugin, `ForbiddenImport` for `org.osmdroid.*` outside `core/map-osmdroid`) and Spotless/ktlint; run `./gradlew detekt spotlessCheck`. Expected: pass on the skeleton.
- [ ] **Step 3b:** Add `checkModuleBoundaries` to `build-logic` (fails when a `:feature:*` project declares a dependency on `:core:data` or `:core:map-osmdroid`). Verify it fails with a temporary bad dependency in `:feature:about`, then remove it. Add the `trees.android.feature` convention plugin and the Robolectric SDK convention.
- [ ] **Step 4:** Add `lefthook.yml` (pre-commit: spotlessCheck + detekt; commit-msg: the script) and run `lefthook install`. Verify a bad commit message is rejected.
- [ ] **Step 5:** Commit `chore: add detekt, spotless and commit hooks`.

### Task 3: Agent setup and project docs

**Files:**
- Create: `CLAUDE.md` (and `AGENTS.md` pointing to it), `.claude/skills/**` (relevant skills from https://github.com/android/skills), `README.md`, `LICENSE`, `docs/adr/0002-map-provider-abstraction.md`, `docs/adr/0003-clean-architecture-layers.md`, `local.properties.example`
- Modify: `.gitignore` (`local.properties`)

- [ ] **Step 1:** Fetch the android/skills repository into a scratch directory outside the repo, choose the skills relevant to Compose, Hilt, Room, navigation and testing, and copy them into `.claude/skills/` with their licence notice.
- [ ] **Step 2:** Write `CLAUDE.md`: module map, dependency rules (Global Constraints), commands (build, test, detekt, spotless, data update), commit convention, and the "no `org.osmdroid` outside `core:map-osmdroid`" rule.
- [ ] **Step 3:** Write `README.md` (setup: JDK 25, `ANDROID_HOME`/`local.properties`, hooks, data refresh) and the two ADRs from spec §5–§6.
- [ ] **Step 4:** Commit `docs: add agent setup, readme and adrs`.

### Task 4: Domain models and use cases

**Files:**
- Create under `core/domain/src/main/kotlin/com/treesofparis/domain/`: `model/GeoPoint.kt`, `model/GeoBounds.kt`, `model/Tree.kt`, `repository/TreeRepository.kt`, `repository/FavoritesRepository.kt`, `usecase/*.kt`
- Test: `core/domain/src/test/kotlin/com/treesofparis/domain/**`

**Interfaces:**
- Produces:
  - `data class GeoPoint(val lat: Double, val lon: Double)`
  - `data class GeoPoint(...) { companion object { val PARIS_CENTER: GeoPoint } }`
  - `data class GeoBounds(val south: Double, val west: Double, val north: Double, val east: Double) { val center: GeoPoint; fun contains(p: GeoPoint): Boolean; companion object { val COVERAGE: GeoBounds } }`
  - `data class Tree(val id: Long, val commonName: String, val genus: String?, val species: String?, val cultivar: String?, val circumferenceCm: Int?, val heightM: Int?, val stage: String?, val remarkable: Boolean, val address: String, val arrondissement: String, val location: GeoPoint)`
  - `interface TreeRepository { suspend fun getTreesInBounds(bounds: GeoBounds, limit: Int): List<Tree>; suspend fun getTree(id: Long): Tree?; suspend fun getTreesByIds(ids: List<Long>): List<Tree> }`
  - `interface FavoritesRepository { fun observeFavoriteIds(): Flow<List<Long>>; fun observeIsFavorite(id: Long): Flow<Boolean>; suspend fun toggle(id: Long) }`
  - `interface LocationRepository { suspend fun currentLocation(): GeoPoint? }` (null when unavailable or permission missing)
  - Use cases with `operator fun invoke`: `GetTreesInBounds(bounds, limit)`, `GetTree(id)`, `ObserveFavorites(): Flow<List<Tree>>` (combines favorite ids with `getTreesByIds`, preserving favorite order, skipping missing trees), `ToggleFavorite(id)`, `ObserveIsFavorite(id)`, `GetUserLocationInCoverage(): GeoPoint?` (returns the location only if inside `GeoBounds.COVERAGE`). Use cases have `@Inject constructor` (`javax.inject` dependency on `:core:domain`).

- [ ] **Step 1:** Write failing tests: `GeoBoundsTest.coverage_containsCentralParis`, `coverage_containsPantinAndThiaisCemeteries`, `coverage_excludesLondon`; `ObserveFavoritesTest.skipsIdsMissingFromTreeRepository`, `emitsAgainWhenFavoritesChange` (fakes + Turbine); `ToggleFavoriteTest.delegatesToRepository`; `GetUserLocationInCoverageTest.insideParis_returnsLocation`, `inPantinCemetery_returnsLocation`, `outsideCoverage_returnsNull`, `unavailable_returnsNull`.
- [ ] **Step 2:** Run `./gradlew :core:domain:test`; expect FAIL.
- [ ] **Step 3:** Implement the types and use cases above with the `COVERAGE` and `PARIS_CENTER` values from Global Constraints.

_Done before revision 3 with `GeoBounds.PARIS` (city box) and `GetUserLocationInParis`; revision 3 replaces them in a follow-up `refactor(domain)` commit at the start of Task 6, tests first._
- [ ] **Step 4:** Run the tests; expect PASS. Commit `feat(domain): add models, repositories and use cases`.

### Task 5: Data layer (Room)

**Files:**
- Create under `core/data/`: `db/TreeEntity.kt`, `db/TreeDao.kt`, `db/TreeDatabase.kt`, `db/FavoriteEntity.kt`, `db/FavoriteDao.kt`, `db/UserDatabase.kt`, `mapper/TreeMapper.kt`, `repository/TreeRepositoryImpl.kt`, `repository/FavoritesRepositoryImpl.kt`, `location/LocationRepositoryImpl.kt`, `di/DataModule.kt`, `trees-db-version.txt` (initially `1`), `schemas/**` (Room export, committed)
- Modify: `core/data/build.gradle.kts` (Room plugin, `schemaDirectory("$projectDir/schemas")`, `buildConfigField("int", "TREES_DB_VERSION", <contents of trees-db-version.txt>)`)
- Test: `core/data/src/test/**` (Robolectric)

**Interfaces:**
- Consumes: Task 4 repository interfaces.
- Produces:
  - `@Entity(tableName = "trees", indices = [Index("lat", "lon")]) data class TreeEntity(@PrimaryKey val id: Long, val commonName: String, val genus: String?, val species: String?, val cultivar: String?, val circumferenceCm: Int?, val heightM: Int?, val stage: String?, val remarkable: Boolean, val address: String, val arrondissement: String, val lat: Double, val lon: Double)` with `@ColumnInfo` snake_case names (`common_name`, `circumference_cm`, ...).
  - `TreeEntity.toDomain(): Tree`; `TreeDatabase` built with `createFromAsset("trees.db")` + `fallbackToDestructiveMigration(dropAllTables = true)`, `@Database(version = BuildConfig.TREES_DB_VERSION)` (a Java `static final int`, so a valid annotation constant; enable `buildFeatures.buildConfig`).
  - `FavoriteEntity(@PrimaryKey treeId: Long, addedAt: Long)` in its own `UserDatabase` (`user.db`), favorites ordered by `addedAt DESC`.
  - `LocationRepositoryImpl` (platform `LocationManager`, `getCurrentLocation` on API 30+, last known location below) implementing Task 4's `LocationRepository`.
  - Hilt bindings for `TreeRepository`, `FavoritesRepository`, `LocationRepository`.
  - Room schema JSON at `core/data/schemas/com.treesofparis.core.data.db.TreeDatabase/<version>.json` — Task 6 reads it.

- [ ] **Step 1:** Write failing tests: `TreeMapperTest.mapsNullableFields`, `TreeDaoTest.inBounds_returnsOnlyTreesInside`, `inBounds_respectsLimit`, `getByIds_ignoresUnknownIds`, `FavoritesRepositoryTest.toggleAddsThenRemoves`, `favorites_orderedByMostRecent`.
- [ ] **Step 2:** Run `./gradlew :core:data:testDebugUnitTest`; expect FAIL.
- [ ] **Step 3:** Implement entities, DAOs, mappers, repositories, DI. DAO tests use in-memory databases seeded with a few entities. Build once so Room exports the schema JSON; commit it.
- [ ] **Step 4:** Run tests; expect PASS. Commit `feat(data): add room databases and repositories`.

### Task 6: Data update script

**Files:**
- Create: `data/scripts/update_trees.py`, `data/tests/test_update_trees.py`, `data/tests/fixtures/sample.csv`, `data/README.md`
- Modify: `.gitignore` (`data/raw/`), `core/data/trees-db-version.txt`
- Test (Android): `core/data/src/test/.../PrepackagedDatabaseTest.kt`

**Interfaces:**
- Consumes: Room schema JSON from Task 5 (`createSql` for the `trees` table and its indices, plus `identityHash`).
- Produces: `core/data/src/main/assets/trees.db` whose schema is exactly Room's, with `PRAGMA user_version` set to the version in `trees-db-version.txt`. Functions `download(dest: Path) -> Path`, `clean(rows) -> tuple[list[dict], Report]`, `build_db(rows, schema_json: Path, out: Path, version: int) -> None`. Flag `--bump-version` increments `trees-db-version.txt`, which flows into `@Database` through `BuildConfig`.

- [ ] **Step 1:** Fetch a small sample from the live dataset (`les-arbres`, Paris Open Data export API) to confirm the real field names, and save a trimmed `sample.csv` fixture (real header, UTF-8 BOM, `;` delimiter) including a duplicate id, a `0,0` row, an unparsable coordinate, a row outside the coverage area (Lyon), real rows from Seine-Saint-Denis and Val-de-Marne, a row with empty species, a row with height `0`, a row with no `libellefrancais` and a `Jeune (arbre)Adulte` stage.
- [ ] **Step 2:** Write failing tests: `test_clean_drops_invalid_coordinates`, `test_clean_keeps_trees_managed_outside_paris`, `test_clean_drops_rows_outside_coverage`, `test_clean_common_name_falls_back_to_genus`, `test_clean_normalizes_stage`, `test_clean_deduplicates_ids`, `test_clean_keeps_rows_with_missing_optional_fields`, `test_clean_zero_height_and_circumference_become_null`, `test_build_db_uses_room_schema` (table SQL equals the JSON's `createSql`), `test_build_db_sets_user_version`, `test_build_db_is_idempotent`, `test_report_counts_dropped_rows`.
- [ ] **Step 3:** Run `python3 -I -m pytest data/tests`; expect FAIL. Implement the script (standard library only; `--output`, `--skip-download`, `--bump-version`; checksum in `data/raw/`; fails loudly if expected fields or the schema JSON are missing).
- [ ] **Step 4:** Run tests; expect PASS. Run the script against the live dataset; expect `trees.db` created and a printed report. Report the file size to the user.
- [ ] **Step 5:** Write failing Robolectric tests: `PrepackagedDatabaseTest.prepackagedAsset_opensWithoutSchemaMismatch` (open `TreeDatabase` from the real asset and query one tree) and `newAssetVersion_replacesOldData_keepsFavorites` (open with an older seeded copy at a lower version, reopen with the asset, assert new data and untouched `UserDatabase`). Run; expect FAIL until wiring is right, then PASS.
- [ ] **Step 6:** Commit `feat(data): add tree dataset update script`; commit `trees.db` separately as `chore(data): add packaged tree database`.

### Task 7: Design system

**Files:**
- Create under `core/designsystem/`: `theme/TreesTheme.kt`, `theme/Color.kt`, `theme/Type.kt`, `component/TreesScaffold.kt`, `component/EmptyState.kt`, `component/LoadingState.kt`, `component/ErrorState.kt`, `component/TreeListItem.kt`, `component/SectionHeader.kt`, `component/TreesBottomSheet.kt`
- Test: `core/designsystem/src/test/**` (screenshot-free; Robolectric Compose tests)

**Interfaces:**
- Produces: `@Composable fun TreesTheme(content)`; `EmptyState(message: String, modifier)`, `LoadingState(modifier)`, `ErrorState(message: String, onRetry: (() -> Unit)?, modifier)`, `TreeListItem(title: String, subtitle: String, onClick: () -> Unit, modifier)`, `SectionHeader(text: String, modifier)`.

- [ ] **Step 1:** Write failing Compose tests: `EmptyState_showsMessage`, `ErrorState_retryButtonInvokesCallback`, `TreeListItem_click`.
- [ ] **Step 2:** Run; expect FAIL. Implement the green Material 3 theme and the components.
- [ ] **Step 3:** Run tests; expect PASS. Commit `feat(designsystem): add theme and reusable components`.

### Task 8: Map contract and osmdroid implementation

**Files:**
- Create: `core/map/.../MapCamera.kt`, `MapMarker.kt`, `MapConfig.kt`, `MapProvider.kt`, `TreeMapView.kt`; `core/map-osmdroid/.../OsmdroidMapProvider.kt`
- Test: `core/map-osmdroid/src/test/**`

**Interfaces:**
- Consumes: `GeoPoint`, `GeoBounds` from Task 4.
- Produces:
  - `data class MapCamera(val center: GeoPoint, val zoom: Double)`, `data class CameraRequest(val id: Long, val camera: MapCamera)`, `data class MapMarker(val id: Long, val position: GeoPoint, val selected: Boolean = false)`, `data class MapConfig(val bounds: GeoBounds, val minZoom: Double, val maxZoom: Double)`
  - `interface MapProvider { @Composable fun Content(initialCamera: MapCamera, cameraRequest: CameraRequest?, markers: List<MapMarker>, userLocation: GeoPoint?, config: MapConfig, onMarkerClick: (Long) -> Unit, onCameraChange: (MapCamera, GeoBounds) -> Unit, modifier: Modifier) }`
  - Camera rule: `initialCamera` is applied once when the view is created; a `cameraRequest` is applied once per distinct `id` (programmatic moves); user gestures only report through `onCameraChange` and never come back in as input. This prevents feedback loops.
  - `val LocalMapProvider: ProvidableCompositionLocal<MapProvider>` and `@Composable fun TreeMapView(...)` (same parameters) that delegates to `LocalMapProvider.current`.
  - `class OsmdroidMapProvider @Inject constructor() : MapProvider`.

This refines spec §6: the composable lives in `core:map` and delegates through a `CompositionLocal`, and `app` provides the implementation (the single wiring point).

- [ ] **Step 1:** Keep the osmdroid glue thin; put decisions in pure, testable functions in `core:map-osmdroid`: `fun MapConfig.applyTo(mapView: MapView)`, `class CameraRequestTracker { fun shouldApply(request: CameraRequest?): Boolean }`, `fun markerIdAt(index: Int, markers: List<MapMarker>): Long?`. Write failing tests: `applyTo_setsScrollLimitsAndZoomLimits` (Robolectric `MapView`), `cameraRequestTracker_appliesEachIdOnce`, `cameraRequestTracker_nullIsIgnored`, `markerIdAt_mapsPointIndexToId`.
- [ ] **Step 2:** Run; expect FAIL. Implement with `AndroidView` + `MapView`: OSM tile source with user agent = application id; `SimpleFastPointOverlay` for markers (updated in place, not recreating the view); a separate overlay for `userLocation`; `CopyrightOverlay` ("© OpenStreetMap contributors"); lifecycle `onResume/onPause` via `LocalLifecycleOwner`, `onDetach` on dispose; `onCameraChange` reported on scroll/zoom end.
- [ ] **Step 3:** Run tests; expect PASS. Confirm `./gradlew detekt checkModuleBoundaries` still passes.
- [ ] **Step 4:** Commit `feat(map): add provider-neutral map contract and osmdroid implementation`.

### Task 9: Map feature

**Files:**
- Create under `feature/map/`: `MapUiState.kt`, `MapViewModel.kt`, `TreeUiModel.kt`, `TreeDetailUi.kt`, `mapper/TreeUiMapper.kt`, `MapScreen.kt`, `TreeDetailSheet.kt`, `LocationPermission.kt`
- Test: `feature/map/src/test/**`

**Interfaces:**
- Consumes: use cases from Task 4, `MapCamera`/`MapMarker`/`MapConfig`, `TreeMapView`, design-system components.
- Produces: `@HiltViewModel class MapViewModel(GetTreesInBounds, GetTree, ObserveIsFavorite, ToggleFavorite, GetUserLocationInParis, SavedStateHandle)` with `val uiState: StateFlow<MapUiState>` (holds `initialCamera`, `cameraRequest`, `markers`, `userLocation`, `showZoomHint`, `selectedTree: TreeDetailUi?`) and `fun onCameraChange(camera: MapCamera, bounds: GeoBounds)`, `fun onMarkerClick(id: Long)`, `fun onDetailDismissed()`, `fun onFavoriteToggle()`, `fun onLocationPermissionResult(granted: Boolean)`; `fun Tree.toDetailUi(): TreeDetailUi`; `@Composable fun MapRoute(modifier)`.
- Navigation input: optional route argument `treeId: Long?`. When present (opened from Favoris), the ViewModel loads that tree, issues a `cameraRequest` centred on it at zoom 18 and opens its detail sheet.

- [ ] **Step 1:** Write failing tests (fake use-case repositories, Turbine, test dispatcher): `onCameraChange_belowMinZoom_showsZoomHintAndLoadsNoTrees`, `onCameraChange_debouncesAndCapsMarkers`, `permissionGranted_insideCoverage_issuesCameraRequestToUser`, `permissionGranted_outsideCoverage_keepsParisCentre`, `permissionDenied_keepsParisCentre`, `treeIdArgument_centresOnTreeAndOpensDetail`, `onMarkerClick_loadsDetail`, `onFavoriteToggle_updatesIsFavorite`, `camera_survivesRecreationViaSavedStateHandle`, `toDetailUi_missingFields_usesFrenchPlaceholder` (e.g. "Espèce inconnue", "Hauteur inconnue"), `toDetailUi_formatsArea` ("PARIS 19E ARRDT" → "Paris 19e", "PARIS 1ER ARRDT" → "Paris 1er", "SEINE-SAINT-DENIS" → "Seine-Saint-Denis", "BOIS DE BOULOGNE" → "Bois de Boulogne").
- [ ] **Step 2:** Run `./gradlew :feature:map:testDebugUnitTest`; expect FAIL.
- [ ] **Step 3:** Implement the ViewModel (debounce 300 ms, `MAX_MARKERS`, `MIN_TREE_ZOOM`), UI mappers, the screen (map + zoom hint + detail sheet with favorite toggle) and the permission request (Compose permission handling, fine location; denied means the Paris centre).
- [ ] **Step 4:** Run tests; expect PASS. Commit `feat(map): add map screen with tree details`.

### Task 10: Favorites and About features

**Files:**
- Create: `feature/favorites/.../FavoritesViewModel.kt`, `FavoritesScreen.kt`, `FavoriteUiModel.kt`; `feature/about/.../AboutScreen.kt`, `AboutViewModel.kt`, `LibraryInfo.kt`
- Test: `feature/favorites/src/test/**`, `feature/about/src/test/**`

**Interfaces:**
- Produces: `@HiltViewModel class FavoritesViewModel(ObserveFavorites, ToggleFavorite)` exposing `uiState: StateFlow<FavoritesUiState>` (`Loading | Empty | Content(items)`) and `fun onRemove(id: Long)`; `@Composable fun FavoritesRoute(onTreeClick: (Long) -> Unit, modifier)`, `AboutRoute(modifier)`; the About screen lists app version, Paris Open Data (ODbL) credit, "© OpenStreetMap contributors" and the libraries used, generated by the AboutLibraries Gradle plugin (`com.mikepenz.aboutlibraries.plugin`, latest stable) rather than a hand-written list.

- [ ] **Step 1:** Write failing tests: `FavoritesViewModelTest.emptyList_emitsEmpty`, `favorites_emitContentInOrder`, `onRemove_togglesFavorite`; `FavoritesScreen_itemClick_reportsTreeId` (Compose); `AboutScreen_showsOsmAttribution` (Compose).
- [ ] **Step 2:** Run; expect FAIL. Implement both features using design-system components (empty state: "Aucun favori pour le moment").
- [ ] **Step 3:** Run tests; expect PASS. Commit `feat(favorites,about): add favorites and about screens`.

### Task 11: App wiring and navigation

**Files:**
- Create: `app/.../TreesApplication.kt` (`@HiltAndroidApp`), `MainActivity.kt`, `navigation/TreesDestination.kt`, `navigation/TreesNavHost.kt`, `TreesApp.kt`; manifest with location + internet permissions
- Test: `app/src/test/**`

**Interfaces:**
- Consumes: the three `*Route` composables, `OsmdroidMapProvider`.
- Produces: type-safe destinations `Map(treeId: Long? = null)`, `Favorites`, `About` (labels "Carte", "Favoris", "À propos"); tapping a favorite navigates to `Map(treeId)` and selects the Carte tab; `TreesApp` provides `LocalMapProvider` with `OsmdroidMapProvider` and shows the bottom navigation bar.

- [ ] **Step 1:** Write failing Robolectric Compose tests: `bottomBar_showsThreeFrenchTabs`, `selectingFavoris_showsFavoritesScreen`, `favoriteClick_navigatesToCarteTab`.
- [ ] **Step 2:** Run; expect FAIL. Implement navigation (single top, state restoration) and the application and activity.
- [ ] **Step 3:** Run `./gradlew assembleDebug test detekt spotlessCheck`; expect all pass. If an emulator is available, install and launch the app to smoke-test; otherwise state that the app was not run on a device.
- [ ] **Step 4:** Commit `feat(app): wire hilt, navigation and bottom bar`.

### Task 12: CI and repository automation

**Files:**
- Create: `.github/workflows/ci.yml`, `.github/workflows/pr-title.yml`, `.github/workflows/update-data.yml` (scheduled, manual dispatch; opens a PR when `trees.db` changes), `.github/dependabot.yml`, `.github/pull_request_template.md`

- [ ] **Step 1:** Write `ci.yml`: JDK 25 (Temurin), Gradle cache, run `spotlessCheck detekt lintDebug testDebugUnitTest assembleDebug`, and `pytest` for `data/`; upload reports as artifacts.
- [ ] **Step 2:** Write `pr-title.yml` enforcing Conventional Commits, Dependabot for Gradle and Actions, and the data-update workflow.
- [ ] **Step 3:** Validate the YAML (`actionlint` if available). The workflows cannot run until the repo is pushed, so say that in the hand-off.
- [ ] **Step 4:** Commit `ci: add github actions workflows and dependabot`.

---

## Self-review notes

- Spec coverage: §2 tabs (Tasks 9–11), §4 modules (1), §5 layers (4, 5, 9), §6 map (8, 9), §7 data (6, with versioned refresh in 5–6), §8 design system (7), §9 tooling/CI/agent setup (2, 3, 12), §10 testing (in each task), §11 environment (1).
- Deviation from spec §6: the map composable delegates through `LocalMapProvider` (Task 8) because a composable cannot be implemented by another module otherwise. Still one wiring point, in `app`.
- Revision 2 (after Task 1, model switch): data versioning, schema single source of truth (Task 5/6 order swapped), camera request model, location repository, fast point overlay, map attribution, module boundary check, AboutLibraries, favorite → map navigation. Spec §6 R-tree option dropped (Room can't use it).
- Revision 3 (user request): keep the ~25k trees managed outside Paris; `GeoBounds.PARIS` → `GeoBounds.COVERAGE` (extent of all trees + margin), `GeoPoint.PARIS_CENTER` for the default camera, `GetUserLocationInParis` → `GetUserLocationInCoverage`.
