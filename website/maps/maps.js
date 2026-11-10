// The web map of arbresdeparis.fr: the same trees and « Herbier » map as the Android app, in the browser.
// Trees come from data/ (built from the app's trees.db by data/scripts/build_web_map.py): one file per ~1 km cell,
// fetched from street level, plus the remarkable trees, shown at every zoom.

import {
  CLUSTER_LAYERS,
  CLUSTER_LEVELS,
  CLUSTERS_SOURCE,
  herbierStyle,
  PIN_LAYERS,
  PINS_SOURCE,
  SELECTED_SOURCE,
  USER_SOURCE,
} from "./style.js";

/* global maplibregl */

// Street level, as in the app (its zoom 15.5 on 256 px tiles is 14.5 on MapLibre's 512 px tiles).
const TREE_MIN_ZOOM = 14.5;
const PARIS = [2.3522, 48.8566];
const WIKIPEDIA = "https://fr.wikipedia.org/wiki/";

const F = { id: 0, lat: 1, lon: 2, name: 3, genus: 4, species: 5, cultivar: 6, circ: 7, height: 8, stage: 9, remarkable: 10, address: 11, area: 12 };

const $ = (selector) => document.querySelector(selector);
const nf = new Intl.NumberFormat("fr-FR");

const state = {
  index: null,
  taxa: {},
  remarkable: [],
  clusters: null, // the tree groups below street level, as GeoJSON
  cells: new Map(), // key → rows
  pending: new Map(), // key → Promise
  byId: new Map(),
  onlyRemarkable: false,
  selected: null,
  user: null,
};

const darkQuery = window.matchMedia("(prefers-color-scheme: dark)");
const mode = () => (darkQuery.matches ? "dark" : "light");

const map = new maplibregl.Map({
  container: "map",
  style: herbierStyle(mode()),
  center: PARIS,
  zoom: 12,
  minZoom: 10,
  maxZoom: 19,
  hash: "carte",
  attributionControl: false,
  dragRotate: false,
  pitchWithRotate: false,
  touchPitch: false,
});
map.touchZoomRotate.disableRotation();
map.addControl(new maplibregl.NavigationControl({ showCompass: false }), "bottom-right");
map.addControl(
  new maplibregl.AttributionControl({
    compact: true,
    // OpenFreeMap's tiles bring their own credits (OpenFreeMap, OpenMapTiles, OpenStreetMap).
    customAttribution:
      'Arbres : <a href="https://opendata.paris.fr/explore/dataset/les-arbres/information/">Ville de Paris</a> (ODbL)',
  }),
  "bottom-left",
);

// --- data -------------------------------------------------------------------------------------------------------------

async function getJson(path) {
  const response = await fetch(path);
  if (!response.ok) throw new Error(`${path}: ${response.status}`);
  return response.json();
}

function remember(rows) {
  for (const row of rows) state.byId.set(row[F.id], row);
  return rows;
}

function loadCell(key) {
  if (state.cells.has(key)) return Promise.resolve(state.cells.get(key));
  if (!state.pending.has(key)) {
    const promise = getJson(`data/cells/${key}.json`)
      .then((rows) => {
        state.cells.set(key, remember(rows));
        return rows;
      })
      .finally(() => state.pending.delete(key));
    state.pending.set(key, promise);
  }
  return state.pending.get(key);
}

/** The keys of the cells that hold data and touch the visible area. */
function visibleCells() {
  const { coverage, cell, cells } = state.index;
  const [south, west] = coverage;
  const b = map.getBounds();
  const rows = [Math.floor((b.getSouth() - south) / cell[0]), Math.floor((b.getNorth() - south) / cell[0])];
  const cols = [Math.floor((b.getWest() - west) / cell[1]), Math.floor((b.getEast() - west) / cell[1])];
  const known = new Set(cells);
  const keys = [];
  for (let r = rows[0]; r <= rows[1]; r++) {
    for (let c = cols[0]; c <= cols[1]; c++) if (known.has(`${r}_${c}`)) keys.push(`${r}_${c}`);
  }
  return keys;
}

const feature = (row) => ({
  type: "Feature",
  geometry: { type: "Point", coordinates: [row[F.lon], row[F.lat]] },
  properties: { id: row[F.id], r: row[F.remarkable] },
});

const collection = (rows) => ({ type: "FeatureCollection", features: rows.map(feature) });

let refreshToken = 0;

async function refresh() {
  if (!state.index) return;
  const token = ++refreshToken;
  const street = map.getZoom() >= TREE_MIN_ZOOM;
  $("#hint").hidden = street || state.onlyRemarkable;
  for (const id of CLUSTER_LAYERS) {
    if (map.getLayer(id)) {
      const visibility = state.onlyRemarkable ? "none" : "visible";
      map.setLayoutProperty(id, "visibility", visibility);
      map.setLayoutProperty(`${id}-count`, "visibility", visibility);
    }
  }
  let rows = state.remarkable;
  if (street && !state.onlyRemarkable) {
    const keys = visibleCells();
    $("#loading").hidden = keys.every((key) => state.cells.has(key));
    try {
      const loaded = await Promise.all(keys.map(loadCell));
      rows = loaded.flat();
    } catch (error) {
      console.error(error);
      showToast("Impossible de charger les arbres. Vérifiez votre connexion.");
    }
    if (token !== refreshToken) return; // a newer move took over
    $("#loading").hidden = true;
  }
  map.getSource(PINS_SOURCE)?.setData(collection(rows));
}

/** The groups of data/clusters.json as one GeoJSON collection; each point knows its level, count and label. */
function clusterCollection(levels) {
  const features = Object.entries(levels).flatMap(([level, groups]) =>
    groups.map(([lat, lon, n, size]) => ({
      type: "Feature",
      geometry: { type: "Point", coordinates: [lon, lat] },
      properties: { level, n, size, label: nf.format(n).replace(/\s/g, "\u00a0") },
    })),
  );
  return { type: "FeatureCollection", features };
}

function drawClusters() {
  if (state.clusters) map.getSource(CLUSTERS_SOURCE)?.setData(state.clusters);
}

function drawSelectionAndUser() {
  map.getSource(SELECTED_SOURCE)?.setData(collection(state.selected ? [state.selected] : []));
  const user = state.user ? [{ type: "Feature", geometry: { type: "Point", coordinates: state.user }, properties: {} }] : [];
  map.getSource(USER_SOURCE)?.setData({ type: "FeatureCollection", features: user });
}

// --- tree sheet -------------------------------------------------------------------------------------------------------

const escapeHtml = (text) =>
  String(text).replace(/[&<>"']/g, (ch) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[ch]);

function scientificName(row) {
  const latin = [row[F.genus], row[F.species]].filter(Boolean).join(" ");
  if (!latin) return null;
  return row[F.cultivar] ? `${latin} '${row[F.cultivar]}'` : latin;
}

function wikipedia(row) {
  if (!row[F.genus]) return null;
  const link = state.taxa[`${row[F.genus]}|${row[F.species] ?? ""}`] ?? state.taxa[`${row[F.genus]}|`];
  if (!link) return null;
  const [title, genusOnly] = link;
  return { title, genusOnly: genusOnly === 1, url: WIKIPEDIA + encodeURIComponent(title.replaceAll(" ", "_")) };
}

function extractionDate() {
  const at = state.index?.extractedAt;
  return at ? new Date(at).toLocaleDateString("fr-FR", { day: "numeric", month: "long", year: "numeric", timeZone: "Europe/Paris" }) : null;
}

function measure(label, value, unit) {
  return `<div class="fact"><dt>${label}</dt><dd>${value == null ? "<span class=\"unknown\">inconnue</span>" : `${nf.format(value)}<small>${unit}</small>`}</dd></div>`;
}

function openSheet(row) {
  state.selected = row;
  drawSelectionAndUser();
  const latin = scientificName(row);
  const wiki = wikipedia(row);
  const place = [row[F.area], row[F.address]].filter(Boolean).map(escapeHtml).join(" · ");
  $("#sheet-body").innerHTML = `
    ${row[F.remarkable] ? '<p class="badge">Arbre remarquable</p>' : ""}
    <h2 id="sheet-title">${escapeHtml(row[F.name])}</h2>
    ${latin ? `<p class="latin">${escapeHtml(latin)}</p>` : '<p class="latin unknown">Espèce inconnue</p>'}
    <p class="place">${place}</p>
    <dl class="facts">
      ${measure("Hauteur", row[F.height], "m")}
      ${measure("Circonférence", row[F.circ], "cm")}
      <div class="fact"><dt>Stade</dt><dd>${row[F.stage] ? escapeHtml(row[F.stage]) : '<span class="unknown">inconnu</span>'}</dd></div>
    </dl>
    ${
      wiki
        ? `<a class="wiki" href="${wiki.url}" target="_blank" rel="noopener">
             <span>${wiki.genusOnly ? `Le genre ${escapeHtml(row[F.genus])} sur Wikipédia` : "En savoir plus sur Wikipédia"}</span>
             <small>${escapeHtml(wiki.title)} · fr.wikipedia.org</small>
           </a>`
        : ""
    }
    <p class="source">Arbre n° ${row[F.id]} · Source : « Les arbres », Paris Data</p>`;
  $("#sheet").hidden = false;
  $("#sheet").scrollTop = 0;
  keepInSight(row);
}

/** Pans the map when the sheet covers the tree: to the middle of what is left (right of the card, or above the sheet). */
function keepInSight(row) {
  const sheet = $("#sheet").getBoundingClientRect();
  const point = map.project([row[F.lon], row[F.lat]]);
  const covered = point.x >= sheet.left && point.x <= sheet.right && point.y >= sheet.top && point.y <= sheet.bottom;
  if (!covered) return;
  const { clientWidth: width, clientHeight: height } = map.getContainer();
  const sideCard = sheet.width < width;
  const target = sideCard ? [(sheet.right + width) / 2, height / 2] : [width / 2, sheet.top / 2 + 30];
  map.panBy([point.x - target[0], point.y - target[1]]);
}

function closeSheet() {
  state.selected = null;
  $("#sheet").hidden = true;
  drawSelectionAndUser();
}

// --- controls ---------------------------------------------------------------------------------------------------------

let toastTimer;
function showToast(message) {
  const toast = $("#toast");
  toast.textContent = message;
  toast.hidden = false;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => (toast.hidden = true), 4000);
}

function insideCoverage([lon, lat]) {
  const [south, west, north, east] = state.index.coverage;
  return lat >= south && lat <= north && lon >= west && lon <= east;
}

function locate() {
  if (!("geolocation" in navigator)) return showToast("Votre navigateur ne donne pas la position.");
  const button = $("#locate");
  button.setAttribute("aria-busy", "true");
  navigator.geolocation.getCurrentPosition(
    ({ coords }) => {
      button.removeAttribute("aria-busy");
      state.user = [coords.longitude, coords.latitude];
      drawSelectionAndUser();
      if (!state.index || insideCoverage(state.user)) {
        map.flyTo({ center: state.user, zoom: Math.max(map.getZoom(), 16.5) });
      } else {
        showToast("Vous êtes en dehors de la zone couverte par la carte.");
      }
    },
    (error) => {
      button.removeAttribute("aria-busy");
      showToast(
        error.code === error.PERMISSION_DENIED
          ? "Autorisez l’accès à la position pour vous localiser."
          : "Position indisponible. Vérifiez que la localisation est activée.",
      );
    },
    { enableHighAccuracy: true, timeout: 15000, maximumAge: 30000 },
  );
}

function toggleRemarkable() {
  state.onlyRemarkable = !state.onlyRemarkable;
  $("#only-remarkable").setAttribute("aria-pressed", String(state.onlyRemarkable));
  refresh();
}

// --- wiring -----------------------------------------------------------------------------------------------------------

map.on("click", (event) => {
  const existing = (ids) => ids.filter((id) => map.getLayer(id));
  // What is drawn right under the finger wins (topmost first), then a tree pin within 8 px: pins are small.
  const exact = map.queryRenderedFeatures(event.point, { layers: existing([...PIN_LAYERS, ...CLUSTER_LAYERS]) })[0];
  const box = [
    [event.point.x - 8, event.point.y - 8],
    [event.point.x + 8, event.point.y + 8],
  ];
  const hit = exact ?? map.queryRenderedFeatures(box, { layers: existing(PIN_LAYERS) })[0];
  if (hit?.properties.level) {
    const { next } = CLUSTER_LEVELS.find(({ level }) => level === hit.properties.level);
    return map.easeTo({ center: hit.geometry.coordinates, zoom: next });
  }
  const row = hit && state.byId.get(hit.properties.id);
  if (row) return openSheet(row);
  if (!$("#sheet").hidden) closeSheet();
});
for (const id of [...PIN_LAYERS, ...CLUSTER_LAYERS]) {
  map.on("mouseenter", id, () => (map.getCanvas().style.cursor = "pointer"));
  map.on("mouseleave", id, () => (map.getCanvas().style.cursor = ""));
}
map.on("moveend", refresh);

// On phones, start with the map credits folded behind their « i » button: open, they cover the bottom of the map.
map.once("load", () => {
  if (window.innerWidth >= 720) return;
  const attribution = map.getContainer().querySelector(".maplibregl-ctrl-attrib");
  attribution?.classList.remove("maplibregl-compact-show");
  attribution?.removeAttribute("open");
});

// A new colour scheme rebuilds the style, which empties the sources: draw the pins again once it is in.
darkQuery.addEventListener("change", () => {
  map.setStyle(herbierStyle(mode()), { diff: false });
  map.once("style.load", () => {
    drawClusters();
    refresh();
    drawSelectionAndUser();
  });
});

$("#locate").addEventListener("click", locate);
$("#hint").addEventListener("click", () => map.easeTo({ zoom: TREE_MIN_ZOOM + 0.7 }));
$("#only-remarkable").addEventListener("click", toggleRemarkable);
$("#sheet-close").addEventListener("click", closeSheet);
document.addEventListener("keydown", (event) => {
  if (event.key === "Escape" && !$("#sheet").hidden) closeSheet();
});

async function start() {
  try {
    const [index, taxa, remarkable, clusters] = await Promise.all([
      getJson("data/index.json"),
      getJson("data/taxa.json"),
      getJson("data/remarkable.json"),
      getJson("data/clusters.json"),
    ]);
    state.clusters = clusterCollection(clusters);
    state.index = index;
    state.taxa = taxa;
    state.remarkable = remember(remarkable);
    const [south, west, north, east] = index.coverage;
    map.setMaxBounds([
      [west - 0.05, south - 0.04],
      [east + 0.05, north + 0.04],
    ]);
    $("#count").textContent = nf.format(index.treeCount);
    const date = extractionDate();
    if (date) $("#extraction").textContent = `Données du ${date}`;
  } catch (error) {
    console.error(error);
    showToast("Impossible de charger les arbres. Vérifiez votre connexion.");
    return;
  }
  const draw = () => {
    drawClusters();
    refresh();
  };
  if (map.isStyleLoaded()) draw();
  else map.once("load", draw);
}

start();
