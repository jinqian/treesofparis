"""The towns of Paris and the inner suburbs (départements 75, 92, 93, 94), to tell which town a tree stands in.

`data/communes.geojson` holds their boundaries from geo.api.gouv.fr (IGN Admin Express, Licence Ouverte). It changes
only when towns merge or are renamed; refresh it with `python3 data/scripts/update_trees.py --refresh-communes`.
Standard library only.
"""

from __future__ import annotations

import json
import urllib.request
from dataclasses import dataclass
from pathlib import Path

DEPARTEMENTS = ("75", "92", "93", "94")
API = "https://geo.api.gouv.fr/departements/{}/communes?fields=nom,code&format=geojson&geometry=contour"


@dataclass(frozen=True)
class Commune:
    code: str  # INSEE code, e.g. « 92040 »
    name: str  # official name, e.g. « Issy-les-Moulineaux »
    polygons: tuple  # of polygons; each a tuple of rings (outer first, then holes) of (lon, lat) points
    box: tuple  # west, south, east, north


def _inside(lon: float, lat: float, ring) -> bool:
    """Ray casting: is the point inside this closed ring of (lon, lat) points?"""
    inside = False
    j = len(ring) - 1
    for i in range(len(ring)):
        xi, yi = ring[i]
        xj, yj = ring[j]
        if (yi > lat) != (yj > lat) and lon < (xj - xi) * (lat - yi) / (yj - yi) + xi:
            inside = not inside
        j = i
    return inside


class Communes:
    def __init__(self, communes: list[Commune]):
        self._communes = communes
        self._by_code = {c.code: c for c in communes}

    @classmethod
    def load(cls, path: Path) -> Communes:
        features = json.loads(path.read_text(encoding="utf-8"))["features"]
        communes = []
        for feature in features:
            geometry = feature["geometry"]
            polygons = [geometry["coordinates"]] if geometry["type"] == "Polygon" else geometry["coordinates"]
            outer = [point for polygon in polygons for point in polygon[0]]
            box = (
                min(p[0] for p in outer),
                min(p[1] for p in outer),
                max(p[0] for p in outer),
                max(p[1] for p in outer),
            )
            rings = tuple(tuple(tuple(tuple(p) for p in ring) for ring in polygon) for polygon in polygons)
            communes.append(Commune(feature["properties"]["code"], feature["properties"]["nom"], rings, box))
        return cls(communes)

    def name_of(self, code: str) -> str | None:
        commune = self._by_code.get(code)
        return commune.name if commune else None

    def locate(self, lat: float, lon: float) -> str | None:
        """The name of the town holding the point, or None outside the four départements."""
        for commune in self._communes:
            west, south, east, north = commune.box
            if not (west <= lon <= east and south <= lat <= north):
                continue
            for polygon in commune.polygons:
                if _inside(lon, lat, polygon[0]) and not any(_inside(lon, lat, hole) for hole in polygon[1:]):
                    return commune.name
        return None


def fetch(path: Path, user_agent: str) -> int:
    """Downloads the boundaries of the four départements into one GeoJSON file; returns the number of towns."""
    features = []
    for departement in DEPARTEMENTS:
        request = urllib.request.Request(API.format(departement), headers={"User-Agent": user_agent})
        with urllib.request.urlopen(request, timeout=120) as response:
            features += json.load(response)["features"]
    features.sort(key=lambda f: f["properties"]["code"])
    collection = {"type": "FeatureCollection", "features": features}
    path.write_text(json.dumps(collection, ensure_ascii=False, separators=(",", ":")) + "\n", encoding="utf-8")
    return len(features)
