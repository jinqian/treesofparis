# Website — arbresdeparis.fr

The app's site, in the same « Herbier » look: plain HTML, CSS and JavaScript, no framework, no cookies. Visits are counted by
[GoatCounter](https://arbresdeparis.goatcounter.com) (cookieless, no IP stored; one `<script>` at the end of each page's
`<head>`, and `data-goatcounter-click` on the map, group and Google Play links). The privacy policy describes it
(`confidentialite/#audience`): update it when the counted clicks change.
GitHub Pages serves this folder (`.github/workflows/pages.yml` deploys it when it, `trees.db` or the map script change on
`main`). This README is
published with the site too; it holds nothing private.

| File | What |
|------|------|
| `index.html`, `styles.css` | The page. Colours are CSS variables from `HerbierPalette`, with the night palette for dark mode. |
| `confidentialite/index.html` | Privacy policy, linked from the footer and from the Play Console (`https://arbresdeparis.fr/confidentialite/`). Update its date when it changes. |
| `maps/` | The web map (`/maps/`): `index.html`, `maps.js` (cells, taps, tree sheet with a remarkable tree's « Son histoire » and its photo in full screen, location), `style.js` (the Herbier map style, a copy of the app's `HerbierMapStyle.kt` and pin colours: keep them in step), `maps.css`. MapLibre GL JS 5.24.0 is self-hosted in `vendor/` with its BSD licence. |
| `maps/data/` | Git-ignored. The trees, cut from the app's `trees.db` by `data/scripts/build_web_map.py`: one JSON file per ~1 km cell (loaded from street level), `clusters.json` (tree counts on 2 km, 1 km, 500 m and 250 m grids, drawn as discs below street level), `remarkable.json` (shown at every zoom), `stories.json` (the remarkable trees' stories, « Les arbres remarquables », loaded when one is opened; photos and signs stay on the City's server), `taxa.json` (Wikipedia titles), `genera/` (the groups of each genus alone, for `?genre=`) and `index.json`. The Pages workflow builds it before each deployment. The map also opens on one genus (`/maps/?genre=Tilia`) or one remarkable tree (`/maps/?arbre=<id>`): the Herbier page links there. |
| `herbier/` | The Herbier page (`/herbier/`): the app's Herbier tab on one page (totals, « 1 arbre sur 5 », the drawn genera, the flowering calendar, the remarkable trees by area, ages and most wooded arrondissements; Paris only, as in the app). `index.html` is git-ignored: `data/scripts/build_herbier.py` writes it from `trees.db` and `data/templates/herbier.html` (edit the template). `herbier.css`, and `herbier.js`, which marks the month in progress and fills « En fleur en … ». |
| `robots.txt`, `sitemap.xml` | For search engines: every page may be indexed except the map's data files; the sitemap lists the pages (add a new page there). The home page also describes the site as structured data (`WebSite`, JSON-LD). |
| `404.html` | Shown by GitHub Pages for unknown paths (absolute links, as it can be served from any path). |
| `fonts/` | Inter and Newsreader from `:core:designsystem`, subset to Latin + French punctuation and weights 400–600, WOFF2, with their OFL licences. Self-hosted: no request to Google Fonts. |
| `images/` | App screenshots (debug build on an emulator, demo-mode status bar), 490 × 1100 JPEG. |
| `favicon.svg` | The app logo, « Goutte » (same drawing as the launcher icon). |
| `CNAME` | The custom domain. |

## Preview

```bash
python3 data/scripts/build_web_map.py              # once, and after trees.db changes: the map's tree files
python3 data/scripts/build_herbier.py              # the Herbier page (/herbier/)
python3 -m http.server --directory website 8000   # then open http://localhost:8000 (the map: /maps/)
```

## When the app is on a store

Replace the « Bientôt disponible sur Android » block (`div.store-badge`) in `index.html` with the link (the comment above it has the
Play Store URL for `fr.arbresdeparis`). Use Google's official badge only once the store listing exists.

## Domain setup (once)

1. **Repository → Settings → Pages → Build and deployment → Source: GitHub Actions.** Then run the « Website »
   workflow (Actions tab → Website → Run workflow) or merge a change under `website/`.
2. **Settings → Pages → Custom domain:** `arbresdeparis.fr`, then tick « Enforce HTTPS » once the certificate is ready
   (it can take up to an hour after DNS works).
3. **At the domain registrar**, DNS records for `arbresdeparis.fr`:

   | Type | Name | Value |
   |------|------|-------|
   | A | `@` | `185.199.108.153` |
   | A | `@` | `185.199.109.153` |
   | A | `@` | `185.199.110.153` |
   | A | `@` | `185.199.111.153` |
   | AAAA | `@` | `2606:50c0:8000::153` |
   | AAAA | `@` | `2606:50c0:8001::153` |
   | AAAA | `@` | `2606:50c0:8002::153` |
   | AAAA | `@` | `2606:50c0:8003::153` |
   | CNAME | `www` | `jinqian.github.io.` |

   Remove any other A/AAAA records on `@` (registrars often add a parking page).
4. **Recommended: verify the domain** in your GitHub account (Settings → Pages → Add a domain), so no other GitHub
   account can claim `arbresdeparis.fr`. GitHub gives a TXT record to add at the registrar.

Check with `dig arbresdeparis.fr +short` (the four `185.199.x.153` addresses) and `dig www.arbresdeparis.fr +short`.
