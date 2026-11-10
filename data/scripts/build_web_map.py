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
COVERAGE = (48.735, 2.200, 48.920, 2.480)
# A cell: 0.01° of latitude × 0.015° of longitude, about 1.1 km × 1.1 km in Paris.
CELL_LAT = 0.01
CELL_LON = 0.015
DECIMALS = 6  # about 10 cm
# Tree groups shown below street level, coarsest first: (level name, cell height in °, cell width in °).
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
)

# --- French place names (same rules as fr.arbresdeparis.domain.format.PlaceNames) ---------------------------------

PARTICLES = {"de", "du", "des", "la", "le", "les", "et", "sur", "sous", "au", "aux", "en", "à"}
ROMAN_NUMERAL = re.compile(r"^[ivx]{2,}$")
ARRONDISSEMENT = re.compile(r"^PARIS (\d{1,2})(?:ER|E) ARRDT$")


def format_area(raw: str) -> str:
    """"PARIS 19E ARRDT" → "Paris 19e", "PARIS 1ER ARRDT" → "Paris 1er", "SEINE-SAINT-DENIS" → "Seine-Saint-Denis"."""
    match = ARRONDISSEMENT.fullmatch(raw.strip())
    if not match:
        return format_place(raw)
    number = int(match.group(1))
    return f"Paris {number}{'er' if number == 1 else 'e'}"


def _title_compound(word: str) -> str:
    parts = word.split("-")
    return "-".join(p if i > 0 and p in PARTICLES else p[:1].upper() + p[1:] for i, p in enumerate(parts))


def format_place(raw: str) -> str:
    """Upper-case dataset text to French title case: "QUAI DE LA SEINE" → "Quai de la Seine"."""
    words = []
    for index, word in enumerate(raw.strip().lower().split(" ")):
        if index > 0 and word == "a":
            words.append("à")  # the dataset has no accents: a lone « A » is the preposition « à »
        elif index > 0 and word in PARTICLES:
            words.append(word)
        elif index > 0 and ROMAN_NUMERAL.match(word):
            words.append(word.upper())
        else:
            words.append(_title_compound(word))
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
        format_area(row["arrondissement"]),
    ]


def clusters(points: list[tuple[float, float]]) -> dict[str, list]:
    """For each grid of CLUSTER_GRIDS: one [lat, lon, count, size] per non-empty cell, at the mean position of its
    trees. `size` (0–1) is the square root of the count over the level's largest count: the page sizes discs by area."""
    levels = {}
    for name, cell_lat, cell_lon in CLUSTER_GRIDS:
        sums: dict[tuple[int, int], list] = {}
        for lat, lon in points:
            total = sums.setdefault((int(lat // cell_lat), int(lon // cell_lon)), [0.0, 0.0, 0])
            total[0] += lat
            total[1] += lon
            total[2] += 1
        biggest_first = sorted(sums.values(), key=lambda s: -s[2])
        largest = biggest_first[0][2] if biggest_first else 1
        levels[name] = [
            [round(la / n, 5), round(lo / n, 5), n, round(math.sqrt(n / largest), 3)] for la, lo, n in biggest_first
        ]
    return levels


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
