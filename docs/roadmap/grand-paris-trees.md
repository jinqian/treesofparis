# Trees beyond Paris — integration plan

Drafted on 2026-11-10 from the « Petite couronne first » idea in `ideas.md`; updated on 2026-11-11. Sizes are rough:
**S** ≈ half a day, **M** ≈ 1–2 days, **L** ≈ 3+ days.

**Where it stands.** Phase 1 is built ([PR #28](https://github.com/jinqian/treesofparis/pull/28), ADR 0008): the
app and the web map show 252,975 trees, the 220,339 of Paris and the 32,636 of Grand Paris Seine Ouest's 8 towns,
each with its town and source. Next comes phase 2: the two départements' road trees and Plaine Commune, about 114,000
more trees. East of Paris (Pantin, Montreuil, Bagnolet…) no town publishes its trees; the Seine-Saint-Denis road
trees are the only usable source there (see « East of Paris » below).

## Done

| What | Where | Notes |
|------|-------|-------|
| GPSO's 8 towns, 32,636 trees | `data/scripts/gpso.py`, data version 3 | 48 stumps dropped; 24 shared ids renumbered; no tree also listed by Paris |
| `city`, `source`, `height_class` columns | `TreeEntity`, Room schema 3 | `city` from the source, or from the town boundaries (`data/communes.geojson`) for the ~25,000 trees Paris manages in other towns |
| Ids across sources | `gpso.tree_id` | 10¹¹ + INSEE × 10⁶ + number, far above Paris ids: favourites survive new sources |
| Tree sheet | `:feature:map` | The town outside Paris, « 5 à 10 m » for a height range, the source line |
| Credits | À propos, website, store texts | GPSO and its Licence Ouverte next to Paris Data's ODbL |
| Map area | `GeoBounds.COVERAGE` | Widened west to 2.150 for the GPSO towns |
| Tree groups | `GetTreeClusters` | Below street level, groups of trees with their count, placed where trees gather |
| Herbier | `TreeStatisticsDao` | Stays on the Paris data (`source = 'paris'`) until it can be filtered by town |
| Wording | app, website, store | « Paris et huit villes voisines » wherever the map is described |

## Cities with open tree data

Checked on 2026-11-10 (west, north) and 2026-11-11 (east) through each portal's API. Counts are trees in the
dataset. A town can appear in more than one source, because the town, its intercommunality and the département each
manage different trees.

| Source | Towns covered | Trees | Last update | Species | Size | Verdict |
|--------|---------------|-------|-------------|---------|------|---------|
| [GPSO — Arbres](https://data.seineouest.fr/explore/dataset/arbres-v2/) | Issy 9,840 · Boulogne 9,188 · Meudon 6,044 · Sèvres 2,746 · Vanves 2,439 · Ville-d'Avray 1,198 · Chaville 1,173 · Marnes-la-Coquette 56 | 32,684 | Nov 2026 | Latin + French name | Height class only | **Done** (phase 1) |
| [Seine-Saint-Denis — Arbres d'alignement sous gestion départementale](https://data.seinesaintdenis.fr/explore/dataset/dnpbarbres_alignement/) | The whole département, top: Saint-Denis 3,673 · Aulnay 1,938 · Montreuil 1,835 · Noisy-le-Grand 1,798 · Bobigny 1,382 · Pantin 956 | 32,485 | Nov 2026 | Latin + French name | Height (m), circumference, remarkable flag | Phase 2, first: the only source east of Paris |
| [Plaine Commune — Les arbres gérés par Plaine Commune](https://www.data.gouv.fr/datasets/les-arbres-geres-par-plaine-commune) | Saint-Denis, Saint-Ouen, Aubervilliers, La Courneuve, Épinay, Stains, Pierrefitte, Villetaneuse, L'Île-Saint-Denis | ≈ 51,000 (13 MB CSV) | May 2026, yearly | Family, genus, species | Height and circumference classes, planting date | Phase 2 |
| [Hauts-de-Seine — Arbres d'alignement sur la voirie départementale](https://opendata.hauts-de-seine.fr/explore/dataset/arbres-dalignement-sur-la-voirie-departementale/) | 36 towns, top: Rueil 1,855 · Gennevilliers 1,835 · Saint-Cloud 1,577 · Antony 1,565 · Boulogne 1,522 | 30,454 | Oct 2026 | Genus only | Height (m), circumference (cm) | Phase 2 |
| [Clichy — Arbres sur la voirie communale](https://opendata.hauts-de-seine.fr/explore/dataset/fr-219200243-arbres/) | Clichy | 6,221 | Feb 2023 | « Latin (French) » in one field | Height class, circumference, stage | Phase 3 |
| [Paris La Défense — Données Arbre](https://opendata.hauts-de-seine.fr/explore/dataset/fr-833718794-donnees-arbres/) | La Défense business district (Courbevoie, Puteaux) | 4,004 | Nov 2025 | Genus + species, upper case | Height class, trunk diameter, planting year | Phase 3 |
| [Montreuil — Arbres de la voirie communale](https://data.montreuil.fr/explore/dataset/arbres-voirie-communale/) | Montreuil | 505 | Jul 2019 | Latin + French name | None; a planting count per point | Skip: new plantings, not an inventory |
| [Hauts-de-Seine — Cadastre vert, les arbres](https://opendata.hauts-de-seine.fr/explore/dataset/cadastre-vert-les-arbres/) | All 36 towns, public and private | 527,663 | Feb 2026 | None | Crown diameter | Skip: canopy detection, no species |

Not found: any town or intercommunality tree inventory in the Val-de-Marne, Est Ensemble, Vallée Sud Grand Paris or
Paris Ouest La Défense (apart from Clichy). Versailles Grand Parc publishes one, but it lies outside the map area.

## East of Paris

Surveyed on 2026-11-11 for the Est Ensemble towns (Pantin, Montreuil, Bagnolet, Les Lilas, Romainville,
Le Pré-Saint-Gervais, Bobigny, Bondy, Noisy-le-Sec) and Vincennes, Saint-Mandé, Fontenay-sous-Bois.

| Town | In the app already | Département road trees | Town data | OpenStreetMap trees |
|------|--------------------|------------------------|-----------|---------------------|
| Pantin | 6,400 (Paris's cemetery) | 956 | none | 8,315, mostly the Paris cemetery |
| Montreuil | — | 1,835 | 505 plantings (2019) | 535 |
| Bobigny | 3,283 (Paris's sports park) | 1,382 | none | 3,384, mostly the same park |
| Bagnolet | — | 566 | none | 536 |
| Noisy-le-Sec | — | 1,047 | none | — |
| Romainville | — | 418 | none | — |
| Les Lilas | — | 302 | none (68 datasets, none on trees) | 208 |
| Le Pré-Saint-Gervais | — | 136 | none | 719 |
| Vincennes | — | — (Val-de-Marne) | green areas only, no tree points (data.vincennes.fr) | 571 |
| Saint-Mandé | — | — | none | 96 |
| Fontenay-sous-Bois | — | — | none | 940 |

What it means:

- **The Seine-Saint-Denis road trees are the way east.** They cover the main avenues of every town in the table but
  Vincennes, Saint-Mandé and Fontenay: about 7,000 trees in Est Ensemble, with species, height and circumference.
  Streets, squares and parks managed by the towns stay empty.
- **OpenStreetMap is not a source for whole towns.** Where it looks rich (Pantin, Bobigny) it holds trees the app
  already has from the Paris data; elsewhere it maps a few hundred trees per town (Issy: 612 in OSM against 9,840 in
  GPSO's inventory). It is ODbL like the Paris data, so it could fill a gap later, with a strict duplicate check.
- **The Val-de-Marne side has nothing.** No departmental road trees either, so Vincennes, Saint-Mandé and Fontenay stay
  empty until someone publishes.
- **Ask for the data.** Est Ensemble and Montreuil could be asked to open their tree inventories: local authorities
  of more than 3,500 inhabitants and 50 staff must publish their public data (loi pour une République numérique,
  2016). A short e-mail citing GPSO and Plaine Commune as examples costs little.

## Mapping onto our schema

As built in phase 1, every source fits the `trees` table; no source fills every column.

| Our column | GPSO (done) | Seine-Saint-Denis roads | Plaine Commune | Hauts-de-Seine roads |
|------------|-------------|-------------------------|----------------|----------------------|
| `id` | 10¹¹ + INSEE × 10⁶ + number | from `cd93_id_arb` (`ARB_DNPB_…`) | from `idsmart` (UUID) | from `id_emplacement` |
| `genus`, `species`, `cultivar` | parsed from `genespvar` | parsed from `arb_nom_latin` | `genre`, `espece` | `genre` only |
| `common_name` | `vernaculaire`, else the Paris name | `arb_nom_vernaculaire` | Paris name for genus + species | Paris name for the genus |
| `height_m` / `height_class` | class | metres | class « 5,01 à 10m » | metres |
| `circumference_cm` | none | number (unit to check) | class « 0,51 à 1 m » | cm |
| `remarkable` | `particular` = Remarquable or Majeur | `arb_remarquable` | `valeur_patrimoniale`? (to check) | false |
| `address` | `site`, without its section number | `nom_rue`, else `num_route` | `adresse` (often empty) | `nom_voie` |
| `city`, `arrondissement` | the town, from its INSEE code | `code_insee` | `commune` | `code_insee` |

`arrondissement` was kept: for other sources it holds the town, as `city` does. New sources need ids that are not
numbers (UUIDs, « ARB_DNPB_… »): give each source its own range (10¹¹ × source number) plus a stable hash, and test
that no two trees collide.

## Pipeline

As built: one module per source next to `update_trees.py` (`gpso.py`), which turns its rows into the table's
columns; `update_trees.py` downloads every source, gives each tree its town, drops a tree already listed by Paris
(same genus within 1 m), and records each source in `trees-db-meta.json` (`otherSources`). The monthly workflow
downloads them all.

Still to do for phase 2:

1. **Coverage east and north.** `COVERAGE` stops at 48.920 N and 2.480 E. Seine-Saint-Denis reaches 48.98 N
   (Tremblay) and 2.60 E (Noisy-le-Grand): widen `COVERAGE` and `GeoBounds.COVERAGE` to the data's extent, and the
   town boundaries file already covers the four départements.
2. **Duplicates between managers.** GPSO republishes the Hauts-de-Seine road trees of its towns; the 1 m check must
   run against every source loaded before, not only Paris.
3. **One failing source must not block the others.** Today a download error stops the whole run.
4. **Size.** `trees.db` is 42 MB on disk, 12 MB compressed in the APK. About 114,000 more trees add roughly 15 MB on
   disk, 4–5 MB in the APK.

## Phases

1. **GPSO** — *L.* **Done** (PR #28).
2. **Seine-Saint-Denis roads, then Plaine Commune, then Hauts-de-Seine roads** (≈ 114,000 trees) — *M each.*
   Seine-Saint-Denis first: it is the only source east of Paris. Gate: duplicates under 1 % per source; the tree
   sheet reads well for a road tree (« D 20 » as an address).
3. **Small sources (Clichy, La Défense)** — *S each.* Only if the format work is cheap; Clichy's data dates from 2023.
4. **Watch list and requests.** Re-run the portal search every few months (Val-de-Marne, Est Ensemble, Vallée Sud
   Grand Paris, Paris Ouest La Défense) and ask Est Ensemble and Montreuil to publish their inventories.
5. **Herbier by town.** With `city` in place: per-town figures, a town picker, the remarkable trees of each town.

## Open questions

- Before or after v1.0.0? PR #28 brings GPSO into 0.1.0; phase 2 can follow in an update during the closed test.
- Show road trees from the départements as « Département de la Seine-Saint-Denis » or name the road manager in the
  source line only?
- Is the Plaine Commune circumference class precise enough to show, or hide it?
- Does Etalab attribution need the per-source date shown in the app, or is the website enough?

Settled in phase 1: `arrondissement` stays (with `city` next to it); the coverage box follows the data's extent
rather than the A86 ring.

## Sources

Checked on 2026-11-10 and 2026-11-11 through the portals' APIs:
[data.seineouest.fr](https://data.seineouest.fr/explore/dataset/arbres-v2/),
[opendata.hauts-de-seine.fr](https://opendata.hauts-de-seine.fr/explore/?q=arbre),
[data.seinesaintdenis.fr](https://data.seinesaintdenis.fr/explore/dataset/dnpbarbres_alignement/),
[data.montreuil.fr](https://data.montreuil.fr/explore/dataset/arbres-voirie-communale/),
[data.vincennes.fr](https://data.vincennes.fr/explore/),
[data.issy.com](https://data.issy.com/explore/dataset/arbres-remarquables-issy-les-moulineaux/),
[data.iledefrance.fr](https://data.iledefrance.fr/explore/?q=arbre), the
[data.gouv.fr](https://www.data.gouv.fr/datasets/les-arbres-geres-par-plaine-commune) dataset and organisation search
(Est Ensemble, Les Lilas, Bobigny, Vincennes, Saint-Mandé), and OpenStreetMap through the Overpass API (`natural=tree`
nodes per town, 2026-11-11).
