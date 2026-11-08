# Trees of Paris

[![CI](https://github.com/jinqian/treesofparis/actions/workflows/ci.yml/badge.svg)](https://github.com/jinqian/treesofparis/actions/workflows/ci.yml)

An Android app to explore the trees of Paris on an OpenStreetMap map.

- **Carte**: the trees around you, in Paris and in the sites the City of Paris manages nearby (cemeteries of Pantin, Thiais, Ivry…). Tap a tree to see its details and add it to your favorites.
- **Favoris**: the trees you saved (stored only on your device).
- **À propos**: about the app, its data and the open-source projects it uses.

Tree data: [Paris Open Data, « Les arbres »](https://opendata.paris.fr/explore/dataset/les-arbres/information/)
(ODbL). Map: © [OpenStreetMap](https://www.openstreetmap.org/copyright) contributors, rendered with osmdroid.

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
| Refresh tree data | see [`data/README.md`](data/README.md) |

## Project layout

```
app/                    application, navigation, wiring
core/domain/            pure Kotlin: models, repository interfaces, use cases
core/data/              Room databases, repositories
core/designsystem/      theme and reusable components
core/map/               provider-neutral map contract
core/map-osmdroid/      osmdroid implementation of the map contract
feature/{map,favorites,about}/
build-logic/            Gradle convention plugins
data/                   dataset update script
docs/                   spec, plan, architecture decision records
```

## Continuous integration

GitHub Actions (`.github/workflows/`):

| Workflow | When | What |
|----------|------|------|
| `ci.yml` | pull requests, pushes to `main` | Spotless, Detekt, module boundaries, unit tests, Android lint, debug APK, data script tests, dependency review |
| `pr-title.yml` | pull requests | PR title follows Conventional Commits (it becomes the squash commit message) |
| `update-data.yml` | monthly, or manually | Rebuilds `trees.db` from Paris Open Data and opens a PR when the data changed |

Dependabot keeps Gradle, GitHub Actions and Python dev dependencies up to date.

## Contributing

Commits follow [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)
(`feat(map): show remarkable trees`). The `commit-msg` hook checks this locally and CI checks PR titles.

Commit subjects become the changelog: [`CHANGELOG.md`](CHANGELOG.md) is generated from them, and
[`RELEASING.md`](RELEASING.md) describes versioning, release notes and tagging.

## License

[MIT](LICENSE). Vendored agent skills in `.claude/skills/` are Apache 2.0 (Google LLC).
