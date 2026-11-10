// The « Herbier » map style: a copy of the app's HerbierMapStyle.kt (:core:map-maplibre) and its pin colours
// (TreesColors / HerbierPalette in :core:designsystem). Keep both in step when one changes.

const OPENFREEMAP_TILES = "https://tiles.openfreemap.org/planet";
const OPENFREEMAP_GLYPHS = "https://tiles.openfreemap.org/fonts/{fontstack}/{range}.pbf";
const FONT_REGULAR = "Noto Sans Regular";
const FONT_ITALIC = "Noto Sans Italic";
const FONT_BOLD = "Noto Sans Bold";
const VECTOR = "openmaptiles";

export const PINS_SOURCE = "pins";
export const SELECTED_SOURCE = "selected";
export const USER_SOURCE = "user";
export const CLUSTERS_SOURCE = "clusters";
export const PIN_LAYERS = ["pins-selected", "pins-remarkable", "pins-tree"]; // topmost first, for taps

/**
 * Tree groups below street level (data/clusters.json: about 2 km, 1 km, 500 m and 250 m), each shown between two zooms;
 * the trees themselves take over at 14.5. `radius` is the largest disc's radius at the level's first and last zoom,
 * about 0.4 of the spacing between groups there, so neighbours do not touch. A tap zooms to `next`.
 */
export const CLUSTER_LEVELS = [
  { level: "district", minzoom: 0, maxzoom: 11.5, radius: [16, 30], next: 11.9 },
  { level: "coarse", minzoom: 11.5, maxzoom: 12.8, radius: [24, 34], next: 13.2 },
  { level: "mid", minzoom: 12.8, maxzoom: 13.8, radius: [28, 34], next: 14.2 },
  { level: "fine", minzoom: 13.8, maxzoom: 14.5, radius: [28, 34], next: 15.2 },
];
const CLUSTER_MIN_RADIUS = 9;
export const CLUSTER_LAYERS = CLUSTER_LEVELS.map(({ level }) => `clusters-${level}`);

export const COLORS = {
  light: {
    land: "#F4F3EC", green: "#DCE4D2", water: "#CFDDE0", building: "#E8E6DC", road: "#FBFAF6",
    casing: "#E2E1D8", rail: "#D3D2C8", label: "#5C665D", labelHalo: "#F4F3EC", place: "#1E2620",
    tree: "#4F7556", remarkable: "#C9962E", selected: "#1E2620", ring: "#FBFAF6", user: "#4A7FA8",
    group: "#3F5F45", groupLabel: "#F4F3EC",
  },
  dark: {
    land: "#1A1F1B", green: "#20291E", water: "#1B2A2E", building: "#222823", road: "#272D28",
    casing: "#2E352F", rail: "#2E352F", label: "#A3AB9F", labelHalo: "#1A1F1B", place: "#E6E8E1",
    tree: "#7FA286", remarkable: "#D4A443", selected: "#E6E8E1", ring: "#141815", user: "#7FB0D6",
    // The night pins are a light green: groups use the day pin green, so the white count stays legible.
    group: "#4F7556", groupLabel: "#F4F3EC",
  },
};

const byZoom = (...stops) => ["interpolate", ["linear"], ["zoom"], ...stops.flat()];
const grow = (...stops) => ["interpolate", ["exponential", 1.4], ["zoom"], ...stops.flat()];
const classIn = (...classes) => ["match", ["get", "class"], classes, true, false];
const notTunnel = ["!=", ["get", "brunnel"], "tunnel"];
const frenchName = ["coalesce", ["get", "name:fr"], ["get", "name"]];
const MAJOR = classIn("motorway", "trunk", "primary", "secondary", "tertiary");

const vector = (id, type, sourceLayer, extra) => ({ id, type, source: VECTOR, "source-layer": sourceLayer, ...extra });

const fill = (id, sourceLayer, color, { minzoom, filter, outline } = {}) =>
  vector(id, "fill", sourceLayer, {
    ...(minzoom != null && { minzoom }),
    ...(filter && { filter }),
    paint: { "fill-color": color, ...(outline && { "fill-outline-color": outline }) },
  });

const line = (id, color, { minzoom, filter, width, dashed = false, sourceLayer = "transportation" }) =>
  vector(id, "line", sourceLayer, {
    ...(minzoom != null && { minzoom }),
    ...(filter && { filter }),
    layout: { "line-cap": dashed ? "butt" : "round", "line-join": "round" },
    paint: { "line-color": color, "line-width": width, ...(dashed && { "line-dasharray": [2, 2] }) },
  });

const label = (id, sourceLayer, c, { minzoom, maxzoom, size, font = FONT_REGULAR, alongLine = false, color = c.label, opacity = 1, filter }) =>
  vector(id, "symbol", sourceLayer, {
    ...(minzoom != null && { minzoom }),
    ...(maxzoom != null && { maxzoom }),
    ...(filter && { filter }),
    layout: {
      "text-field": frenchName, "text-font": [font], "text-size": size, "text-max-width": 8,
      ...(alongLine && { "symbol-placement": "line" }),
    },
    paint: { "text-color": color, "text-halo-color": c.labelHalo, "text-halo-width": 1.2, "text-opacity": opacity },
  });

const circle = (id, source, color, radius, ring, ringWidth, filter) => ({
  id, type: "circle", source,
  ...(filter && { filter }),
  paint: {
    "circle-color": color, "circle-radius": radius, "circle-stroke-color": ring,
    "circle-stroke-width": ringWidth, "circle-pitch-alignment": "map",
  },
});

/** A soft disc per group, sized by its count (area grows with the number of trees), with the count inside. */
function clusterLayers(c) {
  const sized = (largest) => ["+", CLUSTER_MIN_RADIUS, ["*", ["get", "size"], largest - CLUSTER_MIN_RADIUS]];
  return CLUSTER_LEVELS.flatMap(({ level, minzoom, maxzoom, radius }) => {
    const common = { source: CLUSTERS_SOURCE, minzoom, maxzoom, filter: ["==", ["get", "level"], level] };
    return [
      {
        id: `clusters-${level}`, type: "circle", ...common,
        paint: {
          "circle-color": c.group,
          "circle-opacity": 0.92,
          "circle-radius": ["interpolate", ["linear"], ["zoom"], Math.max(minzoom, 10), sized(radius[0]), maxzoom, sized(radius[1])],
          "circle-stroke-color": c.ring,
          "circle-stroke-width": 1.5,
        },
      },
      {
        id: `clusters-${level}-count`, type: "symbol", ...common,
        layout: {
          "text-field": ["get", "label"], "text-font": [FONT_BOLD], "text-size": 11.5,
          "text-allow-overlap": true, "text-ignore-placement": true,
        },
        paint: { "text-color": c.groupLabel },
      },
    ];
  });
}

const empty = () => ({ type: "geojson", data: { type: "FeatureCollection", features: [] } });

/** The full style for `mode` ("light" or "dark"): basemap back to front, then trees and the user. */
export function herbierStyle(mode) {
  const c = COLORS[mode];
  const pinRing = byZoom([13, 0.8], [15, 1.2], [16.5, 2]);
  return {
    version: 8,
    name: "Herbier",
    glyphs: OPENFREEMAP_GLYPHS,
    sources: {
      [VECTOR]: { type: "vector", url: OPENFREEMAP_TILES },
      [PINS_SOURCE]: empty(),
      [SELECTED_SOURCE]: empty(),
      [USER_SOURCE]: empty(),
      [CLUSTERS_SOURCE]: empty(),
    },
    layers: [
      { id: "background", type: "background", paint: { "background-color": c.land } },
      fill("green-landcover", "landcover", c.green, { minzoom: 9, filter: classIn("grass", "wood") }),
      fill("green-park", "park", c.green),
      fill("green-cemetery", "landuse", c.green, { minzoom: 11, filter: classIn("cemetery") }),
      fill("water", "water", c.water, { filter: notTunnel }),
      line("waterway", c.water, { sourceLayer: "waterway", width: grow([10, 0.5], [18, 6]) }),
      fill("building", "building", c.building, { minzoom: 14, outline: c.casing }),
      line("road-casing", c.casing, { minzoom: 11, filter: ["all", notTunnel, MAJOR], width: grow([10, 3], [20, 24]) }),
      line("road-minor", c.road, {
        minzoom: 12, filter: ["all", notTunnel, classIn("minor", "service", "track")], width: grow([13, 1.8], [20, 20]),
      }),
      line("road-major", c.road, { minzoom: 6, filter: ["all", notTunnel, MAJOR], width: grow([10, 2], [20, 20]) }),
      line("road-path", c.road, { minzoom: 14, filter: ["all", notTunnel, classIn("path")], width: grow([14, 0.8], [20, 6]) }),
      line("rail", c.rail, {
        minzoom: 12, filter: ["all", notTunnel, classIn("rail", "transit")], width: grow([14, 1], [20, 4]), dashed: true,
      }),
      label("label-water", "water_name", c, { font: FONT_ITALIC, size: 13, alongLine: true }),
      label("label-street-major", "transportation_name", c, { minzoom: 12.5, size: 12, alongLine: true, filter: MAJOR }),
      label("label-street-minor", "transportation_name", c, {
        minzoom: 14, size: 11, alongLine: true, filter: classIn("minor", "service", "track"),
      }),
      label("label-quarter", "place", c, {
        minzoom: 12, size: 13, color: c.place, opacity: 0.7, filter: classIn("suburb", "quarter", "neighbourhood"),
      }),
      label("label-town", "place", c, {
        minzoom: 8, maxzoom: 13, size: 14, color: c.place, opacity: 0.7, filter: classIn("city", "town", "village"),
      }),
      ...clusterLayers(c),
      circle("pins-tree", PINS_SOURCE, c.tree, byZoom([13, 2.5], [15, 3.5], [16.5, 6]), c.ring, pinRing, ["==", ["get", "r"], 0]),
      circle("pins-remarkable", PINS_SOURCE, c.remarkable, byZoom([11, 4], [13, 5], [15, 6], [16.5, 8]), c.ring, pinRing, ["==", ["get", "r"], 1]),
      circle("pins-selected", SELECTED_SOURCE, c.selected, 9, c.ring, 2),
      circle("user", USER_SOURCE, c.user, 8, c.ring, 3),
    ],
  };
}
