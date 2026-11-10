# 8. Trees from several open datasets, each with its town

Status: accepted (2026-11-10).

## Context
- The map stopped at the edge of Paris. Neighbouring towns publish their own tree inventories, each in its own
  format (see `docs/roadmap/grand-paris-trees.md`). The first one, Grand Paris Seine Ouest (GPSO), covers 8 towns
  south-west of Paris with 32,684 trees, species and French names, but heights only as ranges.
- The Paris data names an arrondissement, a Bois or, for the ~25,000 trees the City manages elsewhere (cemeteries,
  sports grounds), only a département (« SEINE-SAINT-DENIS »). Nothing said which town a tree stands in.
- The Herbier's figures and wording are about Paris (« 1 arbre sur 5 à Paris est un platane »).

## Decision
- One `trees` table for every source, with three new columns (tree data version 3):
  - `city`, the town the tree stands in (« Paris », « Issy-les-Moulineaux », « Pantin »);
  - `source`, its dataset (« paris », « gpso »; `TreeSource` in the domain);
  - `height_class`, a height range in metres (« 5 à 10 ») for sources without an exact height.
- `city` comes from the source when it names the town (GPSO's INSEE code; « Paris » for an arrondissement or a
  Bois), otherwise from the official town boundaries (`data/communes.geojson`, geo.api.gouv.fr, Licence Ouverte)
  with a point-in-polygon test. Where a tree sits a few metres across a border, the source wins.
- Ids stay unique across sources: a GPSO tree is `10¹¹ + INSEE × 10⁶ + its number`, far above Paris ids, so
  favourites keep working when sources are added.
- Each source is a small module in `data/scripts/` (`gpso.py`) that turns its rows into the table's columns;
  `update_trees.py` merges them, drops a tree already listed by Paris (same genus within 1 m), and records each
  source in `trees-db-meta.json` (`otherSources`).
- The map, the tree sheet and favourites show every source. The tree sheet names the town outside Paris, shows a
  height range when there is no exact height, and credits its source. The Herbier stays on the Paris data
  (`source = 'paris'`) until it can be filtered by town.
- `GeoBounds.COVERAGE` widens west to 2.150 to hold the GPSO towns.

## Consequences
- `trees.db` grows from 33 to 42 MB (10 to 12 MB compressed in the APK).
- A new source = one module, its tests, a `TreeSource` value, a credit in À propos and the tree sheet, and the
  coverage box if it reaches further out.
- `city` is what Herbier features by town will build on (per-town figures, a town picker).
- GPSO's own « Remarquable » and « Majeur » trees get gold pins on the map; the Herbier's remarkable list stays the
  City of Paris's 181.
