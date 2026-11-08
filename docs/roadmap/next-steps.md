# Next steps

Detailed from the original notes (2026-11-08). Each task lists the goal, the approach in this codebase, the work,
the open decisions and when it is done. Sizes are rough: **S** ≈ half a day, **M** ≈ 1–2 days, **L** ≈ 3+ days.

| # | Task | Size | Status | Blocks a public release? |
|---|------|------|--------|--------------------------|
| 1 | [Map provider → MapLibre](#1-map-provider--maplibre) | L | To do (planned, parked) | Yes (OSM tile policy) |
| 2 | [Wikipedia links for trees](#2-wikipedia-links-for-trees) | M | **Done** (2026-11-08) | No |
| 3 | [Release notes](#3-release-notes) | S | **Done** (2026-11-08), two follow-ups | Yes, before the first store release |

Next: 1 when the design work is done. Before the first store release: the follow-ups of task 3 (signing,
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

**Status: done** — commits `feat(data): link each species to its French Wikipedia article` and
`feat(map): open the tree's French Wikipedia article from its sheet`. Design: option A of the « Fiche arbre — lien
Wikipédia » row on the canvas.

**What was built.**
- `update_trees.py` looks every genus + species up on Wikidata (taxon name, P225) and keeps the French article,
  falling back to the plant genus's article. Answers, including « no article », are cached in
  `data/wikipedia-titles.json`; failed lookups are logged and retried next run, never failing the build.
- The links ship in a new `taxon_links` table (Room schema 2, data version 2), about one row per species: 91 % of
  trees get their species article, 8 % their genus article, 0.6 % none.
- `TreeRepository.getTree` returns the article; the sheet shows a « En savoir plus sur Wikipédia » card with the
  article title (« Le genre … sur Wikipédia » for a genus fallback), opened in a Custom Tab (`androidx.browser`).

**Decisions taken.** Link only, no offline extract (no CC BY-SA text in the app) · Custom Tab, falling back to the
default browser · generic wording plus the article title, to avoid French elision (« le platane », « l'érable »).

**Follow-ups (optional).** Show the same link in the Herbier tab's genus rows · re-check the cached « no article »
answers once a year (delete them from the cache to look them up again).

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
