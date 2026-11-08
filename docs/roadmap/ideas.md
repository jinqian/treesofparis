# Feature ideas

Brainstormed on 2026-11-08. Not planned yet: pick one, then detail it in `next-steps.md` (goal, approach, work,
decisions, done when). Sizes are rough: **S** ≈ half a day, **M** ≈ 1–2 days, **L** ≈ 3+ days.

Implemented from this list: « Herbier → carte » (tap a genus in Herbier to see only those trees on the map).

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
  the map. Needs curated content and probably the MapLibre map.
- **Share a tree** — *S–M.* A share card with the leaf drawing, name and place, and a link that opens the app on that
  tree (Android App Link).

## Polish and reach

- **Offline use** — *L.* Ship the Paris map with the app (PMTiles, see the MapLibre plan) so it works in parks with poor
  signal.
- **Home-screen widget** — *S–M.* « L'arbre du jour » with its leaf (Glance).
- **English version** — *S.* UI strings already live in resources; a translation pass plus English tree names for the
  21 drawn genera.
