# 2. Provider-neutral map contract

Status: accepted (2026-11-06). The contract stands; its osmdroid implementation was replaced by MapLibre
(ADR 0005), so `:core:map-maplibre` now plays the role described for `:core:map-osmdroid` below.

## Context
The MVP uses osmdroid (OpenStreetMap raster tiles, no API key). The map provider may change later
(MapLibre, Google Maps), and that should not mean rewriting the features.

## Decision
- `:core:map` defines a declarative contract: `MapProvider` (a composable `Content(...)`), plain data types
  (`MapCamera`, `CameraRequest`, `MapMarker`, `MapConfig`) and `TreeMapView`, which delegates to
  `LocalMapProvider`.
- `:core:map-osmdroid` is the only module that imports `org.osmdroid`. `:app` provides the implementation
  through `LocalMapProvider` — the single wiring point.
- Camera state is one-directional: `initialCamera` once, `CameraRequest(id, camera)` for programmatic moves,
  gestures reported through `onCameraChange`. This avoids feedback loops between ViewModel and map view.

## Consequences
- Swapping providers = a new `:core:map-<provider>` module implementing `MapProvider` + one line in `:app`.
- Contract types stay minimal; they grow only when a feature needs something.
- Enforced by Detekt `ForbiddenImport` and the `checkModuleBoundaries` Gradle task.
