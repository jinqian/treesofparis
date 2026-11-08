# Next steps

Detailed from the original notes (2026-11-08). Each task lists the goal, the approach in this codebase, the work,
the open decisions and when it is done. Sizes are rough: **S** ≈ half a day, **M** ≈ 1–2 days, **L** ≈ 3+ days.

| # | Task | Size | Blocks a public release? |
|---|------|------|--------------------------|
| 1 | [Map provider → MapLibre](#1-map-provider--maplibre) | L | Yes (OSM tile policy) |
| 2 | [Wikipedia links for trees](#2-wikipedia-links-for-trees) | M | No |
| 3 | [Open source libraries on their own screen](#3-open-source-libraries-on-their-own-screen) | S | No |
| 4 | [Show which data extraction the app uses](#4-show-which-data-extraction-the-app-uses) | S–M | No, but expected by ODbL users |
| 5 | [Release notes](#5-release-notes) | S | Yes, before the first store release |

Suggested order: 3 and 4 (small, independent), then 5 before any release, 2, and 1 when the design work is done.

---

## 1. Map provider → MapLibre

**Goal.** A map that matches the « Herbier » design (paper land, sage parks, soft blue Seine, no POI clutter, a real
dark map) and that does not depend on `tile.openstreetmap.org`, whose usage policy forbids heavy use by a distributed
app without permission.

**Status.** Researched and planned, parked on 2026-11-07. The full plan, with the style colour table, task list and
sources, is in [`docs/superpowers/plans/2026-11-07-maplibre-herbier-map.md`](../superpowers/plans/2026-11-07-maplibre-herbier-map.md).

**In short.**
- New `:core:map-maplibre` module implementing the existing `MapProvider` contract (ADR 0002), so features do not
  change; `:app` switches provider in one place.
- Vector tiles from OpenFreeMap to start, Protomaps PMTiles later for self-hosting and offline use.
- Own style JSON (light + dark) built from the Herbier palette; pins as GPU circle layers using `MapPalette`, which
  also fixes the « green blobs » at medium zoom (clustering, zoom-dependent radius).
- Keep osmdroid behind a debug switch until MapLibre is accepted on device, then remove it.

**Decisions to take.** Re-check versions and OpenFreeMap terms (plan's Task 0) · keep or replace the « Zoomez pour
voir les arbres » hint with clustering · self-host tiles before release or not.

**Done when.** The plan's acceptance task passes: screenshots light/dark match the canvas, attribution visible,
osmdroid removed, ADR 0005 written.

---

## 2. Wikipedia links for trees

**Goal.** From a tree's sheet, open the French Wikipedia article of its species (or its genus when the species has no
article), so curious users can read more without the app shipping long texts.

**What the data gives us.** `genus`, `species` and `cultivar` columns (1,004 distinct genus + species pairs), with
dataset quirks: `sp.` or empty species, hybrids written `x hispanica`, varieties such as
`triacanthos f. Inermis`.

**Approach (recommended): resolve links when the data is built, not in the app.**
- In `data/scripts/update_trees.py`, after cleaning, look each distinct `genus species` up on Wikidata (taxon name,
  property P225) and keep its French Wikipedia article title (`frwiki` sitelink). Fall back to the genus when the
  species is unknown or has no French article. Cache the results in a committed JSON file
  (`data/wikipedia-titles.json`) so monthly refreshes only look up new taxa and the CI job stays fast and stable.
- Store the title with the tree data. The schema is owned by Room: add a `wikipedia_title` column to `TreeEntity`
  (or a small `taxa` entity keyed by genus + species), export the schema, update `COLUMNS` in the script, rebuild.
- Domain: `Tree.wikipediaTitle: String?`. Map feature: `TreeDetailUi.wikipediaUrl: String?`
  (`https://fr.wikipedia.org/wiki/<title>`), built in `TreeDetailMapper`.
- Detail sheet: a « En savoir plus sur Wikipédia » row under the figures, shown only when a link exists, opened with
  `LocalUriHandler` (as À propos does) or a Custom Tab (`androidx.browser`, new dependency in the catalog).

**Alternative.** Build the URL in the app from the Latin name (`fr.wikipedia.org/wiki/Platanus_×_hispanica`) and rely
on Wikipedia redirects. No pipeline work, but many links will land on « page inexistante » because of the quirks above.

**Work.**
1. Script: Wikidata lookup with cache, report how many taxa resolved (species / genus / none), pytest with recorded
   responses (no network in tests).
2. Room column + schema export + data version bump; mapper and domain field; tests on the mapper.
3. Sheet row + Compose test (shown with a link, hidden without); TalkBack label « Ouvrir l'article Wikipédia ».

**Decisions to take.** Link only, or also a short offline extract (it would need CC BY-SA attribution and a licence
line in À propos) · Custom Tab or the default browser · show the link for genus-level fallbacks or only exact species.

**Done when.** Most trees (target: the 21 drawn genera at least) show a working link; trees without a match show no
row; the data README documents the lookup and its cache.

---

## 3. Open source libraries on their own screen

**Goal.** À propos stays short (app card, Données, Carte, Projet, Polices); the long list of libraries moves to a
secondary screen, as already drawn on the design canvas (« Bibliothèques open source » row with a chevron).

**Today.** `AboutRoute` reads the AboutLibraries JSON (`R.raw.aboutlibraries`, generated in `:app`) with
`produceLibraries` and `AboutScreen` appends every library to the same `LazyColumn`.

**Approach.**
- `:feature:about`: a new `LibrariesRoute`/`LibrariesScreen` (title « Bibliothèques open source », back arrow) that
  takes over the libraries loading; `AboutScreen` gets an `InfoRow` with `InfoRowTrailing.CHEVRON` and an
  `onOpenLibraries` callback, and no longer needs the libraries list.
- Navigation 3: a `LibrariesKey` pushed on the À propos back stack, so system back returns to À propos and the tab
  remembers it (`TreesNavigator` gains `openLibraries()`; `TreesApp` gets a `librariesContent` slot like the others).
- `TreesScaffold` has no back button today: add an optional `onBack` to it (design system) rather than a one-off bar.

**Work.** Move the existing library tests to the new screen · AboutTest checks the row and the callback · TreesAppTest
checks À propos → libraries → back · previews light/dark.

**Done when.** À propos fits on about one screen; the libraries screen lists every library with its licence and opens
its page; back works from the screen and from the system gesture.

---

## 4. Show which data extraction the app uses

**Goal.** Say clearly which version of « Les arbres » the app contains: when it was extracted, from where, how many
trees were kept and dropped. Users should see it in the app; maintainers should find it in the repo.

**Today.** The CSV comes from the Opendatasoft export URL in `update_trees.py`; the shipped `trees.db` was built from
the 2026-11-06 download (220,339 records read and kept). Only a data version number (`core/data/trees-db-version.txt`)
and a content hash are recorded; the extraction date and the report are printed, then lost.

**Approach.**
- Script: write a committed `core/data/trees-db-meta.json` next to the version file, with `extractedAt` (download
  time, UTC), `sourceUrl`, `datasetModifiedAt` (the dataset's « modified » date from the Opendatasoft metadata API),
  `recordsRead`, `recordsKept`, dropped counts by reason, and the data version. `--skip-download` keeps the previous
  extraction date. pytest covers the file.
- Extraction note for people: a « Data provenance » section in `data/README.md` (current extraction, what was dropped
  and why, licence ODbL), plus the report in the body of the monthly update PR (already sent by `update-data.yml`).
- App: expose the JSON through `BuildConfig` fields generated by the `:core:data` build (as `TREES_DB_VERSION` is
  today) and an `AppInfo`-like domain model (`DatasetInfo`), so no schema change is needed.
- UI: in À propos « Données », a row « Extraction du 6 novembre 2026 · 220 339 arbres » under the dataset credit;
  optionally the same line in small print at the bottom of the Herbier tab, since its figures come from it.

**Decisions to take.** Show the extraction date only, or also the dataset's own « last modified » date · put the line
in Herbier too or only in À propos.

**Done when.** After `update_trees.py` runs, the meta file changes with the data; the app shows the date and count
from it; the README explains how to read it.

---

## 5. Release notes

**Goal.** Every release says what changed, in French for users and in English for the repo, without writing it twice.

**Today.** `versionCode = 1`, `versionName = "0.1.0"`, no tags, no changelog. Commits follow Conventional Commits
(checked by Lefthook and the `pr-title` workflow), which is enough to generate a changelog.

**Approach.**
- `CHANGELOG.md` at the root, generated from commits between tags with git-cliff (config `cliff.toml`, grouping
  `feat` → « Added », `fix` → « Fixed », skipping `build`/`ci`/`deps` noise). Run it locally or in a release workflow.
- User-facing notes in French for the store: `fastlane/metadata/android/fr-FR/changelogs/<versionCode>.txt`
  (Google Play limit: 500 characters), written by hand from the changelog, a few lines per release.
- Versioning rule written in the README: `versionName` follows SemVer (`0.y.z` until the first public release),
  `versionCode` goes up by one per release; tag `v<versionName>` on `main`.
- Later, optional: an in-app « Nouveautés » sheet shown once after an update (last seen `versionCode` in DataStore).

**Work.** Add `cliff.toml` and generate the first `CHANGELOG.md` (Herbier redesign, leaves, Herbier tab…) · write the
first French store note · document the release steps (bump, changelog, tag) in the README or a `RELEASING.md`.

**Decisions to take.** Will the app go to Google Play (then fastlane metadata is worth it) or only GitHub releases
(then the changelog in the release body is enough) · do we want the in-app « Nouveautés » sheet.

**Done when.** Tagging a release produces an up-to-date `CHANGELOG.md`, and each store release has its French note.
