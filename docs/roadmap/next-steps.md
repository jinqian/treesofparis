# Next steps

Detailed from the original notes (2026-11-08). Each task lists the goal, the approach in this codebase, the work,
the open decisions and when it is done. Sizes are rough: **S** ≈ half a day, **M** ≈ 1–2 days, **L** ≈ 3+ days.

| # | Task | Size | Status | Blocks a public release? |
|---|------|------|--------|--------------------------|
| 1 | [Map provider → MapLibre](#1-map-provider--maplibre) | L | To do (planned, parked) | Yes (OSM tile policy) |
| 2 | [Wikipedia links for trees](#2-wikipedia-links-for-trees) | M | To do | No |
| 3 | [Release notes](#3-release-notes) | S | **Done** (2026-11-08), two follow-ups | Yes, before the first store release |

Next: 2, then 1 when the design work is done. Before the first store release: the follow-ups of task 3 (signing,
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

## 3. Release notes

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
