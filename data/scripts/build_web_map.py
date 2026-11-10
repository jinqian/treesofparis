#!/usr/bin/env python3
"""Cut the app's `trees.db` into the small JSON files the web map (website/maps/) loads as the user moves.

The trees are split on a fixed grid of cells about 1 km wide; the page fetches only the cells it shows, from street
level (as the app does). Below street level it shows groups of trees instead: `clusters.json` counts the trees on three
coarser grids (about 2 km, 1 km, 500 m and 250 m), each group placed at the mean position of its trees. Remarkable trees go
in one extra file so the page can show them at every zoom, and the Wikipedia titles in another.

Standard library only. Usage (from the repository root):

    python3 data/scripts/build_web_map.py             # writes website/maps/data/ (git-ignored)

The Pages workflow (.github/workflows/pages.yml) runs it before each deployment, so the site always matches the
`trees.db` on main.
"""

from __future__ import annotations

import argparse
import json
import math
import re
import shutil
import sqlite3
import sys
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
DB = REPO / "core" / "data" / "src" / "main" / "assets" / "trees.db"
META = REPO / "core" / "data" / "trees-db-meta.json"
OUT = REPO / "website" / "maps" / "data"

# Must match GeoBounds.COVERAGE in :core:domain and COVERAGE in update_trees.py: south, west, north, east.
COVERAGE = (48.735, 2.150, 48.920, 2.480)
# A cell: 0.01° of latitude × 0.015° of longitude, about 1.1 km × 1.1 km in Paris.
CELL_LAT = 0.01
CELL_LON = 0.015
DECIMALS = 6  # about 10 cm
# Tree groups shown below street level, coarsest first: (level name, group radius in ° of latitude, of longitude).
CLUSTER_GRIDS = (
    ("district", 0.02, 0.03),
    ("coarse", 0.01, 0.015),
    ("mid", 0.005, 0.0075),
    ("fine", 0.0025, 0.00375),
)

# Row fields, in the order each tree is written (an array per tree keeps the files small).
FIELDS = (
    "id",
    "lat",
    "lon",
    "name",
    "genus",
    "species",
    "cultivar",
    "circumferenceCm",
    "heightM",
    "stage",
    "remarkable",
    "address",
    "area",
    "heightClass",
    "source",
)

# --- French place names (same rules as fr.arbresdeparis.domain.format.PlaceNames) ---------------------------------

PARTICLES = {"de", "du", "des", "la", "le", "les", "et", "sur", "sous", "au", "aux", "en", "à"}
ROMAN_NUMERAL = re.compile(r"^[ivx]{2,}$")
ELISION = re.compile(r"^([dl])'(.+)$")
ARRONDISSEMENT = re.compile(r"^PARIS (\d{1,2})(?:ER|E) ARRDT$")


def format_area(raw: str) -> str:
    """"PARIS 19E ARRDT" → "Paris 19e", "PARIS 1ER ARRDT" → "Paris 1er", "SEINE-SAINT-DENIS" → "Seine-Saint-Denis"."""
    match = ARRONDISSEMENT.fullmatch(raw.strip())
    if not match:
        return format_place(raw)
    number = int(match.group(1))
    return f"Paris {number}{'er' if number == 1 else 'e'}"


def _title_compound(word: str, first: bool) -> str:
    parts = []
    for i, part in enumerate(word.split("-")):
        elision = ELISION.match(part)
        if elision:  # « d'Avray », « l'Est »; the article keeps its capital only at the very start
            article = elision.group(1).upper() if first and i == 0 else elision.group(1)
            parts.append(f"{article}'{elision.group(2)[:1].upper()}{elision.group(2)[1:]}")
        elif i > 0 and part in PARTICLES:
            parts.append(part)
        else:
            parts.append(part[:1].upper() + part[1:])
    return "-".join(parts)


def format_place(raw: str) -> str:
    """Dataset text to French title case: "QUAI DE LA SEINE" → "Quai de la Seine", "VILLE-D'AVRAY" → "Ville-d'Avray"."""
    words = []
    for index, word in enumerate(re.sub(r"['’]+", "'", raw.strip()).lower().split(" ")):
        if index > 0 and word == "a":
            words.append("à")  # the dataset has no accents: a lone « A » is the preposition « à »
        elif index > 0 and word in PARTICLES:
            words.append(word)
        elif index > 0 and ROMAN_NUMERAL.match(word):
            words.append(word.upper())
        else:
            words.append(_title_compound(word, first=index == 0))
    return " ".join(words)


# --- cells -----------------------------------------------------------------------------------------------------------


def cell_of(lat: float, lon: float) -> str:
    """The key of the grid cell holding a point, « row_column » counted from the south-west corner of COVERAGE."""
    south, west, _, _ = COVERAGE
    return f"{int((lat - south) // CELL_LAT)}_{int((lon - west) // CELL_LON)}"


def tree_row(row: sqlite3.Row) -> list:
    return [
        row["id"],
        round(row["lat"], DECIMALS),
        round(row["lon"], DECIMALS),
        row["common_name"],
        row["genus"],
        row["species"],
        row["cultivar"],
        row["circumference_cm"],
        row["height_m"],
        row["stage"],
        1 if row["remarkable"] else 0,
        format_place(row["address"]) if row["address"] else None,
        format_area(row["arrondissement"]) if row["city"] == "Paris" else row["city"],
        row["height_class"],
        row["source"],
    ]


def clusters(points: list[tuple[float, float]]) -> dict[str, list]:
    """For each level of CLUSTER_GRIDS: groups of trees placed where the trees gather, as [lat, lon, count, size].

    The trees are counted in cells a third of the level's radius; then, densest cell first, each cell not yet taken
    gathers every free cell within the radius into one group, at the mean position of its trees (the app does the
    same: GetTreeClusters). Groups so sit on parks and avenues rather than on a grid, more than a radius apart. `size`
    (0–1) is the square root of the count over the level's largest count: the page sizes discs by area."""
    levels = {}
    for name, radius_lat, radius_lon in CLUSTER_GRIDS:
        cell_lat, cell_lon = radius_lat / 3, radius_lon / 3
        sums: dict[tuple[int, int], list] = {}
        for lat, lon in points:
            total = sums.setdefault((int(lat // cell_lat), int(lon // cell_lon)), [0.0, 0.0, 0])
            total[0] += lat
            total[1] += lon
            total[2] += 1
        cells = [(la / n, lo / n, n) for la, lo, n in sums.values()]
        groups = gather(cells, radius_lat, radius_lon)
        largest = max((g[2] for g in groups), default=1)
        levels[name] = [
            [round(la, 5), round(lo, 5), n, round(math.sqrt(n / largest), 3)]
            for la, lo, n in sorted(groups, key=lambda g: -g[2])
        ]
    return levels


def gather(cells: list[tuple], radius_lat: float, radius_lon: float) -> list[tuple]:
    """Densest first, each free (lat, lon, count) cell takes every free cell within the radius ellipse; returns the
    groups as (lat, lon, count) at the mean position of their trees."""
    ordered = sorted(cells, key=lambda c: (-c[2], c[0], c[1]))
    buckets: dict[tuple[int, int], list[int]] = {}
    for index, (lat, lon, _) in enumerate(ordered):
        buckets.setdefault((math.floor(lat / radius_lat), math.floor(lon / radius_lon)), []).append(index)
    taken = [False] * len(ordered)
    groups = []
    for seed, (seed_lat, seed_lon, _) in enumerate(ordered):
        if taken[seed]:
            continue
        row, column = math.floor(seed_lat / radius_lat), math.floor(seed_lon / radius_lon)
        members = [
            index
            for d_row in (-1, 0, 1)
            for d_column in (-1, 0, 1)
            for index in buckets.get((row + d_row, column + d_column), ())
            if not taken[index]
            and ((ordered[index][0] - seed_lat) / radius_lat) ** 2 + ((ordered[index][1] - seed_lon) / radius_lon) ** 2
            <= 1
        ]
        for index in members:
            taken[index] = True
        count = sum(ordered[i][2] for i in members)
        lat = sum(ordered[i][0] * ordered[i][2] for i in members) / count
        lon = sum(ordered[i][1] * ordered[i][2] for i in members) / count
        groups.append((lat, lon, count))
    return groups


def build(db: Path, meta_file: Path, out: Path) -> dict:
    """Writes the cells, remarkable trees, Wikipedia titles and an index to `out`; returns the index."""
    con = sqlite3.connect(f"file:{db}?mode=ro", uri=True)
    con.row_factory = sqlite3.Row
    cells: dict[str, list] = {}
    remarkable = []
    points = []
    for row in con.execute("SELECT * FROM trees ORDER BY id"):
        tree = tree_row(row)
        points.append((row["lat"], row["lon"]))
        cells.setdefault(cell_of(row["lat"], row["lon"]), []).append(tree)
        if row["remarkable"]:
            remarkable.append(tree)
    taxa = {
        f"{r['genus']}|{r['species']}": [r["wikipedia_title"], 1 if r["genus_only"] else 0]
        for r in con.execute("SELECT * FROM taxon_links")
    }
    con.close()

    if out.exists():
        shutil.rmtree(out)
    (out / "cells").mkdir(parents=True)
    for key, trees in cells.items():
        _write(out / "cells" / f"{key}.json", trees)
    _write(out / "remarkable.json", remarkable)
    _write(out / "clusters.json", clusters(points))
    _write(out / "taxa.json", taxa)

    meta = json.loads(meta_file.read_text(encoding="utf-8")) if meta_file.exists() else {}
    index = {
        "fields": list(FIELDS),
        "coverage": list(COVERAGE),
        "cell": [CELL_LAT, CELL_LON],
        "cells": sorted(cells),
        "treeCount": sum(len(t) for t in cells.values()),
        "remarkableCount": len(remarkable),
        "extractedAt": meta.get("extractedAt"),
        "dataVersion": meta.get("dataVersion"),
    }
    _write(out / "index.json", index)
    return index


def _write(path: Path, value) -> None:
    path.write_text(json.dumps(value, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--db", type=Path, default=DB)
    parser.add_argument("--meta", type=Path, default=META)
    parser.add_argument("--output", type=Path, default=OUT)
    args = parser.parse_args(argv)
    index = build(args.db, args.meta, args.output)
    size = sum(f.stat().st_size for f in args.output.rglob("*.json"))
    print(
        f"{index['treeCount']} trees in {len(index['cells'])} cells, {index['remarkableCount']} remarkable; "
        f"wrote {args.output.relative_to(REPO) if args.output.is_relative_to(REPO) else args.output} "
        f"({size / 1e6:.1f} MB)"
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
