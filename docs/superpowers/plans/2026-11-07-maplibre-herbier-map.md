# Herbier Map (MapLibre + vector tiles) Implementation Plan

> **Status: parked (2026-11-07).** Proposed after the « Herbier » UI redesign (branch `feat/herbier-redesign`).
> Pick it up when the remaining design details are done. Re-check the facts marked _verify_ before starting:
> they come from a quick web search on 2026-11-07.
>
> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or
> superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the map itself match the Herbier design (paper land, sage parks, soft blue Seine, no POI clutter,
a real dark map) and move off `tile.openstreetmap.org`, which a published app may not use heavily.

**Architecture:** A new `:core:map-maplibre` module implements the existing `MapProvider` contract
(`docs/adr/0002-map-provider-abstraction.md`) with MapLibre Native through MapLibre Compose, drawing vector tiles
with our own style JSON. Features do not change: they already pass `MapPalette` and markers through `TreeMapView`.
`:app` switches the provider in one place (`LocalMapProvider`).

**Tech Stack:** MapLibre Native Android, MapLibre Compose (`org.maplibre.compose`), vector tiles from OpenFreeMap
(OpenMapTiles schema) to start, Protomaps PMTiles later for self-hosting and offline use.

**Design reference:** the « Arbres de Paris — Redesign » canvas (https://claude.ai/artifact/Dh8SzfBuBAw5TWhFtUguRs),
artboards « Carte », « Fiche arbre », « Fiche arbre — mode sombre » and « Fondations ».

## Why (findings, 2026-11-07)

1. **The tiles clash with the new UI.** Standard OSM (Mapnik) raster tiles are bright and busy with POI icons; the
   Herbier chrome looks pasted on top. Screenshots taken on the emulator during the redesign.
2. **Tile usage policy.** The OSMF policy forbids distributing a heavy-usage app that pulls tiles from
   `tile.openstreetmap.org` without prior permission, and asks apps not to hardcode that URL. We need another tile
   source before a public release whatever we decide on the look.
3. **Pins merge into blobs at medium zoom.** `SimpleFastPointOverlay` with up to `MAX_MARKERS = 1500` dots at zoom
   ~16 draws continuous green bands along streets. MapLibre draws circle layers on the GPU and supports clustering
   and zoom-dependent radius.
4. **Raster colour filter is a ceiling, not a fix.** Tried on the emulator (not committed): osmdroid
   `tilesOverlay.setColorFilter` with saturation 0.2, then 72 % of the tile + 28 % of paper `#F4F3EC`. Calmer and
   the pins stand out, but parks and the Seine turn grey, POI icons stay, and the tile policy issue remains.
   Snippet kept under « Fallback » below in case we need a stop-gap.

## Options compared

| Option | Look | Tile policy | Effort | Notes |
|---|---|---|---|---|
| 1. osmdroid + colour filter on OSM tiles | calmer, still busy, greys out parks and water | ❌ still OSM tiles | ~15 lines | stop-gap only |
| **2. MapLibre + own vector style (recommended)** | exact Herbier palette, light and dark | ✅ OpenFreeMap now, self-hosted PMTiles later | new module, ~2–4 days | also fixes pin blobs (GPU circles, clustering) |
| 3. Hosted styled raster/vector (Stadia/Stamen, MapTiler, CARTO) | nice fast | ✅ with an API key | small | keys, quotas, terms; can't tune to our palette |

## Global Constraints

- `org.maplibre` imports allowed **only** in `:core:map-maplibre` (extend Detekt `ForbiddenImport`, same as
  `org.osmdroid`). Features never depend on `:core:map-maplibre` (extend `checkModuleBoundaries`).
- The `MapProvider` contract stays provider-neutral. Camera rules unchanged: `initialCamera` once, `CameraRequest`
  applied once per id, gestures only through `onCameraChange`. Grow the contract only if a feature needs it
  (e.g. clustering on/off) and update ADR 0002.
- Pin colours come from `MapPalette` (tree, remarkable, selected, ring, userLocation): no hard-coded colours in
  the provider. The ring becomes `circle-stroke-color` / `circle-stroke-width` (2 dp) instead of a second layer.
- Attribution stays visible on the map: « © OpenStreetMap contributors » plus the tile provider (OpenFreeMap /
  Protomaps) as their terms require; update À propos (`feature/about` strings and the libraries list).
- Versions in `gradle/libs.versions.toml`, never inline. New module uses `trees.android.library` +
  `trees.android.compose` (+ `trees.android.hilt` if it provides the binding).
- Keep osmdroid working until the MapLibre provider is accepted on device; then remove it in its own commit.
- TDD as usual: pure functions (style colour mapping, GeoJSON building, camera tracking) get JVM tests;
  Robolectric where MapLibre allows it (it needs native libs, so keep logic outside the view).

## Herbier map style (target values)

Write the style as JSON assets (`herbier-light.json`, `herbier-dark.json`) on the OpenMapTiles schema, starting
from OpenFreeMap **Positron** (POIs already removed) or **Liberty** (_verify_ which is maintained: a fork notes the
upstream OpenMapTiles styles Positron/Bright/Dark/Fiord are abandoned while Liberty is maintained).

| Layer | Light | Dark |
|---|---|---|
| background / land | Paper `#F4F3EC` | `#1A1F1B` |
| parks, cemeteries, woods (`landuse`/`landcover` park, grass, wood, cemetery) | `#DCE4D2` | `#20291E` |
| water (Seine, canals, lakes) | `#CFDDE0` | `#1B2A2E` |
| buildings | `#E8E6DC`, from zoom 15, no 3D | `#222823` |
| roads, all classes | `#FBFAF6` (width by class) | `#272D28` |
| road casing | `#E2E1D8` | `#2E352F` |
| rail | `#D3D2C8` dashed | `#2E352F` |
| street labels | Inter-like sans, `#5C665D`, halo paper | `#A3AB9F`, halo `#1A1F1B` |
| place / arrondissement labels | serif if glyphs allow, `#1E2620` at 70 % | `#E6E8E1` at 70 % |
| POI icons, transit icons, shops | **hidden** | hidden |
| boundaries | hidden except city limit, `#C9C8BC` hairline | `#3A423B` |

Pins (from `MapPalette`, already in the theme): tree `#4F7556` / `#7FA286`, remarkable `#C9962E` / `#D4A443`,
ring `#FBFAF6` / `#141815`, user `#4A7FA8` / `#7FB0D6`, selected = `onSurface`.

Glyphs: the style's fonts must exist on the glyph server (OpenFreeMap serves Noto Sans). Serif labels need our own
glyph PBFs (generate from Newsreader with `font-maker` / `build_pbf_glyphs`) — optional, do it last.

---

### Task 0: Re-verify the moving parts (½ day)

- [ ] MapLibre Compose: latest release and its MapLibre Native version (v0.18.0 was announced; minor releases have
      breaking changes) — _verify_ on the GitHub releases page; pin exact versions.
- [ ] OpenFreeMap: style URLs (`https://tiles.openfreemap.org/styles/liberty`, `/positron` — _verify_), terms
      (public instance is as-is and may change), attribution text.
- [ ] Protomaps: PMTiles support in MapLibre Native Android (`pmtiles://`), flavors (light, dark, white,
      grayscale, black), cost of an Île-de-France extract covering `GeoBounds.COVERAGE`.
- [ ] Confirm minSdk 26 / compileSdk 37 / JDK 25 / AGP compatibility of the MapLibre artifacts.

### Task 1: ADR 0005 — vector map with MapLibre

- [ ] `docs/adr/0005-maplibre-vector-map.md`: context (findings above), decision, consequences (APK size of
      native libs, tile source, attribution, offline path). Link it from `docs/adr/README.md`.

### Task 2: Herbier style JSON

- [ ] Copy the chosen OpenFreeMap style, keep its sources, and apply the table above (light + dark).
- [ ] Remove POI/transit symbol layers; keep street and place labels.
- [ ] Assets in `:core:map-maplibre` (`src/main/assets/map/`); a pure-Kotlin test checks every colour in the style
      JSON is one of the palette values (keeps the style and the theme in sync).

### Task 3: `:core:map-maplibre` module

- [ ] Module with convention plugins; `MapLibreMapProvider : MapProvider`.
- [ ] Style chosen from `isSystemInDarkTheme()` (or a contract flag if features need control).
- [ ] Markers → one GeoJSON source (`id`, `style`, `selected` properties) + circle layers: tree, remarkable on
      top, selected on top with a larger radius; `circle-stroke` from `palette.ring`. Click → `onMarkerClick(id)`
      via a feature query on the circle layers.
- [ ] User location: its own source + circle layer (ring + dot), drawn above trees.
- [ ] `MapConfig` → camera bounds and min/max zoom; `initialCamera`, `CameraRequest` (reuse a tracker like
      `CameraRequestTracker`), `onCameraChange` with visible bounds, debounced like today.
- [ ] Attribution control styled small, bottom-start, above the bottom bar (same place as today's overlay).
- [ ] Tests: GeoJSON building, layer ordering, camera request de-duplication, palette → style expressions.

### Task 4: Boundaries and wiring

- [ ] Detekt `ForbiddenImport` for `org.maplibre` outside `:core:map-maplibre`; `checkModuleBoundaries` forbids
      features from depending on it.
- [ ] `:app` provides `MapLibreMapProvider` through `LocalMapProvider`. For the comparison phase, a debug-only
      switch (build config field or a debug menu) to flip between osmdroid and MapLibre on device.

### Task 5: Revisit marker limits

- [ ] With GPU circles, measure frame time at zoom 15–17 in the densest areas (Marais, Bois de Vincennes edge).
      Revisit `MAX_MARKERS = 1500` and `MIN_TREE_ZOOM = 16.0` (spec + `MapViewModel`).
- [ ] Try zoom-interpolated `circle-radius` (smaller at 15, 6 dp at 17+) and MapLibre clustering below
      `MIN_TREE_ZOOM` instead of the « Zoomez pour voir les arbres » hint. Keep the hint if clustering is worse.

### Task 6: Accept on device, then remove osmdroid

- [ ] Screenshots light/dark side by side with the canvas; check the tree sheet, locate, remarkable filter,
      opening a tree from Favoris, rotation/process death keeps the camera.
- [ ] Remove `:core:map-osmdroid`, its Detekt rule entry, the osmdroid setup and dependency; update À propos
      (« Tuiles … affichées avec osmdroid »), `CLAUDE.md` module table, the spec, ADR 0002 consequences.

### Task 7 (later): self-hosted / offline tiles

- [ ] Build a PMTiles extract for `GeoBounds.COVERAGE` (Protomaps basemap), host it (or bundle it if size allows)
      with glyphs and sprites (Protomaps `basemaps-assets`), and point the style at `pmtiles://…`.
- [ ] Optional: serif label glyphs from Newsreader.

## Review Focus

- No network / tile server down: the map shows the paper background and the pins, no crash, no blank white.
- Dark mode switch while the map is open: style swaps without losing camera or markers.
- Tapping overlapping pins picks the top one (remarkable over ordinary), same as today.
- Attribution always visible and not hidden by the bottom bar or the sheet.
- APK size delta from MapLibre native libraries (per ABI) is acceptable; consider ABI splits if not.

## Fallback (if we need something before this plan)

Stop-gap look on the existing osmdroid map (`createMapView` in `OsmdroidMapProvider.kt`), tested on the emulator:

```kotlin
overlayManager.tilesOverlay.setColorFilter(
    ColorMatrixColorFilter(
        ColorMatrix().apply {
            setSaturation(0.2f)
            postConcat(
                ColorMatrix(
                    floatArrayOf(
                        0.72f, 0f, 0f, 0f, 0.28f * 244,
                        0f, 0.72f, 0f, 0f, 0.28f * 243,
                        0f, 0f, 0.72f, 0f, 0.28f * 236,
                        0f, 0f, 0f, 1f, 0f,
                    ),
                ),
            )
        },
    ),
)
```

It does not solve the OSM tile usage policy; a different raster tile URL would still be needed for release.

## Sources (checked 2026-11-07)

- OpenFreeMap quick start: https://openfreemap.org/quick_start/
- OpenFreeMap styles fork (style status): https://github.com/niyamaka/openfreemap-styles
- MapLibre Compose: https://klibs.io/project/maplibre/maplibre-compose
- MapLibre Compose v0.18 announcement: https://slack-chats.kotlinlang.org/t/34453530/i-ve-just-released-v0-18-0-of-https-maplibre-org-maplibre-co
- Protomaps themes: https://docs.protomaps.com/basemaps/themes
- Protomaps with MapLibre: https://docs.protomaps.com/basemaps/maplibre
- MapLibre PMTiles guide: https://maplibre.org/flutter-maplibre-gl/advanced/pmtiles/
- OSM tile usage (discussion): https://help.openstreetmap.org/questions/62771/can-i-use-osm-tiles-in-android-or-not
- OSMF blog on tile usage: https://blog.openstreetmap.org/2011/11/01/tile-usage-policy/
