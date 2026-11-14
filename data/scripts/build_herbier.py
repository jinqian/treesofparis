#!/usr/bin/env python3
"""Write the website's Herbier page (website/herbier/index.html) from the app's `trees.db` (issue #84).

The page shows what the app's Herbier tab shows, on one page: the totals, « 1 arbre sur 5 à Paris est un platane », the
drawn genera with their share of the trees, the flowering calendar, the remarkable trees by area and their records,
the trees' ages and the most wooded arrondissements. Its figures are about Paris only (`source = 'paris'`), as in
the app (TreeStatisticsDao). The page is plain HTML, so search engines read it; `herbier.js` only marks the month in
progress, which the build cannot know.

Nothing is copied from the app by hand: the leaf drawings (TreeLeafPaths.kt), the genus names (strings.xml of
:core:designsystem) and the flowering months (GuideFloweringRepository.kt) are read from its sources.

Standard library only. Usage (from the repository root):

    python3 data/scripts/build_herbier.py             # writes website/herbier/index.html (git-ignored)

The Pages workflow (.github/workflows/pages.yml) runs it before each deployment, like build_web_map.py.
"""

from __future__ import annotations

import argparse
import json
import math
import re
import sqlite3
import sys
import unicodedata
import xml.etree.ElementTree as ET
from dataclasses import dataclass
from datetime import datetime
from decimal import ROUND_HALF_UP, Decimal
from html import escape
from pathlib import Path
from string import Template
from zoneinfo import ZoneInfo

# build_web_map.py sits next to this script; isolated mode (python3 -I) leaves its folder off the path.
sys.path.insert(0, str(Path(__file__).resolve().parent))

from build_web_map import DB, META, REPO, format_area, format_place, story_teaser  # noqa: E402

ANDROID = REPO / "android"
DESIGNSYSTEM = ANDROID / "core" / "designsystem" / "src" / "main"
TREE_LEAF = DESIGNSYSTEM / "kotlin" / "fr" / "arbresdeparis" / "core" / "designsystem" / "leaf" / "TreeLeaf.kt"
LEAF_PATHS = TREE_LEAF.with_name("TreeLeafPaths.kt")
LEAF_STRINGS = DESIGNSYSTEM / "res" / "values" / "strings.xml"
FLOWERING = (
    ANDROID / "core" / "data" / "src" / "main" / "kotlin" / "fr" / "arbresdeparis" / "core" / "data" / "repository"
    / "GuideFloweringRepository.kt"
)
TEMPLATE = REPO / "data" / "templates" / "herbier.html"
OUT = REPO / "website" / "herbier" / "index.html"

NNBSP = " "  # French thousands separator and space before « % », as the app's NumberFormat
TOP_AREAS = 3  # GetTreeStatistics.TOP_AREAS
STAGE_ORDER = ["Jeune", "Jeune adulte", "Adulte", "Mature"]  # HerbierUiMapper.STAGE_ORDER
MONTHS = [
    "janvier", "février", "mars", "avril", "mai", "juin",
    "juillet", "août", "septembre", "octobre", "novembre", "décembre",
]
INITIALS = "JFMAMJJASOND"
PARIS_TIME = ZoneInfo("Europe/Paris")
ARRONDISSEMENT = re.compile(r"^PARIS (\d{1,2})(?:ER|E) ARRDT$")


# --- the app's sources ------------------------------------------------------------------------------------------------


@dataclass(frozen=True)
class Leaf:
    genus: str
    label: str
    plural: str
    path: str  # on a 100 × 100 viewport


@dataclass(frozen=True)
class Season:
    genus: str
    months: frozenset[int]
    showy: bool


def _android_strings(file: Path) -> dict[str, str]:
    return {
        node.get("name"): (node.text or "").replace("\\'", "'").replace('\\"', '"')
        for node in ET.parse(file).getroot().iter("string")
    }


def _kotlin_constants(file: Path) -> dict[str, str]:
    """`const val NAME = "a" + "b"` → {"NAME": "ab"}."""
    text = file.read_text(encoding="utf-8")
    return {
        match.group(1): "".join(re.findall(r'"([^"]*)"', match.group(2)))
        for match in re.finditer(r'const val (\w+) =\s*((?:"[^"]*"\s*\+?\s*)+)', text)
    }


def leaves(tree_leaf: Path = TREE_LEAF, paths: Path = LEAF_PATHS, strings: Path = LEAF_STRINGS) -> list[Leaf]:
    """The drawn genera, in the order of the app's TreeLeaf enum (GENERIC left out)."""
    labels = _android_strings(strings)
    drawings = _kotlin_constants(paths)
    entry = re.compile(r'^\s*[A-Z_]+\("(\w+)", R\.string\.(\w+), R\.string\.(\w+), (\w+)\),', re.M)
    return [
        Leaf(genus, labels[label], labels[plural], drawings[path])
        for genus, label, plural, path in entry.findall(tree_leaf.read_text(encoding="utf-8"))
    ]


def flowering_seasons(file: Path = FLOWERING) -> list[Season]:
    """One season per genus from the guide's sheets, in the order of the year (FloweringSeason.toSeasons)."""
    sheet = re.compile(
        r'sheet\(\s*"(\w+)",\s*"[^"]*",\s*"[^"]*",\s*(\d+)\.\.(\d+),\s*"[0-9a-f]+",?\s*(showy\s*=\s*true)?'
    )
    merged: dict[str, tuple[set[int], bool]] = {}
    for genus, first, last, showy in sheet.findall(file.read_text(encoding="utf-8")):
        months, was_showy = merged.setdefault(genus, (set(), False))
        months.update(range(int(first), int(last) + 1))
        merged[genus] = (months, was_showy or bool(showy))
    seasons = [Season(g, frozenset(m), s) for g, (m, s) in merged.items()]
    return sorted(seasons, key=lambda s: (min(s.months), max(s.months), s.genus))


_LEAVES: dict[str, Leaf] | None = None


def leaf_for(genus: str | None) -> Leaf | None:
    """The drawing of a dataset genus, or None when the genus is not drawn (TreeLeaf.forGenus → GENERIC)."""
    global _LEAVES
    if _LEAVES is None:
        _LEAVES = {leaf.genus.lower(): leaf for leaf in leaves()}
    return _LEAVES.get((genus or "").strip().lower())


# --- French formatting ------------------------------------------------------------------------------------------------


def _half_up(value: float, places: int = 0) -> Decimal:
    return Decimal(repr(value)).quantize(Decimal(1).scaleb(-places), rounding=ROUND_HALF_UP)


def format_count(count: int) -> str:
    return f"{count:,}".replace(",", NNBSP)


def format_share(fraction: float) -> str:
    """0.19312 → « 19,3 % »."""
    return f"{_half_up(fraction * 100, 1)}".replace(".", ",") + f"{NNBSP}%"


def format_month_range(months) -> str:
    """« mai », or « février – avril » for a run of months (formatMonthRange)."""
    ends = sorted({min(months), max(months)})
    return " – ".join(MONTHS[m - 1] for m in ends)


def french_list(items: list[str]) -> str:
    """« noisetier », « noisetier et orme », « noisetier, orme et if »."""
    if len(items) < 2:
        return "".join(items)
    return ", ".join(items[:-1]) + " et " + items[-1]


def short_place(address: str) -> str:
    """« PARC DES BUTTES CHAUMONT / 7 RUE BOTZARIS, 19-08 » → « Parc des Buttes Chaumont » (HerbierUiMapper)."""
    return format_place(address.split(" / ")[0].split(",")[0])


def _sort_key(text: str) -> str:
    """French alphabetical order, near enough to a collator: accents and case ignored."""
    return "".join(c for c in unicodedata.normalize("NFD", text) if not unicodedata.combining(c)).casefold()


def _arrondissement_number(raw: str) -> int | None:
    match = ARRONDISSEMENT.fullmatch(raw.strip())
    return int(match.group(1)) if match else None


# --- figures (TreeStatisticsDao + HerbierUiMapper, Paris only) --------------------------------------------------------

PARIS = "source = 'paris'"
WITH_GENUS = f"{PARIS} AND genus IS NOT NULL AND genus != ''"


def herbier(con: sqlite3.Connection) -> dict:
    """Everything the page shows, already formatted for French."""
    one = lambda sql: con.execute(sql).fetchone()[0]  # noqa: E731
    total = one(f"SELECT COUNT(*) FROM trees WHERE {PARIS}")
    genera = con.execute(
        f"SELECT genus, COUNT(*) AS count FROM trees WHERE {WITH_GENUS} GROUP BY genus ORDER BY count DESC, genus"
    ).fetchall()
    drawn = [(leaf_for(g), g, n) for g, n in genera if leaf_for(g)]
    first = max(drawn[0][2], 1) if drawn else 1

    headline = None
    if drawn and total:
        leaf, genus, count = drawn[0]
        runner_up = drawn[1] if len(drawn) > 1 else None
        headline = {
            "genus": genus,
            "oneIn": int(_half_up(total / count)),
            "name": leaf.label.lower(),
            "count": format_count(count),
            "plural": leaf.plural,
            "runnerUp": runner_up and {"count": format_count(runner_up[2]), "plural": runner_up[0].plural},
        }

    stages = con.execute(f"SELECT stage, COUNT(*) FROM trees WHERE {PARIS} GROUP BY stage").fetchall()
    known = sorted(
        ((s, n) for s, n in stages if s and s.strip()),
        key=lambda sn: STAGE_ORDER.index(sn[0]) if sn[0] in STAGE_ORDER else len(STAGE_ORDER),
    )
    most = max((n for _, n in known), default=1) or 1
    unknown = sum(n for s, n in stages if not (s and s.strip()))

    leading = con.execute(
        f"SELECT genus, COUNT(*) FROM trees WHERE {WITH_GENUS} AND remarkable = 1 "
        "GROUP BY genus ORDER BY COUNT(*) DESC, genus LIMIT 1"
    ).fetchone()
    tallest = con.execute(
        f"SELECT * FROM trees WHERE {PARIS} AND remarkable = 1 AND height_m IS NOT NULL "
        "ORDER BY height_m DESC, id LIMIT 1"
    ).fetchone()
    widest = con.execute(
        f"SELECT * FROM trees WHERE {PARIS} AND remarkable = 1 AND circumference_cm IS NOT NULL "
        "ORDER BY circumference_cm DESC, id LIMIT 1"
    ).fetchone()
    areas = con.execute(
        f"SELECT arrondissement, COUNT(*) FROM trees WHERE {PARIS} AND arrondissement LIKE 'PARIS %ARRDT' "
        "GROUP BY arrondissement ORDER BY COUNT(*) DESC, arrondissement LIMIT ?",
        (TOP_AREAS,),
    ).fetchall()

    planting_order = [g for g, _ in genera]
    return {
        "total": format_count(total),
        "genusCount": format_count(len(genera)),
        "taxonCount": format_count(
            one(f"SELECT COUNT(*) FROM (SELECT DISTINCT genus, IFNULL(species, '') FROM trees WHERE {WITH_GENUS})")
        ),
        "headline": headline,
        "genera": [
            {
                "genus": genus,
                "label": leaf.label,
                "share": format_share(count / total),
                "count": format_count(count),
                "fraction": round(count / first, 4),
            }
            for leaf, genus, count in drawn
        ],
        "drawnShare": f"{_half_up(sum(n for *_, n in drawn) * 100 / max(total, 1))}{NNBSP}%",
        "stages": [{"label": s, "count": format_count(n), "fraction": round(n / most, 4)} for s, n in known],
        "unknownStageCount": format_count(unknown) if unknown else None,
        "remarkable": {
            "count": format_count(one(f"SELECT COUNT(*) FROM trees WHERE {PARIS} AND remarkable = 1")),
            "leading": leading
            and leaf_for(leading[0])
            and {"count": leading[1], "plural": leaf_for(leading[0]).plural},
            "tallest": tallest and _record(tallest, f"{tallest['height_m']} m"),
            "widest": widest and _record(widest, f"{_half_up(widest['circumference_cm'] / 100, 2)} m".replace(".", ",")),
        },
        "areas": [
            {"rank": i + 1, "name": format_area(area), "count": format_count(n)} for i, (area, n) in enumerate(areas)
        ],
        "remarkableAreas": remarkable_areas(con),
        "flowering": flowering_rows(planting_order),
    }


def _record(tree: sqlite3.Row, value: str) -> dict:
    return {
        "name": tree["common_name"],
        "value": value,
        "place": f"{format_area(tree['arrondissement'])} · {short_place(tree['address'])}",
        "id": tree["id"],
    }


def _stories(con: sqlite3.Connection) -> dict[int, str]:
    if not con.execute("SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'remarkable_trees'").fetchone():
        return {}
    return {row[0]: row[1] for row in con.execute("SELECT tree_id, story FROM remarkable_trees")}


def remarkable_areas(con: sqlite3.Connection) -> list[dict]:
    """Paris 1er to 20e first, then the woods and other places by name; by name within an area (RemarkableTreesMapper)."""
    stories = _stories(con)
    by_area: dict[str, list] = {}
    for tree in con.execute(f"SELECT * FROM trees WHERE {PARIS} AND remarkable = 1"):
        by_area.setdefault(tree["arrondissement"], []).append(tree)
    ordered = sorted(
        by_area.items(),
        key=lambda item: (_arrondissement_number(item[0]) or math.inf, _sort_key(format_area(item[0]))),
    )
    return [
        {
            "name": format_area(area),
            "trees": [
                {
                    "id": tree["id"],
                    "name": tree["common_name"],
                    "detail": " · ".join(
                        [short_place(tree["address"])] + ([f"{tree['height_m']} m"] if tree["height_m"] else [])
                    ),
                    "teaser": story_teaser(stories.get(tree["id"])),
                }
                for tree in sorted(trees, key=lambda t: _sort_key(t["common_name"]))
            ],
        }
        for area, trees in ordered
    ]


def flowering_rows(planting_order: list[str]) -> list[dict]:
    """The calendar of the drawn genera, in the order of the year; `rank` orders the « en fleur » list."""
    rank = {genus: i for i, genus in enumerate(planting_order)}
    return [
        {
            "genus": season.genus,
            "label": leaf_for(season.genus).label,
            "months": sorted(season.months),
            "range": format_month_range(season.months),
            "showy": season.showy,
            "rank": rank.get(season.genus, len(rank)),
        }
        for season in flowering_seasons()
        if leaf_for(season.genus)
    ]


# --- the page ---------------------------------------------------------------------------------------------------------

CHEVRON = (
    '<svg class="chevron" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" '
    'stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m9 6 6 6-6 6"/></svg>'
)


LEAF_STROKE = (
    'fill="none" stroke="currentColor" stroke-width="1.3" vector-effect="non-scaling-stroke" '
    'stroke-linecap="round" stroke-linejoin="round"'
)


def _leaf(genus: str, css: str = "leaf") -> str:
    return f'<svg class="{css}" viewBox="0 0 100 100" aria-hidden="true"><use href="#leaf-{escape(genus)}"/></svg>'


def _sprite() -> str:
    symbols = "".join(
        f'<symbol id="leaf-{escape(leaf.genus)}" viewBox="0 0 100 100"><path d="{escape(leaf.path)}" {LEAF_STROKE}/></symbol>'
        for leaf in leaves()
    )
    return f'<svg class="sprite" aria-hidden="true">{symbols}</svg>'


def _headline(h: dict) -> str:
    headline = h["headline"]
    if not headline:
        return ""
    runner = headline["runnerUp"]
    detail = (
        f"{headline['count']} {headline['plural']}, devant {runner['count']} {runner['plural']}."
        if runner
        else f"{headline['count']} {headline['plural']}."
    )
    return (
        '<article class="card headline">'
        f"{_leaf(headline['genus'], 'leaf watermark')}"
        '<p class="eyebrow">Le saviez-vous ?</p>'
        f"<p class=\"headline-text\">1 arbre sur {headline['oneIn']} à Paris est un {escape(headline['name'])}.</p>"
        f"<p class=\"headline-detail\">{escape(detail)}</p>"
        "</article>"
    )


def _genera(h: dict) -> str:
    rows = "".join(
        "<li>"
        f'<a class="genus" href="../maps/?genre={escape(g["genus"])}" data-goatcounter-click="herbier-genre-{escape(g["genus"])}">'
        f'<span class="leaf-disc">{_leaf(g["genus"])}</span>'
        '<span class="genus-body">'
        f'<span class="genus-top"><span class="genus-name">{escape(g["label"])}</span>'
        f'<span class="genus-share">{g["share"]}</span></span>'
        f'<span class="bar" style="--fraction: {g["fraction"]}"></span>'
        f'<span class="genus-count">{g["count"]} arbres <span class="visually-hidden">— voir sur la carte</span></span>'
        "</span>"
        f"{CHEVRON}"
        "</a></li>"
        for g in h["genera"]
    )
    return f'<ol class="genera">{rows}</ol>'


def _calendar(h: dict) -> str:
    head = "".join(
        f'<th scope="col" data-month="{i + 1}"><abbr title="{MONTHS[i]}">{INITIALS[i]}</abbr></th>' for i in range(12)
    )
    rows = []
    for row in h["flowering"]:
        cells = "".join(
            f'<td data-month="{m}"{" class=\"bloom\"" if m in row["months"] else ""}></td>' for m in range(1, 13)
        )
        kind = "Fleurs voyantes" if row["showy"] else "Fleurs discrètes"
        rows.append(
            f'<tr class="{"showy" if row["showy"] else "discreet"}" data-genus="{escape(row["genus"])}" '
            f'data-label="{escape(row["label"])}" data-months="{" ".join(map(str, row["months"]))}" '
            f'data-rank="{row["rank"]}">'
            f'<th scope="row"><span class="row-name">{_leaf(row["genus"])}<span>{escape(row["label"])}</span>'
            f'<span class="visually-hidden"> : {escape(row["range"])}, {kind.lower()}</span></span></th>'
            f"{cells}</tr>"
        )
    return (
        '<div class="calendar-scroll"><table class="calendar">'
        "<caption class=\"visually-hidden\">Calendrier des floraisons</caption>"
        f'<thead><tr><td></td>{head}</tr></thead><tbody>{"".join(rows)}</tbody></table></div>'
    )


def _record_card(title: str, record: dict | None) -> str:
    if not record:
        return ""
    return (
        f'<a class="card record" href="../maps/?arbre={record["id"]}">'
        f'<span class="eyebrow">{title}</span>'
        f'<span class="record-value">{escape(record["value"])}</span>'
        f'<span class="record-name">{escape(record["name"])}</span>'
        f'<span class="record-place">{escape(record["place"])}</span>'
        "</a>"
    )


def _remarkable(h: dict) -> str:
    r = h["remarkable"]
    count = f"{r['count']} arbres remarquables"
    if r["leading"]:
        count += f", dont {r['leading']['count']} {r['leading']['plural']}"
    areas = "".join(
        f'<section class="area" aria-labelledby="zone-{i}"><h3 id="zone-{i}">{escape(area["name"])}</h3><ul>'
        + "".join(
            f'<li><a href="../maps/?arbre={tree["id"]}">'
            f'<span class="tree-name">{escape(tree["name"])}</span>'
            f'<span class="tree-detail">{escape(tree["detail"])}</span>'
            + (f'<span class="tree-teaser">{escape(tree["teaser"])}</span>' if tree["teaser"] else "")
            + "</a></li>"
            for tree in area["trees"]
        )
        + "</ul></section>"
        for i, area in enumerate(h["remarkableAreas"], start=1)
    )
    return (
        f'<p class="section-lead">{escape(count)}.</p>'
        f'<div class="records">{_record_card("Le plus haut", r["tallest"])}{_record_card("Le plus large", r["widest"])}</div>'
        '<p class="hint">Tous les arbres remarquables, par arrondissement. Choisissez-en un pour le voir sur la carte.</p>'
        f'<div class="areas">{areas}</div>'
    )


def _ages(h: dict) -> str:
    stages = "".join(
        f'<li><span class="stage-top"><span>{escape(s["label"])}</span><span>{s["count"]}</span></span>'
        f'<span class="bar" style="--fraction: {s["fraction"]}"></span></li>'
        for s in h["stages"]
    )
    unknown = (
        f'<p class="hint">{h["unknownStageCount"]} arbres sans stade renseigné.</p>' if h["unknownStageCount"] else ""
    )
    areas = "".join(
        f'<li><span class="rank">{a["rank"]}</span><span class="area-name">{escape(a["name"])}</span>'
        f'<span class="area-count">{a["count"]} arbres</span></li>'
        for a in h["areas"]
    )
    return (
        '<div class="ages">'
        f'<div class="card"><h3>Âge des arbres</h3><ul class="stages">{stages}</ul>{unknown}</div>'
        f'<div class="card"><h3>Les plus boisés</h3><ol class="wooded">{areas}</ol></div>'
        "</div>"
    )


def _extraction_date(meta: dict) -> str | None:
    at = meta.get("extractedAt")
    if not at:
        return None
    day = datetime.fromisoformat(at.replace("Z", "+00:00")).astimezone(PARIS_TIME)
    return f"{'1er' if day.day == 1 else day.day} {MONTHS[day.month - 1]} {day.year}"


def render(h: dict, meta: dict, template: Path = TEMPLATE) -> str:
    date = _extraction_date(meta)
    values = {
        "total": h["total"],
        "genusCount": h["genusCount"],
        "taxonCount": h["taxonCount"],
        "remarkableCount": h["remarkable"]["count"],
        "drawnCount": str(len(h["genera"])),
        "drawnShare": h["drawnShare"],
        "floweringCount": str(len(h["flowering"])),
        "dataDate": f" Données du {date}." if date else "",
        "sprite": _sprite(),
        "headline": _headline(h),
        "genera": _genera(h),
        "calendar": _calendar(h),
        "remarkable": _remarkable(h),
        "ages": _ages(h),
    }
    return Template(template.read_text(encoding="utf-8")).substitute(values)


def build(db: Path, meta_file: Path, out: Path, template: Path = TEMPLATE) -> dict:
    """Writes the page to `out`; returns its figures."""
    con = sqlite3.connect(f"file:{db}?mode=ro", uri=True)
    con.row_factory = sqlite3.Row
    try:
        figures = herbier(con)
    finally:
        con.close()
    meta = json.loads(meta_file.read_text(encoding="utf-8")) if meta_file.exists() else {}
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(render(figures, meta, template), encoding="utf-8")
    return figures


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--db", type=Path, default=DB)
    parser.add_argument("--meta", type=Path, default=META)
    parser.add_argument("--output", type=Path, default=OUT)
    args = parser.parse_args(argv)
    figures = build(args.db, args.meta, args.output)
    print(
        f"Herbier: {figures['total']} trees, {len(figures['genera'])} drawn genera, "
        f"{figures['remarkable']['count']} remarkable; wrote "
        f"{args.output.relative_to(REPO) if args.output.is_relative_to(REPO) else args.output} "
        f"({args.output.stat().st_size / 1e3:.0f} kB)"
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
