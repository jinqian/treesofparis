# Trees of Paris — agent guide

« Arbres de Paris »: the trees of Paris and its neighbouring towns (Paris Open Data `les-arbres` and « Les arbres
remarquables », Grand Paris Seine Ouest, the Hauts-de-Seine's remarkable trees; ADR 0008–0010) on an OpenStreetMap map. Two products share one dataset:
- **The Android app** (`fr.arbresdeparis`, closed test on Google Play), four tabs: **Carte** (tree pins and groups,
  genus and remarkable filters, tree sheet with flowering and Wikipedia, remarkable trees' stories and photos, nearest
  tree, location), **Herbier** (statistics, the 21 drawn genera, flowering calendar, remarkable trees, ages and
  areas), **Favoris**, **À propos** (sources, appearance, crash-report consent).
- **The website** arbresdeparis.fr: landing page, web map (`/maps/`), Herbier page (`/herbier/`), privacy policy.

UI language: **French**. Read `docs/adr/` for decisions and `docs/roadmap/` for ideas and plans.

The Android app lives in `android/` (one Gradle build: run `./gradlew` there, open it in Android Studio). The
repository root holds what is shared: `data/` (builds `trees.db` and the website's generated files), `website/`,
`docs/`, `scripts/`, `.github/`.

## Modules

| Module | Kind | Contains | May depend on |
|--------|------|----------|---------------|
| `:app` | Android app | Hilt app, `MainActivity`, Navigation 3 host, bottom bar, wiring of implementations | everything |
| `:core:domain` | **pure JVM** | `Tree`, `GeoPoint`, `GeoBounds`, repository interfaces, use cases | coroutines, `javax.inject` only |
| `:core:data` | Android lib | Room (`TreeDatabase` from asset, `UserDatabase` for favorites), entities, mappers, repository impls | `:core:domain` |
| `:core:designsystem` | Android lib | `TreesTheme`, colours, type, spacing, icons, leaf drawings, reusable components (see « Design system ») | — |
| `:core:map` | Android lib | provider-neutral map contract (`MapProvider`, `TreeMapView`, `MapCamera`, `CameraRequest`, `MapMarker`, `MapConfig`) | `:core:domain` |
| `:core:map-maplibre` | Android lib | MapLibre Native implementation of `MapProvider`: OpenFreeMap vector tiles, « Herbier » style (ADR 0005) | `:core:map` |
| `:feature:map` / `:feature:herbier` / `:feature:favorites` / `:feature:about` | Android lib | one tab each: screens, scoped ViewModel, UI models, UI mappers | `:core:domain`, `:core:designsystem`, `:core:map` |
| `build-logic` | included build | convention plugins (`trees.*`) | — |
| `android/fastlane/` | store listing | Google Play texts, images, French release notes (`changelogs/<versionCode>.txt`) | — |
| `data/` | Python (standard library) | dataset download + `trees.db` builder (`update_trees.py`), the web map's files (`build_web_map.py`), the Herbier page (`build_herbier.py`, template in `data/templates/`) | — |
| `website/` | static site | arbresdeparis.fr: landing page, web map (`maps/`, MapLibre GL JS), Herbier page (`herbier/`), privacy policy; deployed to GitHub Pages (see `website/README.md`) | — |

## Rules (enforced)

- `:core:domain` has **no Android dependencies**.
- Features **never** depend on `:core:data` or `:core:map-maplibre` (`checkModuleBoundaries` fails the build).
- `org.maplibre` imports are allowed **only** in `:core:map-maplibre` (Detekt `ForbiddenImport`).
- Firebase (Crashlytics only) lives **only** in `:app`, behind `CrashReportingRepository` (ADR 0007).
  `android/app/google-services.json` is git-ignored; builds without it send nothing.
- New modules use the convention plugins: `trees.jvm.library`, `trees.android.library`,
  `trees.android.compose`, `trees.android.hilt`, `trees.android.feature`, `trees.android.application`.
  Add versions to `android/gradle/libs.versions.toml`, never inline.

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
  `build_web_map.py` copies `formatArea`/`formatPlace` and `storyTeaser`. Change both sides together. The Herbier page
  (`website/herbier/`) is written by `data/scripts/build_herbier.py` from `data/templates/herbier.html`; it mirrors
  the app's Herbier tab (`TreeStatisticsDao`, `HerbierUiMapper`) and reads the leaf drawings, genus names and flowering
  months straight from the app's sources.
- The website is indexed by search engines: `website/robots.txt` and `website/sitemap.xml` (Google Search Console,
  domain property, sitemap submitted, in the owner's account). A new page under `website/` goes in `sitemap.xml`
  (with a title, a description and a canonical URL); one that must not be indexed gets `<meta name="robots"
  content="noindex">`, like `404.html`.
- The tree schema is owned by the Room entities. `data/scripts/update_trees.py` builds `trees.db` from
  Room's exported schema JSON in `android/core/data/schemas/`. Never hand-edit the schema in two places.

## Design system

The « Herbier » look: botanical, calm, minimal. Paper backgrounds, forest ink, moss green, and **gold only for
remarkable trees**. It lives in `:core:designsystem`; features use its tokens and components, never raw colours, raw
dp values or an icon library. A substantial visual change is designed on the « Herbier » design canvas first (ask
the owner for the link), then built.

- **Colours** (`theme/`): `HerbierPalette` holds the raw values, light and night (« Crépuscule », a warm olive dark
  theme). Screens read them through Material 3 `MaterialTheme.colorScheme` (mapped in `TreesTheme`) and, for what
  Material has no slot for, `TreesTheme.colors` (`TreesColors`): `remarkable`, `treePin`, `pinRing`, `userLocation`,
  `treeGroup`, `bloomShowy`, `bloomDiscreet`, `bloomNone`, `currentMonth`. No dynamic colour. The user picks light,
  dark or the phone's setting in À propos.
- **Type** (`Type.kt`): Newsreader (serif) for what you read about a tree (names, screen titles, figures), Inter (sans)
  for the interface. `displaySmall` is the screen title and the tree name; `labelMedium` the small uppercase eyebrow.
- **Spacing and shapes**: `TreesSpacing` (`xs` 4, `s` 8, `m` 16, `l` 24, `xl` 32 dp); rounded shapes from 8 dp
  (`extraSmall`) to 28 dp (`extraLarge`), pills and dots `CircleShape`.
- **Icons** (`icon/`): `TreesIcons`, thin rounded line icons on a 24 dp grid, tinted by `Icon`; `TreesIllustrations`
  (the sprig of empty states); `TreesIcons.AppLogo`, the « Goutte » tree logo (launcher, splash, website, store).
- **Leaves** (`leaf/`): `TreeLeaf` maps the 21 drawn genera (about 85 % of the trees) to hand-drawn leaves in
  `TreeLeafPaths.kt` (exported from the canvas; do not hand-edit), drawn by `LeafDrawing`; other genera get `GENERIC`.
  The website reads these files too.
- **Components** (`component/`): `TreesScaffold`/`ScreenHeader`, `TreesCardGroup` with `InfoRow`/`SwitchRow`/
  `ChoiceRow` and `TreesDivider`, `TreeListItem`/`LeafBadge`, `SectionHeader`, `TreesBottomSheet`, `LoadingState`,
  `EmptyState`, `ErrorState`. Reuse them before writing a new one; a component used by two features moves here.
- **Clickable surfaces**: put `clickable`/`toggleable` *inside* a shaped `Surface` (or use its `onClick` overload),
  never on the modifier passed to it, which sits outside the clip: the ripple would be rectangular (#81).
- **The website** uses the same palette as CSS variables (`website/styles.css`, light and dark), the same fonts
  (self-hosted subsets in `website/fonts/`) and the same leaves; the web map's style copies `HerbierMapStyle.kt`.
  Change a colour on both sides.

## Commands

```bash
cd android                              # every ./gradlew command runs here
./gradlew assembleDebug                 # build the app
./gradlew test                          # all unit tests (JVM + Robolectric)
./gradlew :feature:map:testDebugUnitTest
./gradlew recordRoborazziDebug         # re-record every screenshot (each feature's src/test/screenshots/)
./gradlew testDebugUnitTest -Proborazzi.test.verify=true   # what CI runs: tests + screenshot comparison
./gradlew detekt spotlessCheck checkModuleBoundaries
./gradlew spotlessApply                 # auto-format
./gradlew check                         # everything CI runs on Kotlin code
cd ..                                   # the rest runs from the repository root
python3 -I -m pytest data/tests         # data script tests
python3 data/scripts/update_trees.py    # refresh trees.db (see data/README.md)
python3 data/scripts/build_web_map.py             # web map tree files from trees.db (git-ignored)
python3 data/scripts/build_herbier.py             # the Herbier page from trees.db (git-ignored)
python3 -m http.server --directory website 8000   # preview the website (map at /maps/)
```

## Keeping the app, the website and the screenshots in step

The app and the website show the same trees; a change on one side usually needs the other. Check these before
opening a PR, and say in the PR what was updated (or why not).

- **Data update** (a new `trees.db`: the monthly `chore(data)` PR from `.github/workflows/update-data.yml`, or
  `update_trees.py` run by hand). Update **both** products:
  - *App*: the database ships only with the next release; a `data` commit counts toward one (`release` skill).
  - *Website*: the Pages workflow rebuilds the map's files and the Herbier page from `trees.db` on merge, but some
    figures are written by hand. Update them in the same PR: in `website/index.html` the description and
    `og:description`, the hero paragraph and the « En chiffres » figures (trees, genera, remarkable trees), and in
    `website/maps/index.html` the description, `og:description` and the `#count` fallback. Get them with
    `sqlite3 android/core/data/src/main/assets/trees.db "SELECT COUNT(*), COUNT(DISTINCT genus), SUM(remarkable) FROM trees"`.
    The app-screenshot alt text in `index.html` quotes the Herbier's figures (Paris only): refresh it with the
    screenshot (next point). Then build and preview the site (`build_web_map.py`, `build_herbier.py`).
- **App release** (`chore(release)`, `release` skill). Update the website's app screenshots, `website/images/
  app-herbier.jpg` and `app-fiche-arbre.jpg` (debug build on an emulator, demo-mode status bar, 490 × 1100 JPEG, see
  `website/README.md`), whenever the screens they show changed, and their `alt` texts. The Google Play screenshots
  (`android/fastlane/metadata/android/fr-FR/images/phoneScreenshots/`) are not uploaded by CI: refresh them and
  upload them in the Play Console by hand. A new user-facing feature also belongs in the landing page's feature list.
- **Visual change in the app** (anything a screen draws: layout, colour, text, icon). Re-record the screenshot tests
  (`./gradlew recordRoborazziDebug`), look at every changed PNG in `src/test/screenshots/` to check that only what
  you meant changed, and commit the images **in the same commit** as the code: CI compares them pixel for pixel and
  fails otherwise. A new screen, sheet or state gets its own case in the feature's `<Feature>ScreenshotTest`, and a
  new map control or sheet one in `SmallScreenScreenshotTest` (320 dp, 130 % text). If the colour or style changed,
  change the website too (« Design system »).

## Workflow

- Conventional Commits (`feat(map): ...`, `fix(data): ...`), enforced by the Lefthook `commit-msg` hook.
  Scopes: module names without prefix (`domain`, `data`, `designsystem`, `map`, `herbier`, `favorites`, `about`,
  `app`), plus `website`, `build`, `ci`, `deps`, `release` (`chore(release): v0.2.0`, see `android/RELEASING.md`).
- Branch from `main` and open a PR: `main` needs a PR with green checks (CI, data tests, Conventional Commits title,
  Claude review). PRs are merged with a merge commit, keeping each commit. Release tags `v*` are immutable.
- TDD: write the failing test first. JUnit 4 + MockK + Turbine + coroutines-test; Robolectric for Android
  and Compose tests (`android/config/robolectric/robolectric.properties` pins the SDK).
- Screenshots (Roborazzi, set up by `trees.android.feature`): each feature has a `<Feature>ScreenshotTest` with its
  screens on a 411 dp phone, light and dark (`+night`). CI compares them pixel for pixel with the reference images in
  `src/test/screenshots/`; a visual change fails until you record them again and commit the new images with it (see
  « Keeping the app, the website and the screenshots in step »). Robolectric draws no ripple or animation.
- Small screens: `SmallScreenScreenshotTest` (:feature:map) checks that no text is cut or splits a word at 320 dp and
  at 130 % text size. Add a case there for any new map control or sheet.
- Toolchain: JDK 25. Bytecode target 17 (Android) / 21 (JVM).
- Android-specific guidance lives in `.claude/skills/` (from android/skills). Releases: the `release` skill, which
  follows `android/RELEASING.md`.
