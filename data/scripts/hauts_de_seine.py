"""« Arbres remarquables », Département des Hauts-de-Seine (Licence Ouverte): the remarkable trees of its 36 towns
(issue #91, ADR 0010).

One record per tree or group of trees, outside private property: its French and Latin names, height, circumference
(in metres) and crown spread, why it is remarkable (one general criterion, a few particular ones), its planting year
(15 % of the records), its national label, and a photo on the Département's server (2448 × 3264, about 4 MB). No story,
no address: the town only. Positions were placed on aerial photographs, to a few metres.

Kept: single trees (a group is one point for up to 112 trees) inside the app's map. A record that is a GPSO tree
(same genus within MERGE_DISTANCE_M) gives that tree its details instead of adding a second pin. Standard library
only.
"""

from __future__ import annotations

import math
import re
from dataclasses import dataclass

SOURCE = "hds"
DATASET_PAGE = "https://opendata.hauts-de-seine.fr/explore/dataset/fr-229200506-arbres-remarquables/information/"
EXPORT_URL = (
    "https://opendata.hauts-de-seine.fr/api/explore/v2.1/catalog/datasets/fr-229200506-arbres-remarquables/exports/json"
)
METADATA_URL = "https://opendata.hauts-de-seine.fr/api/explore/v2.1/catalog/datasets/fr-229200506-arbres-remarquables"
LICENSE = "Licence Ouverte"
PHOTO_CREDIT = "Département des Hauts-de-Seine"

# Our ids: ID_OFFSET + the Département's « matricule » (< 10,000), above the GPSO ids (< 200 billion).
ID_OFFSET = 300_000_000_000
MERGE_DISTANCE_M = 10.0
MAX_CIRCUMFERENCE_M = 15.0  # the largest real one is 14.2 m; « 345 », « 27 » are typing errors
MAX_HEIGHT_M = 60.0

# « Pour sa situation » is « son impact paysager » in the dataset's description, « Pour sa rareté » its rareté
# botanique: the words the Paris data uses. « Général » says nothing.
QUALIFICATIONS = {
    "Pour ses dimensions": ("Dimensions", "Remarquable pour ses dimensions."),
    "Pour son port": ("Port", "Remarquable pour son port."),
    "Pour sa situation": ("Paysager", "Remarquable pour sa place dans le paysage."),
    "Pour sa rareté": ("Botanique", "Remarquable pour sa rareté."),
    "Pour son âge": ("Âge", "Remarquable pour son âge."),
    "Historique": ("Historique", "Remarquable pour son histoire."),
    "Pittoresque": ("Pittoresque", "Remarquable pour son caractère pittoresque."),
}
# The particular criteria that describe the tree; « Elagage drastique » and « Masque un pignon aveugle » are notes for
# the gardeners, left out.
PARTICULARS = {
    "Arbre de forêt": "arbre de forêt",
    "Arbre en cépée": "arbre en cépée",
    "Houppier naturel intact": "houppier naturel intact",
    "Arbre d'avenir": "arbre d’avenir",
    "Ecorce remarquable": "écorce remarquable",
    "Pittoresque": "pittoresque",
    "Arbre d'intérêt historique": "arbre d’intérêt historique",
}

_QUOTED = re.compile(r"""\s*['‘’"“”](.+?)['‘’"“”]\s*""")
_YEAR = re.compile(r"^\d{4}$")


@dataclass
class Report:
    total: int = 0
    kept: int = 0
    groups: int = 0
    outside_coverage: int = 0
    invalid: int = 0

    def __str__(self) -> str:
        return (
            f"Hauts-de-Seine remarkable trees: {self.total} records read, {self.kept} kept; dropped: {self.groups} "
            f"groups of trees, {self.outside_coverage} outside the coverage area, {self.invalid} without a position"
        )


def parse_taxon(latin: str) -> tuple[str | None, str | None, str | None]:
    """« Cedrus atlantica (Endl.) Manetti ex Carrière 'Glauca' » → (« Cedrus », « atlantica », « Glauca »): genus,
    species as the Paris data writes it (« x hispanica »), cultivar; the botanists' names after the species dropped."""
    text = " ".join(latin.split())
    cultivar = None
    quoted = _QUOTED.search(text)
    if quoted:
        cultivar = quoted.group(1).strip()
        text = (text[: quoted.start()] + " " + text[quoted.end() :]).strip()
    words = text.replace("×", " × ").split()
    if not words:
        return None, None, cultivar
    genus = words[0].capitalize()
    rest = words[1:]
    species = None
    if len(rest) >= 2 and rest[0].lower() in ("x", "×"):
        species = f"x {rest[1].lower()}"
    elif rest and rest[0].islower():
        species = rest[0]
    return genus, species, cultivar


def _measure(value, maximum: float) -> float | None:
    return value if isinstance(value, (int, float)) and 0 < value <= maximum else None


def _round(value: float) -> int:
    return int(math.floor(value + 0.5))


def _details(record: dict, tree_id: int) -> dict:
    qualification, why = QUALIFICATIONS.get((record.get("critere_general") or "").strip(), (None, None))
    particulars = [
        PARTICULARS[item.strip()] for item in (record.get("critere_autre") or "").split(",") if item.strip() in PARTICULARS
    ]
    summary = " ".join(
        part
        for part in (why, (particulars[0][:1].upper() + ", ".join(particulars)[1:] + ".") if particulars else None)
        if part
    )
    year = (record.get("annee_plantation") or "").strip()
    label_year = (record.get("label_obtention_annee") or "")[:4]
    photo = record.get("photo") or {}
    return {
        "tree_id": tree_id,
        "site": None,
        "qualification": qualification,
        "summary": summary or None,
        "story": None,
        "planting_year": int(year) if _YEAR.match(year) else None,
        "label": f"A.R.B.R.E.S en {label_year}" if record.get("label_obtention") == "oui" and label_year else None,
        "council_decision": None,
        "sign_url": None,
        "photo_url": photo.get("url"),
        "photo_credit": PHOTO_CREDIT if photo.get("url") else None,
    }


def clean(records: list[dict], communes, in_coverage) -> tuple[list[dict], list[dict], Report]:
    """Tree rows (Room columns) and their details (`remarkable_trees` rows), both sorted by id. [communes] names the
    town from its INSEE code; [in_coverage] tells whether a (lat, lon) is on the app's map."""
    report = Report(total=len(records))
    trees, details = [], []
    for record in records:
        point = record.get("geo_point_2d") or {}
        lat, lon = point.get("lat"), point.get("lon")
        matricule = str(record.get("matricule") or "")
        if lat is None or lon is None or not matricule.isdigit():
            report.invalid += 1
            continue
        if (record.get("nb_sujet") or 1) > 1:
            report.groups += 1
            continue
        if not in_coverage(lat, lon):
            report.outside_coverage += 1
            continue
        tree_id = ID_OFFSET + int(matricule)
        genus, species, cultivar = parse_taxon(record.get("nom_latin") or "")
        city = communes.name_of((record.get("code_insee") or "").strip()) or (record.get("commune") or "").title()
        name = " ".join((record.get("nom_francais") or genus or "Arbre").split())
        height = _measure(record.get("hauteur"), MAX_HEIGHT_M)
        circumference = _measure(record.get("circonference"), MAX_CIRCUMFERENCE_M)
        trees.append(
            {
                "id": tree_id,
                "common_name": name[:1].upper() + name[1:],
                "genus": genus,
                "species": species,
                "cultivar": cultivar,
                "circumference_cm": _round(circumference * 100) if circumference else None,
                "height_m": _round(height) if height else None,
                "stage": None,
                "remarkable": 1,
                "address": "",
                "arrondissement": city,
                "lat": lat,
                "lon": lon,
                "city": city,
                "source": SOURCE,
                "height_class": None,
            }
        )
        details.append(_details(record, tree_id))
    trees.sort(key=lambda r: r["id"])
    details.sort(key=lambda r: r["tree_id"])
    report.kept = len(trees)
    return trees, details, report


def _distance_m(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    return math.hypot((lat1 - lat2) * 111_320, (lon1 - lon2) * 111_320 * math.cos(math.radians((lat1 + lat2) / 2)))


def merge(trees: list[dict], details: list[dict], gpso_rows: list[dict]) -> tuple[list[dict], list[dict], int]:
    """A Hauts-de-Seine tree that is a GPSO tree (the same genus within MERGE_DISTANCE_M, the nearest one) is not added
    again: the GPSO tree, whose position is surveyed, becomes remarkable and takes its details. Changes [gpso_rows] in
    place; returns the trees still to add, every detail row (re-keyed), and how many were merged."""
    cell = 0.001  # ≈ 110 m: a grid to look only at nearby GPSO trees
    grid: dict[tuple[int, int], list[dict]] = {}
    for row in gpso_rows:
        grid.setdefault((int(row["lat"] / cell), int(row["lon"] / cell)), []).append(row)
    new_id: dict[int, int] = {}
    taken: set[int] = set()
    for tree in trees:
        if not tree["genus"]:
            continue
        key = (int(tree["lat"] / cell), int(tree["lon"] / cell))
        candidates = [
            (_distance_m(tree["lat"], tree["lon"], row["lat"], row["lon"]), row)
            for dlat in (-1, 0, 1)
            for dlon in (-1, 0, 1)
            for row in grid.get((key[0] + dlat, key[1] + dlon), ())
            if (row["genus"] or "").lower() == tree["genus"].lower() and row["id"] not in taken
        ]
        distance, row = min(candidates, default=(math.inf, None), key=lambda c: c[0])
        if row is not None and distance <= MERGE_DISTANCE_M:
            row["remarkable"] = 1
            taken.add(row["id"])
            new_id[tree["id"]] = row["id"]
    kept = [tree for tree in trees if tree["id"] not in new_id]
    merged_details = sorted(
        ({**detail, "tree_id": new_id.get(detail["tree_id"], detail["tree_id"])} for detail in details),
        key=lambda d: d["tree_id"],
    )
    return kept, merged_details, len(new_id)
