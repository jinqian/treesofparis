# 5. Vector map with MapLibre Native and OpenFreeMap

Status: accepted (2026-11-09). Supersedes the osmdroid choice of ADR 0002 (the contract stays).

## Context
- The osmdroid map loaded raster tiles from `tile.openstreetmap.org`. The OSM Foundation's tile policy does not let
  a published app rely on that server, so the app could not ship to Google Play with it.
- The standard OSM tiles are bright and full of icons; the « Herbier » interface looked pasted on top.
- Pins drawn by osmdroid merged into continuous bands at medium zoom.
- Plan: `docs/superpowers/plans/2026-11-07-maplibre-herbier-map.md`.

## Decision
- `:core:map-maplibre` implements `MapProvider` with **MapLibre Native Android** (`org.maplibre.gl:android-sdk`,
  stable 13.x line), in an `AndroidView` like the osmdroid provider. Not MapLibre Compose: younger, with breaking
  changes between minor versions, and not needed for one view.
- Tiles: **OpenFreeMap** public instance (OpenMapTiles schema, `https://tiles.openfreemap.org/planet`). Free, no
  key, no limit on map views, commercial use allowed; provided as is, without SLA.
- Style: our own « Herbier » style (`HerbierMapStyle.kt`), built in Kotlin from one table of light and dark colours
  (`BasemapColors`), plus the pin colours of the feature's `MapPalette`: paper land, sage parks, soft blue water,
  French labels (`name:fr`), no points of interest, shields or borders. Light or dark follows the system.
- Pins: one GeoJSON source and GPU circle layers (trees, remarkable on top, selected on top), sized by zoom; taps
  query the layers top first.
- Attribution, always visible on the map: « OpenFreeMap · © OpenMapTiles · © OpenStreetMap », each a link.
- `MapCamera.zoom` is the web-map zoom on a 256 dp world; MapLibre (512 dp tiles) uses one level less.

## Consequences
- The app can ship: the tile source allows it. If OpenFreeMap goes away or limits use, the fallback is self-hosted
  Protomaps PMTiles (plan Task 7), which also gives offline use.
- Native libraries add a few MB per ABI; Google Play's per-device APKs keep the download to one ABI.
- Without network the map shows the paper background, the pins and the user's position: no crash, no blank screen.
- `org.maplibre` imports are allowed only in `:core:map-maplibre` (Detekt `ForbiddenImport`); features never depend
  on it (`checkModuleBoundaries`).
- `:core:map-osmdroid` stays until the new map is accepted on a device, then it is removed in its own commit.
