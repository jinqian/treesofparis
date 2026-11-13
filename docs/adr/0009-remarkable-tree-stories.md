# 9. The stories of the remarkable trees, in the tree database

Status: accepted (2026-11-13).

## Context
- « Les arbres » only says a tree is remarkable (`remarquable = OUI`). The City publishes, in a second dataset,
  « Les arbres remarquables » (ODbL), why each one is: its site, its qualification (historique, paysager…), a
  summary, a story of a few paragraphs, its planting year, its national label and council decision when it has one,
  a link to its sign (PDF) and to a photo with its photographer's credit (issue #74).
- Its records carry the tree's id in « Les arbres » (`arbres_idbase`): all 181 remarkable trees of Paris match
  exactly. The Mûrier of the École normale supérieure is not in « Les arbres »; one tree is listed twice. The 166
  remarkable trees of the GPSO towns (ADR 0008) have no story.
- The photos weigh about 270 KB each, ~50 MB for all of them; `trees.db` already weighs 42 MB.

## Decision
- One more table, `remarkable_trees`, in `trees.db` (tree data version 4), keyed by the tree id, owned by
  `RemarkableTreeEntity` like every other table. `data/scripts/remarkable.py` reads the export and keeps one row per
  tree the app has (the first one when a tree is listed twice); `update_trees.py` builds the table and records the
  dataset in `trees-db-meta.json`. No second database: same key, same refresh, same version.
- The text ships in the app (under 100 KB); the photo and the sign are not. The photo is loaded from the City's
  server (capgeo.sig.paris.fr, with Coil) when the story is shown, always with its credit (« © Sonia Yassa / Ville
  de Paris »); the sign opens in the browser. The privacy policy names that server.
- The tree sheet of a remarkable tree shows its site, its qualification and a gold « Son histoire » card (summary,
  photo); the card opens a full page with the photo, why it is remarkable, its story, its measures, planting year,
  label and council decision, the sign and Wikipédia. The Herbier's list of remarkable trees shows one sentence of
  each story, skipping a botanical opening (« … appartient à la famille des … »).
- Trees without a story (GPSO) keep the sheet they had.

## Consequences
- Refreshing the data refreshes the stories; a story change bumps the data version like any other change.
- Without a connection the story reads fine, with a leaf drawing in place of the photo.
- The photo URLs are the City's: if it moves them, the drawing shows until the next data refresh.
