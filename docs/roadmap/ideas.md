# Feature ideas

Brainstormed on 2026-11-08. Not planned yet: pick one, then detail it in its own file in this folder (goal, approach,
work, decisions, phases), like [`grand-paris-trees.md`](grand-paris-trees.md). Sizes are rough: **S** ≈ half a day,
**M** ≈ 1–2 days, **L** ≈ 3+ days.

Implemented from this list: « Herbier → carte » (tap a genus in Herbier to see only those trees on the map), a first
step of « Près de moi »: the map's « Arbre le plus proche » button (the 5 closest trees, one card at a time), and tree
groups below street level (a disc per ~70 dp cell with its count, counted live in SQLite; a tap zooms in).

## Explore the map

- **Search** — *M.* Find a species (« ginkgo », « Ginkgo biloba ») or an address and jump to it. Needs a search index
  in `trees.db` (an FTS table built by the data script, owned by a Room entity like the rest of the schema).
- **« Près de moi »** — *S–M.* The closest remarkable trees, or the nearest tree of each genus, as a list with
  distances; reuses the location the map already asks for.

## Learn and identify

- **« Quel est cet arbre ? »** — *L.* A simple identification key by leaf shape (lobed, compound, needles, toothed…)
  built on the 21 leaf drawings, narrowed to the genera found around the user. The most distinctive idea.
- **Seasonal notes** — *M.* « En fleur ce mois-ci » (cherries, horse chestnuts, limes) and autumn colours (maples,
  ginkgos), from a small hand-written calendar per genus; could highlight those trees on the map.
- **Remarkable tree stories** — *M.* A short text for each of the 181 remarkable trees. Text sources and licences need
  care (the Wikipedia links already cover species-level reading).

## Collect and walk

- **Herbier personnel** — *M.* A « vu » button on the tree sheet; the Herbier tab then shows the species you have
  found yourself (« 12 / 21 essences »). Stored in the user database next to favorites. Gentle gamification and a
  reason to come back.
- **Promenades** — *L.* Short themed walks (« Les géants du 16e », « Les cerisiers du printemps ») drawn as a route on
  the map. Needs curated content.
- **Share a tree** — *S–M.* A share card with the leaf drawing, name and place, and a link that opens the app on that
  tree (Android App Link).

## Polish and reach

- **Offline use** — *L.* Ship the Paris map with the app (a Protomaps PMTiles extract of `GeoBounds.COVERAGE`,
  self-hosted or bundled) so it works in parks with poor signal.
- **Wikipedia in Herbier** — *S.* The species sheet's « En savoir plus sur Wikipédia » link on the Herbier genus rows
  too. Once a year, delete the cached « no article » answers in `data/wikipedia-titles.json` to look them up again.
- **« Nouveautés » sheet** — *S.* Show the French release note once after an update (last seen `versionCode` in
  DataStore).
- **Home-screen widget** — *S–M.* « L'arbre du jour » with its leaf (Glance).
- **English version** — *S.* UI strings already live in resources; a translation pass plus English tree names for the
  21 drawn genera.

## Other cities

- **Petite couronne first** — *L overall, M per new source.* Show the trees just across the périphérique, so the map
  does not stop at the edge of Paris. The app already ships the ~25k trees the City manages outside Paris (cemeteries
  of Pantin, Saint-Ouen, Thiais…); what is missing are the trees the departments and towns manage themselves.

  **What exists** (searched 2026-11-09; open each portal to check fields, size, licence and freshness before choosing):

  | Area | Dataset | Notes |
  |------|---------|-------|
  | Hauts-de-Seine (92) | [« Cadastre vert – Les arbres »](https://www.data.gouv.fr/datasets/cadastre-vert-les-arbres), also on [data.iledefrance.fr](https://data.iledefrance.fr/explore/dataset/cadastre-vert-des-hauts-de-seine-les-arbres/) | Isolated and street trees across the department, updated 2026-02. The department's « arbres » set uses the Licence Ouverte. Street trees on departmental roads and [remarkable trees](https://data.iledefrance.fr/explore/dataset/arbres-remarquables-du-territoire-des-hauts-de-seine-hors-proprietes-privees) are separate sets; each tree is in only one of them ([OSM wiki](https://wiki.openstreetmap.org/wiki/WikiProject_France/Open_Data_Hauts-de-Seine_Arbres)). The best first candidate. |
  | Seine-Saint-Denis (93) | [« Arbres d'alignement sous gestion départementale »](https://www.data.gouv.fr/datasets/arbres-dalignement-sous-gestion-departementale), [remarkable trees](https://www.data.gouv.fr/datasets/arbres-remarquables-en-seine-saint-denis) | Street trees on departmental roads only, from the department's management database. An older « Arbres d'alignement » set is archived. The department's Plan Canopée promises a fuller open tree database. |
  | Val-de-Marne (94) | none found | Check the department's own portal. |
  | Towns | e.g. Versailles ([parks](https://www.data.gouv.fr/datasets/arbres-situes-dans-les-parcs), [street trees](https://www.data.gouv.fr/fr/datasets/arbres-dalignements/)) | Patchy: most towns publish nothing, and town and department data cover different trees (town streets vs departmental roads). |

  **Further away**, the same work would open other cities: [Lyon](https://rdata-grandlyon.readthedocs.io/en/latest/services.html)
  (~92k street trees on data.grandlyon.com), [Grenoble](https://www.data.gouv.fr/datasets/arbres-grenoble),
  [Toulouse](https://www.data.gouv.fr/datasets/arbres-urbains-1), Nantes, Rennes; [Bordeaux](https://www.data.gouv.fr/en/datasets/commune-de-bordeaux-arbres/)'s
  set is archived (2018). There is no national tree data standard: the CNIG is only starting one for hedges and tree
  rows ([mandate](https://cnig.gouv.fr/IMG/pdf/gt_cnig_-mandathaie_v1_0.pdf)), so every source needs its own mapping.

  **What it changes in the app**
  - *Tree ids.* Today a tree's id is Paris's `idbase`, and favorites store it. Ids from two sources can collide, so the
    id must include the source (e.g. a `source` column with a composite key, or ids offset per source). Do this before
    the first release if possible: afterwards, every saved favorite needs a migration.
  - *Schema.* `TreeEntity` mirrors the Paris fields. Other sources lack some (height, stage, French name, address) or
    name them differently: one mapper per source in `update_trees.py`, and `arrondissement` becomes a more general
    « commune / arrondissement ». The Herbier statistics and the remarkable filter must cope with the missing fields.
  - *Duplicates.* Paris's trees outside Paris and a department's data may overlap (same tree, two records): dedupe by
    distance and genus where the coverage areas meet.
  - *Coverage.* `GeoBounds.COVERAGE` (domain and script, kept equal by a test) grows to the new area.
  - *Size.* `trees.db` is 10 MB in the APK for 220k trees; each 100k trees adds roughly 5 MB. Beyond the petite
    couronne, download each city's data on demand instead of shipping it.
  - *Credits and licences.* Paris is ODbL, the 92 data Licence Ouverte: À propos and `trees-db-meta.json` list each
    source with its extraction date and licence.
  - *Name.* « Arbres de Paris » would no longer fit beyond the petite couronne: choose a name before opening other
    cities.
  - *Refresh.* The monthly workflow downloads every source; one failing source must not block the others.

  **Detailed in [`grand-paris-trees.md`](grand-paris-trees.md)** (2026-11-10): the full list of sources, the schema
  mapping and the phases. The cadastre vert above turned out to have no species (canopy detection only); the first
  source is now GPSO (32,684 trees, 8 towns south-west of Paris).
