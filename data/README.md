# Tree data

The app ships open tree datasets as a pre-packaged SQLite database, `android/core/data/src/main/assets/trees.db` (ADR 0008):

| Source | Trees | Licence | Module |
|--------|-------|---------|--------|
| [« Les arbres », Ville de Paris](https://opendata.paris.fr/explore/dataset/les-arbres/information/) | 220,339 | ODbL | `update_trees.py` |
| [« Les arbres remarquables », Ville de Paris](https://opendata.paris.fr/explore/dataset/arbresremarquablesparis/information/) | 181 stories (table `remarkable_trees`, ADR 0009) | ODbL | `remarkable.py` |
| [« Arbres », Grand Paris Seine Ouest](https://data.seineouest.fr/explore/dataset/arbres-v2/information/) (8 towns) | 32,636 | Licence Ouverte 2.0 | `gpso.py` |

`scripts/update_trees.py` downloads them, cleans them, gives every tree its town (`city`) and its dataset
(`source`), and builds that file. It uses only the Python standard library (Python 3.10+). Other towns that publish
tree data, and the plan to add them: `docs/roadmap/grand-paris-trees.md`.

## Refreshing the data

```bash
python3 data/scripts/update_trees.py                  # download + build
python3 data/scripts/update_trees.py --skip-download  # rebuild from data/raw/les-arbres.csv and gpso-arbres.csv
python3 data/scripts/update_trees.py --skip-download --no-wikipedia-lookup  # offline: cached Wikipedia titles only
(cd android && ./gradlew :core:data:testDebugUnitTest)  # opens the new asset through Room
```

The script prints a report such as:

```
220339 records read, 220339 kept; dropped: 0 invalid coordinates, 0 outside the coverage area, 0 duplicate ids
Wrote android/core/data/src/main/assets/trees.db (33.2 MB), data version 1 (unchanged)
```

Then commit `trees.db`, `android/core/data/trees-db-version.txt`, `android/core/data/trees-db-content.sha256`,
`android/core/data/trees-db-meta.json`, `data/wikipedia-titles.json` and, if the version went up, the new Room schema JSON
that the next Gradle build exports to `android/core/data/schemas/`.

A scheduled GitHub Actions workflow (`.github/workflows/update-data.yml`) does this and opens a pull request when
the data changed.

## Data provenance

`android/core/data/trees-db-meta.json` records which extraction the app ships. The script rewrites it on every run. The
top-level fields describe the Paris data:

| Field | Meaning |
|-------|---------|
| `extractedAt` | When the CSV was downloaded (UTC). A `--skip-download` rebuild keeps the download time of the CSV on disk. |
| `datasetModifiedAt` | The dataset's own « last modified » date, from the Opendatasoft metadata API (kept on `--skip-download`). |
| `recordsRead`, `recordsKept`, `dropped` | The cleaning report: rows in the export, rows shipped, and why the others were dropped. |
| `dataVersion` | The data version, as in `trees-db-version.txt`. |
| `wikipediaLinks` | How many genus + species pairs have a French Wikipedia article (see below). |
| `source`, `sourceExport`, `license` | The dataset page, the export URL used, and its licence (ODbL). |
| `otherSources` | The same fields for each other source (`id`: `gpso`, `remarquables`), its `dropped` counts (stumps, trees already in the Paris data). |
| `treesKept` | All the trees shipped, every source together. |

The app shows the extraction date and the number of trees in À propos (« Extraction du 6 novembre 2026 · 220 339
arbres »): `:core:data` reads the file at build time into `BuildConfig` and provides it as `DatasetInfo`.

**Current extraction:** Paris downloaded on 2026-11-06 at 16:30 UTC (dataset last modified 2026-11-06 09:34 UTC),
220,339 records read and kept; GPSO downloaded on 2026-11-10 (dataset last modified 2026-11-10 11:49 UTC), 32,684
read, 32,636 kept (48 stumps dropped). 252,975 trees in all; data version 3.

## What the script does

1. **Download** the full CSV export to `data/raw/les-arbres.csv` (git-ignored) with a SHA-256 checksum next to it.
2. **Check** that the expected fields exist; it fails loudly if the dataset format changed.
3. **Clean**
   - Keeps every tree, including the ~25k the City manages outside Paris (cemeteries of Pantin, Saint-Ouen,
     Thiais, Ivry, Bagneux…).
   - Drops rows with missing, unparsable or `0, 0` coordinates, and rows outside the coverage area
     (`COVERAGE` in the script = `GeoBounds.COVERAGE` in `:core:domain`; a test keeps them equal). If the report
     shows trees outside the area, widen both.
   - Drops duplicate ids (first one wins).
   - Height or circumference `0` → unknown (NULL).
   - Missing common name → genus → « Arbre ».
   - Development stage: « Jeune (arbre) » → « Jeune », « Jeune (arbre)Adulte » → « Jeune adulte ».
   - Cultivar quotes removed (`''Kanzan''` → `Kanzan`).
   - **GPSO** (`gpso.py`): ids become `10¹¹ + INSEE × 10⁶ + number` (a second tree sharing a GPSO id gets
     500,000 more); stumps (« Souche ») are dropped; « Acer platanoides 'Crimson King' » is split into genus,
     species and cultivar like the Paris data; height ranges go to `height_class` (« 5 à 10 »); « Remarquable » and
     « Majeur » trees are remarkable; a tree already in the Paris data (same genus within 1 m) is dropped.
   - **Town** (`city`): « Paris » for an arrondissement or a Bois, the source's own town for GPSO, otherwise the town
     whose boundary holds the tree, from `data/communes.geojson` (geo.api.gouv.fr, départements 75, 92, 93, 94;
     refresh with `--refresh-communes`).
4. **Link to Wikipedia**: for every distinct genus + species, find its French Wikipedia article through Wikidata
   (taxon name, property P225; `x hispanica` is looked up as `× hispanica`, forms and subspecies are dropped, `sp.`
   means genus only). When the species has no article, the genus article is used (plant genera only: rank genus with
   an IPNI ID), and the app says so. Answers, including « no article », are cached in `data/wikipedia-titles.json`,
   so a monthly run only asks Wikidata about new names; a failed request is logged and retried next run, it never
   fails the build. The links go to the `taxon_links` table (Room entity `TaxonLinkEntity`). Delete an entry from the
   cache to look it up again.
5. **Build** `trees.db` from Room's exported schema JSON (`android/core/data/schemas/…TreeDatabase/<n>.json`): the same
   `CREATE TABLE`/index statements and Room's identity hash, so Room accepts the file as is. The Room entity
   `TreeEntity` owns the schema — to add a column, change the entity, build `:core:data` so Room exports the new
   schema, update `COLUMNS` in the script, then re-run it.
6. **Version** the data: `PRAGMA user_version` = `android/core/data/trees-db-version.txt`, which also becomes
   `TreeDatabase`'s Room version through `BuildConfig.TREES_DB_VERSION`. The version goes up only when the content
   hash of the cleaned trees and their Wikipedia links (`android/core/data/trees-db-content.sha256`) changed. A higher version
   makes the app drop its old copy and copy the new asset; favorites live in a separate database and are kept.

## Tests

```bash
python3 -m venv data/.venv && data/.venv/bin/pip install -r data/requirements-dev.txt
data/.venv/bin/python -I -m pytest data/tests
```

`tests/fixtures/sample.csv` holds real rows (including trees outside Paris) plus crafted edge cases.
