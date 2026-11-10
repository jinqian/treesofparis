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
- **Quiz des arbres** — *M–L.* Short rounds of 5–10 questions to learn the trees while playing. Every question
  comes from data the app already ships, so it needs no network and no hand-written content to start:
  - *Quelle est cette feuille ?* One of the 21 leaf drawings, four genus names to choose from.
  - *Quand fleurit-il ?* The flowering months of a genus (« Guide des Essences de Paris »).
  - *Le plus planté ?* Two genera, which one has more trees (Herbier statistics, per town once `city` is used).
  - *Près de vous* (with location shared): the nearest tree's leaf or name, then « Voir sur la carte ».
  - *Remarquables*: the tallest or widest of two remarkable trees, from their height and circumference.

  Each answer shows a one-line explanation and the Wikipedia link, so a wrong answer still teaches something. Scores,
  best streak and the genera already mastered go in the user database next to favorites; questions favour what the
  user got wrong (simple spaced repetition). Entry points: a card in Herbier (« Testez-vous »), and a question of the
  day in the « L'arbre du jour » widget. Pairs well with « Herbier personnel » (trees seen) and « Quel est cet
  arbre ? » (the same leaf vocabulary). Open questions: a tab of its own or a Herbier card; keep scores local only
  (no account, no leaderboard) to stay true to the privacy promise.
- **Photos des essences** — *M.* A photo of the species on the tree sheet (and in the quiz, the Herbier, the
  « L'arbre du jour » widget), from reliable, freely licensed sources. Checked on 2026-11-11:
  - **Wikimedia Commons, through Wikidata** (best first source): the species' image (property P18) exists for 58 of the
    60 most planted species, 98 % of their trees. Look it up in the data script next to the Wikipedia titles (same
    Wikidata query, same cache in `data/`), and keep with each photo its file name, author and licence from the
    Commons API (`extmetadata`): most are CC BY-SA, so the sheet must show « Photo : author, licence » with a link.
    Download a thumbnail on demand from `upload.wikimedia.org` (cache it on the phone; the privacy policy gains a
    line), or ship small thumbnails for the most planted species only: about 100 × 30 KB.
  - **iNaturalist**: about 30,500 research-grade plant observations with photos in Paris, of real trees in place, but
    nearly all under CC BY-NC (one CC0/BY/BY-SA plane tree photo in the whole city). Usable as a link (« Observations à
    Paris sur iNaturalist »), not as shipped photos, unless filtered to free licences.
  - **Remarkable trees**: some have their own Wikidata item and Commons category (a photo of that very tree, not the
    species). Match them by name and position; worth a pass for the 347 remarkable trees.
  - **Pl@ntNet** (identification service with CC BY-SA images) needs an API key and has quotas: to check before
    relying on it.

  Never scrape photos from sites without a clear licence; credit every photo where it is shown, and in À propos.
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
