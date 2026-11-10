# Trees of Paris

[![CI](https://github.com/jinqian/treesofparis/actions/workflows/ci.yml/badge.svg)](https://github.com/jinqian/treesofparis/actions/workflows/ci.yml)

**Arbres de Paris**, an Android app to explore the trees of Paris on a map: [arbresdeparis.fr](https://arbresdeparis.fr).

- **Carte**: the 220,339 trees the City of Paris manages, in Paris and in its sites nearby (cemeteries of Pantin,
  Thiais, Ivry…), on a calm « Herbier » map, light or dark. Show only the remarkable trees or one genus
  (« Toutes les essences »), find the tree closest to you, and tap a tree for its sheet: height, girth, flowering
  calendar, Wikipédia and favorite.
- **Herbier**: the most planted genera with their leaf drawings, flowering through the year, the remarkable trees,
  ages and arrondissements.
- **Favoris**: the trees you saved, stored only on your device (and in Android's backup).
- **À propos**: contact, data sources and credits, the open-source projects the app uses.

## Credits

- **Trees:** [Paris Open Data, « Les arbres »](https://opendata.paris.fr/explore/dataset/les-arbres/information/)
  (Ville de Paris, ODbL).
- **Flowering months:** the species sheets of the
  [« Guide des Essences de Paris »](https://opendata.paris.fr/explore/dataset/fiches-essences-du-guide-des-essences-de-paris/information/)
  (Ville de Paris, Cerema, 2023, ODbL), listed in `GuideFloweringRepository` and in the app under À propos.
- **Map:** vector tiles from [OpenFreeMap](https://openfreemap.org), in the
  [OpenMapTiles](https://www.openmaptiles.org/) schema, with data © [OpenStreetMap](https://www.openstreetmap.org/copyright)
  contributors (ODbL); drawn with [MapLibre Native](https://maplibre.org) in our own « Herbier » style (ADR 0005).
- **Encyclopedia links:** [Wikipédia](https://fr.wikipedia.org), titles found through [Wikidata](https://www.wikidata.org).
- **Fonts:** [Newsreader](https://github.com/productiontype/Newsreader) and [Inter](https://github.com/rsms/inter)
  (SIL Open Font License 1.1).
- The other open-source libraries are listed in the app (À propos → Bibliothèques open source).

This is a playground project for AI-assisted ("vibe") coding. `CLAUDE.md` describes the architecture and
rules for agents and humans alike.

## Requirements

- JDK 25 (the Gradle toolchain is pinned to 25)
- Android SDK with platform 37 (`compileSdk`/`targetSdk` 37, `minSdk` 26)
- Python 3.11+ (only to refresh the tree data)
- [Lefthook](https://lefthook.dev) for git hooks: `brew install lefthook`

## Setup

```bash
git clone git@github.com:jinqian/treesofparis.git && cd treesofparis
cp local.properties.example local.properties   # then set sdk.dir, or export ANDROID_HOME
lefthook install                                # pre-commit + commit-msg hooks
./gradlew assembleDebug
```

Open the project in Android Studio and run the `app` configuration on an emulator or device.

## Everyday commands

| Task | Command |
|------|---------|
| Build | `./gradlew assembleDebug` |
| Unit tests | `./gradlew test` |
| Static analysis + format check | `./gradlew detekt spotlessCheck checkModuleBoundaries` |
| Auto-format | `./gradlew spotlessApply` |
| Data script tests | `python3 -I -m pytest data/tests` |
| Preview the website | `python3 -m http.server --directory website 8000` |
| Refresh tree data | see [`data/README.md`](data/README.md) |

## Project layout

```
app/                    application, navigation, wiring
core/domain/            pure Kotlin: models, repository interfaces, use cases
core/data/              Room databases, repositories
core/designsystem/      theme and reusable components
core/map/               provider-neutral map contract
core/map-maplibre/      MapLibre implementation: OpenFreeMap vector tiles, « Herbier » map style
feature/{map,herbier,favorites,about}/
build-logic/            Gradle convention plugins
data/                   dataset update script
website/                arbresdeparis.fr, published with GitHub Pages
docs/                   spec, plans, architecture decision records, roadmap
```

## Continuous integration

GitHub Actions (`.github/workflows/`):

| Workflow | When | What |
|----------|------|------|
| `ci.yml` | pull requests, pushes to `main` | Spotless, Detekt, module boundaries, unit tests, Android lint, debug APK, data script tests, dependency review |
| `pr-title.yml` | pull requests | PR title follows Conventional Commits (it becomes the squash commit message) |
| `update-data.yml` | monthly, or manually | Rebuilds `trees.db` from Paris Open Data and opens a PR when the data changed |
| `pages.yml` | pushes to `main` that touch `website/`, or manually | Publishes the website to GitHub Pages (see [`website/README.md`](website/README.md)) |

Dependabot keeps Gradle, GitHub Actions and Python dev dependencies up to date.

## Contributing

Commits follow [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)
(`feat(map): show remarkable trees`). The `commit-msg` hook checks this locally and CI checks PR titles.

Commit subjects become the changelog: [`CHANGELOG.md`](CHANGELOG.md) is generated from them, and
[`RELEASING.md`](RELEASING.md) describes versioning, release notes and tagging.

## License

[MIT](LICENSE). Vendored agent skills in `.claude/skills/` are Apache 2.0 (Google LLC).
