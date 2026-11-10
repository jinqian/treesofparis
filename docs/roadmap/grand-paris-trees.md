# Trees beyond Paris — integration plan

Drafted on 2026-11-10 from the « Petite couronne first » idea in `ideas.md`. **Phase 1 (GPSO) built on 2026-11-10**
(ADR 0008): the `city`, `source` and `height_class` columns, GPSO's 32,636 trees, the tree sheet and À propos credits.
The Herbier stays on the Paris data until it can be filtered by town. Sizes are rough:
**S** ≈ half a day, **M** ≈ 1–2 days, **L** ≈ 3+ days.

Five open sources would add about 153,000 trees around Paris: GPSO, Plaine Commune, the Hauts-de-Seine and
Seine-Saint-Denis departmental roads, and Clichy. That is +70 % on the 220,339 Paris trees. Start with GPSO
(32,684 trees in 8 towns south-west of Paris): one clean, fresh dataset with species names.

All the new sources are under the Licence Ouverte 2.0 (Etalab). It allows reuse with attribution, alongside the ODbL
Paris data. The Val-de-Marne and most towns east and south of Paris publish nothing usable.

## Cities with open tree data

Checked on 2026-11-10 through each portal's API. Counts are trees in the dataset. A town can appear in more than one
source, because the town, its intercommunality and the département each manage different trees.

| Source | Towns covered | Trees | Last update | Species | Size | Verdict |
|--------|---------------|-------|-------------|---------|------|---------|
| [GPSO — Arbres](https://data.seineouest.fr/explore/dataset/arbres-v2/) | Issy 9,840 · Boulogne 9,188 · Meudon 6,044 · Sèvres 2,746 · Vanves 2,439 · Ville-d'Avray 1,198 · Chaville 1,173 · Marnes-la-Coquette 56 | 32,684 | Nov 2026 | Latin + French name | Height class only | Phase 1 |
| [Plaine Commune — Les arbres gérés par Plaine Commune](https://www.data.gouv.fr/datasets/les-arbres-geres-par-plaine-commune) | Saint-Denis, Saint-Ouen, Aubervilliers, La Courneuve, Épinay, Stains, Pierrefitte, Villetaneuse, L'Île-Saint-Denis | ≈ 51,000 (13 MB CSV) | May 2026, yearly | Family, genus, species | Height and circumference classes, planting date | Phase 2 |
| [Hauts-de-Seine — Arbres d'alignement sur la voirie départementale](https://opendata.hauts-de-seine.fr/explore/dataset/arbres-dalignement-sur-la-voirie-departementale/) | 36 towns, top: Rueil 1,855 · Gennevilliers 1,835 · Saint-Cloud 1,577 · Antony 1,565 · Boulogne 1,522 | 30,454 | Oct 2026 | Genus only | Height (m), circumference (cm) | Phase 2 |
| [Seine-Saint-Denis — Arbres d'alignement sous gestion départementale](https://data.seinesaintdenis.fr/explore/dataset/dnpbarbres_alignement/) | The whole département, top: Saint-Denis 3,673 · Aulnay 1,938 · Montreuil 1,835 · Noisy-le-Grand 1,798 | 32,485 | Nov 2026 | Latin + French name | Height (m), circumference, remarkable flag | Phase 2 |
| [Clichy — Arbres sur la voirie communale](https://opendata.hauts-de-seine.fr/explore/dataset/fr-219200243-arbres/) | Clichy | 6,221 | Feb 2023 | « Latin (French) » in one field | Height class, circumference, stage | Phase 3 |
| [Paris La Défense — Données Arbre](https://opendata.hauts-de-seine.fr/explore/dataset/fr-833718794-donnees-arbres/) | La Défense business district (Courbevoie, Puteaux) | 4,004 | Nov 2025 | Genus + species, upper case | Height class, trunk diameter, planting year | Phase 3 |
| [Montreuil — Arbres de la voirie communale](https://data.montreuil.fr/explore/dataset/arbres-voirie-communale/) | Montreuil | 505 | Jul 2019 | To check | To check | Skip: stale, tiny |
| [Hauts-de-Seine — Cadastre vert, les arbres](https://opendata.hauts-de-seine.fr/explore/dataset/cadastre-vert-les-arbres/) | All 36 towns, public and private | 527,663 | Feb 2026 | None | Crown diameter | Skip: canopy detection, no species |

Not found: the Val-de-Marne (no departmental or town tree data on data.gouv.fr), Est Ensemble apart from Montreuil,
Vallée Sud Grand Paris, Paris Ouest La Défense towns other than Clichy, and Vincennes. Versailles Grand Parc publishes
one, but it lies outside the map area.

The Paris dataset already covers about 25,000 trees the City manages outside Paris: its cemeteries in Pantin,
Saint-Ouen, Thiais, Ivry and Bagneux, and the Bois.

## Mapping onto our schema

Every source fits the existing `trees` table (`TreeEntity`) with three small additions; no source fills every column.
Paris rows stay as they are.

| Our column | GPSO | Plaine Commune | Hauts-de-Seine roads | Seine-Saint-Denis roads |
|------------|------|----------------|----------------------|-------------------------|
| `id` | `id_arbre` (`92040_10302`), hashed | `idsmart` (UUID), hashed | `id_emplacement` | `cd93_id_arb` (`ARB_DNPB_…`), hashed |
| `genus`, `species` | split `genespvar` | `genre`, `espece` (lower-case the species) | `genre` only | split `arb_nom_latin` |
| `common_name` | `vernaculaire` | none: look up from genus + species | none: look up from genus | `arb_nom_vernaculaire` |
| `height_m` | class « De 5 à 10 m » | class « 5,01 à 10m » | metres | metres |
| `circumference_cm` | none | class « 0,51 à 1 m » | cm | number (unit to check) |
| `remarkable` | `particular` = Remarquable or Majeur | `valeur_patrimoniale`? (to check) | false | `arb_remarquable` |
| `address` | `site` (« Rue Diderot 1 ») | `adresse` (often empty) | `nom_voie` | `nom_rue`, else `num_route` |
| `arrondissement` | `commune` | `commune` | `nom_commune` | `commune` |

The additions:

1. **`source`** (text): which dataset the row comes from, for the credit line on the tree sheet and in À propos.
2. **`height_class`** (text): keep « 5 à 10 m » as a range instead of inventing a number; the sheet shows the range
   when `height_m` is null. Circumference classes go the same way.
3. **`arrondissement` becomes the place label.** For Paris it stays « 12e »; outside it holds the town name. A rename
   to `place` is cleaner but touches every layer; decide in phase 1.

Ids: Paris ids are integers from 99,874 to 2,058,277. Each new source gets its own range (source number × 10⁹ + a
stable hash of its id), so ids never collide and favourites survive a refresh. Do this before the first release if
possible: afterwards, saved favourites need a migration.

Names: a genus + species → French name table, built from the Paris rows (`libellefrancais`), fills the gaps for
Plaine Commune and the Hauts-de-Seine roads. The Wikipedia link lookup works unchanged on genus + species.

## Pipeline

`update_trees.py` becomes a loop over sources: one small adapter per dataset turns its rows into our row dict, then
the existing cleaning, id, Wikipedia and Room-schema steps run once on the merged list.

1. **Adapters.** `data/scripts/sources/` with one module per source: download URL, expected fields, `to_row()`,
   licence and credit text. All Opendatasoft portals share one downloader (CSV export:
   `/api/explore/v2.1/catalog/datasets/<id>/exports/csv`); Plaine Commune is a plain CSV on data.gouv.fr, picked from
   the dataset's latest resource.
2. **Coverage.** Our box (48.735–48.920 N, 2.200–2.480 E) cuts most of the suburbs: today it would keep 30,254 of
   GPSO's 32,684 trees, 23,710 of 30,454 in the Hauts-de-Seine and 9,632 of 32,485 in Seine-Saint-Denis. Widen
   `COVERAGE` and `GeoBounds.COVERAGE` together, roughly to the A86 ring (48.70–48.98 N, 2.10–2.58 E).
3. **Duplicates.** Sources should not overlap (each lists the trees its own body manages), but GPSO also republishes
   the Hauts-de-Seine road trees in its area. Keep one source per manager, and drop a new tree within 1 m of another
   with the same genus; the report counts them.
4. **Provenance.** `trees-db-meta.json` becomes a list: per source, its URL, licence, modified date and counts. The
   data PR shows the per-source report.
5. **Size.** `trees.db` is 32 MB on disk (about 10 MB compressed in the APK) for 220,339 trees. 153,000 more trees
   add about 22 MB on disk, roughly 7 MB in the APK. Measure it in phase 2.
6. **Failure handling.** A source that is down or whose fields changed keeps its previous rows and fails the scheduled
   run loudly; one failing source must not block the others.

Tests: one fixture CSV per source in `data/tests/`, checked through `to_row()`, plus the existing Room asset test.

## App changes

The map and favourites work unchanged once the rows are in `trees.db`; the visible work is in the tree sheet and
À propos.

- **Tree sheet.** Show the town for suburban trees (« Issy-les-Moulineaux » instead of « 12e »), the height range when
  there is no exact height, and hide empty rows. Add a small source line: « Données : GPSO ».
- **Map.** Widen the camera limits with `GeoBounds.COVERAGE`. The OpenFreeMap tiles already cover the whole region.
- **À propos.** One data credit row per source, with its licence and extraction date (Etalab asks for the source and
  the date of last update). The tree count becomes the total, with the per-source split behind it.
- **Herbier and flowering sheets.** The statistics and the remarkable filter must cope with missing fields. If the
  27 « Guide des Essences de Paris » sheets match by genus + species (to check), suburban trees of the same species
  get them for free.
- **Store listing and website.** « Arbres de Paris » stays the name; the descriptions mention « et de la petite
  couronne » only for the towns actually covered.

## Phases

Each phase is one PR, shippable alone; the next starts when the gate is met.

1. **GPSO (8 towns, 32,684 trees)** — *L.* Source adapters, `source` and `height_class` columns, source-aware ids,
   wider coverage, tree sheet and À propos credits. Gate: the data PR report shows no dropped GPSO rows, and the tree
   sheet reads well for a tree with only a height class.
2. **Departmental roads and Plaine Commune (≈ 114,000 trees)** — *M.* Three more adapters, the duplicate check, the
   common name lookup. Gate: duplicates under 1 % per source; `trees.db` growth measured.
3. **Small sources (Clichy, La Défense)** — *S each.* Only if the format work is cheap; Clichy's data dates from 2023.
4. **Watch list.** Re-run the portal search every few months: Val-de-Marne, Est Ensemble, Vallée Sud Grand Paris and
   Paris Ouest La Défense may publish later.

## Open questions

- Before or after v1.0.0? The closed test is starting; source-aware ids are cheaper before release, but more trees
  means a bigger APK and new store text.
- Rename `arrondissement` to `place`, or keep the column and change only its meaning?
- Which coverage box: the A86 ring, or just the towns we have data for?
- Is the Plaine Commune circumference class precise enough to show, or hide it?
- Does Etalab attribution need the per-source date shown in the app, or is the website enough?

## Sources

Checked on 2026-11-10 through the portals' APIs:
[data.seineouest.fr](https://data.seineouest.fr/explore/dataset/arbres-v2/),
[opendata.hauts-de-seine.fr](https://opendata.hauts-de-seine.fr/explore/?q=arbre),
[data.seinesaintdenis.fr](https://data.seinesaintdenis.fr/explore/dataset/dnpbarbres_alignement/),
[data.montreuil.fr](https://data.montreuil.fr/explore/?q=arbre),
[data.issy.com](https://data.issy.com/explore/dataset/arbres-remarquables-issy-les-moulineaux/),
[data.iledefrance.fr](https://data.iledefrance.fr/explore/?q=arbre) and the
[data.gouv.fr](https://www.data.gouv.fr/datasets/les-arbres-geres-par-plaine-commune) dataset search.
