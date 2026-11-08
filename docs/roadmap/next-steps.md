# Next steps

Detailed from the original notes (2026-11-08). Each task lists the goal, the approach in this codebase, the work,
the open decisions and when it is done. Sizes are rough: **S** ≈ half a day, **M** ≈ 1–2 days, **L** ≈ 3+ days.

| # | Task | Size | Status | Blocks a public release? |
|---|------|------|--------|--------------------------|
| 1 | [Map provider → MapLibre](#1-map-provider--maplibre) | L | To do (planned, parked) | Yes (OSM tile policy) |
| 2 | [Wikipedia links for trees](#2-wikipedia-links-for-trees) | M | To do | No |
| 3 | [Open source libraries on their own screen](#3-open-source-libraries-on-their-own-screen) | S | **Done** (2026-11-08) | No |
| 4 | [Show which data extraction the app uses](#4-show-which-data-extraction-the-app-uses) | S–M | **Done** (2026-11-08) | No, but expected by ODbL users |
| 5 | [Release notes](#5-release-notes) | S | **Done** (2026-11-08), two follow-ups | Yes, before the first store release |

Next: 2, then 1 when the design work is done. Before the first store release: the follow-ups of task 5 (signing,
store target).

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

**Status: done** — commit `feat(about): move the open source libraries to their own screen`.

**What was built.**
- À propos keeps a « Bibliothèques open source » row (chevron) in the Projet card; the libraries list moved to
  `LibrariesScreen`/`LibrariesRoute` in `:feature:about`, which now owns the AboutLibraries loading (with a loading
  state).
- Navigation 3: `LibrariesKey` is pushed on the À propos back stack by `TreesNavigator.openLibraries()`, so the back
  button and the system back gesture both return to À propos, which keeps its scroll position.
- Design system: `TreesScaffold`/`ScreenHeader` take an optional `onBack` and show a « Retour » back arrow above
  the title; `TreesIcons.Back` added.
- Tests: scaffold back button (shown / absent), À propos row → callback, libraries list + link + back, app navigation
  À propos → libraries → back (button and system back).

**Follow-ups.** None.

---

## 4. Show which data extraction the app uses

**Status: done** — commit `feat(data): record and show which dataset extraction the app ships`.

**What was built.**
- `data/scripts/update_trees.py` writes `core/data/trees-db-meta.json` on every run: `extractedAt` (download time of
  the CSV, UTC), `datasetModifiedAt` (the dataset's « modified » date from the Opendatasoft metadata API, kept on
  `--skip-download`; a failed fetch leaves it empty without failing the build), `recordsRead`, `recordsKept`,
  `dropped` by reason, `dataVersion`, `source`, `sourceExport`, `license`. pytest covers each piece.
- The monthly `update-data.yml` workflow commits the meta file with the data.
- `:core:data` reads the file at build time into `BuildConfig` (`TREES_EXTRACTED_AT`, `TREES_RECORDS_KEPT`) and
  provides a domain `DatasetInfo`; no Room schema change.
- À propos shows « Extraction du 6 novembre 2026 · 220 339 arbres » under the dataset credit (date in Paris time).
- `data/README.md` has a « Data provenance » section describing every field and the current extraction.

**Current extraction.** Downloaded 2026-11-06 16:30 UTC; dataset last modified 2026-11-06 09:34 UTC (checked: the
API's record count still matches); 220,339 records read and kept.

**Decisions taken.** The app shows the extraction date and tree count only; the dataset's own date stays in the meta
file and README. The line is in À propos only, not in Herbier (easy to add there later from `DatasetInfo`).

---

## 5. Release notes

**Status: done** — commit `docs: changelog, French release note and releasing guide`.

**What was built.**
- `cliff.toml` + generated `CHANGELOG.md`: Conventional Commits grouped as Added / Fixed / Changed / Documentation;
  `build`, `ci`, `chore`, `test` and merge commits are left out. Everything so far is under « Unreleased ».
- First user-facing note, in French: `fastlane/metadata/android/fr-FR/changelogs/1.txt` (408 of the 500 characters
  Google Play allows), in the layout fastlane `supply` and F-Droid read.
- `RELEASING.md`: SemVer `versionName` (`0.y.z` until the first public release), `versionCode` + 1 per release,
  `v<versionName>` tags on `main`, and the step-by-step release (bump, French note, `git cliff --tag`, `chore(release)`
  commit, tag). README and `CLAUDE.md` (new `release` scope) point to it.

**Follow-ups (decisions still open).**
- **Store target.** Google Play, F-Droid or GitHub releases only? The French note's location works for the first two;
  for GitHub only, paste the changelog section into the release body instead.
- **Release signing.** There is no signing config yet; `bundleRelease` needs one (keystore outside the repo, secrets in
  CI) before any store upload.
- **Release automation (optional).** A workflow on `v*` tags that builds the bundle and creates the GitHub release with
  the changelog section.
- **In-app « Nouveautés » sheet (optional).** Show the French note once after an update (last seen `versionCode` in
  DataStore). Not started.
