# 10. The remarkable trees of the Hauts-de-Seine

Status: accepted (2026-11-15).

## Context
- The Département des Hauts-de-Seine publishes « Arbres remarquables » (Licence Ouverte, issue #91): 1,127 records in
  its 36 towns, outside private property. Each has French and Latin names, height, circumference (in metres) and crown
  spread, a general criterion (« Pour ses dimensions », « Pour son port »…) and particular ones (« Écorce
  remarquable », « Arbre de forêt »…), a planting year for 15 % of them, the « Arbre remarquable de France » label for
  12, and a photo for 639 (2448 × 3264 originals, about 4 MB, on the Département's server, no thumbnails).
- Compared with Paris's « Les arbres remarquables » (ADR 0009): no story, no site and no address (the town only), its
  own ids rather than a tree inventory's, and positions placed on aerial photographs, to a few metres. 67 records are
  groups (up to 112 trees as one point). Some measures are zero or typing errors (a circumference of 345 m).
- 391 records are in the eight GPSO towns (ADR 0008), but the GPSO inventory covers streets and parks while the
  Département also lists forest trees: only 43 are a GPSO tree of the same genus within 10 m. The rest, about 1,050,
  are trees the app does not have. 35 are north of the coverage area (Gennevilliers, Colombes, Villeneuve-la-Garenne).

## Decision
- `data/scripts/hauts_de_seine.py` adds the single trees as trees of their own (`source = 'hds'`, ids from 300
  billion, town from the INSEE code, empty address); groups are left out. Latin names lose their botanists' names;
  circumferences become centimetres; zero or impossible measures become unknown.
- A record that is a GPSO tree (same genus, nearest within 10 m) is merged: the GPSO tree, surveyed precisely, becomes
  remarkable and takes the details. No second pin.
- The details go in `remarkable_trees` (ADR 0009) with no schema change: the criterion as `qualification`
  (« Dimensions », « Port », « Paysager » for « Pour sa situation », « Botanique » for « Pour sa rareté »…), a summary
  built from the criteria (« Remarquable pour ses dimensions. Écorce remarquable. »), the planting year, the label
  (« A.R.B.R.E.S en 2026 »), and the photo with its credit (« Département des Hauts-de-Seine »). No story: the sheet's
  card reads « Sa fiche » instead of « Son histoire », with the summary as its sentence.
- The photo is loaded from the Département's server at full size, like Paris's photos; the privacy policy names it.
- `GeoBounds.COVERAGE` (and its copies in the data scripts) goes north to 48.950 for the 35 northern trees.
- The sheet credits « Arbres remarquables », Département des Hauts-de-Seine, alone or after GPSO for a merged tree.
  The Herbier's figures stay about Paris.

## Consequences
- 1,019 new trees and 1,377 remarkable trees in all (181 Paris, 177 GPSO, 1,019 Hauts-de-Seine). Tree data version 5.
- A Hauts-de-Seine photo costs about 4 MB of mobile data when its card is opened. Resized copies served by
  arbresdeparis.fr would fix that; not done yet.
- The Département says the inventory is being updated: the monthly data refresh picks up its changes.
- The groups (alignments, groves) are missing; they could come back as one pin each « Alignement de 97 marronniers ».
