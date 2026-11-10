"""Grand Paris Seine Ouest (GPSO): the trees of its 8 towns, from data.seineouest.fr (Licence Ouverte 2.0).

Boulogne-Billancourt, Chaville, Issy-les-Moulineaux, Marnes-la-Coquette, Meudon, Sèvres, Vanves, Ville-d'Avray.
The dataset has species and French names, but heights only as ranges and no circumference or development stage.
See docs/roadmap/grand-paris-trees.md. Standard library only.
"""

from __future__ import annotations

import csv
import re
from dataclasses import dataclass
from pathlib import Path

SOURCE = "gpso"
DATASET_PAGE = "https://data.seineouest.fr/explore/dataset/arbres-v2/information/"
EXPORT_URL = "https://data.seineouest.fr/api/explore/v2.1/catalog/datasets/arbres-v2/exports/csv?delimiter=%3B"
METADATA_URL = "https://data.seineouest.fr/api/explore/v2.1/catalog/datasets/arbres-v2"
LICENSE = "Licence Ouverte 2.0"

EXPECTED_FIELDS = (
    "id_arbre",
    "code_insee",
    "site",
    "genespvar",
    "vernaculaire",
    "particular",
    "class_hauteur",
    "geo_point_2d",
    "objectid",
)

# GPSO ids are « <INSEE code>_<number> ». Ours: ID_OFFSET + INSEE × 1,000,000 + number, far above Paris ids (< 3
# million), so ids from the two sources never meet. A few GPSO ids are shared by two trees: the second one (by
# objectid) gets DUPLICATE_STEP more.
ID_OFFSET = 100_000_000_000
DUPLICATE_STEP = 500_000

HEIGHT_CLASSES = {
    "Moins de 5 m": "moins de 5",
    "De 5 à 10 m": "5 à 10",
    "De 11 à 20 m": "11 à 20",
    "Plus de 20 m": "plus de 20",
}

REMARKABLE = {"Remarquable", "Majeur"}
NOT_A_TREE = {"souche"}  # a stump
UNKNOWN_GENUS = {"indéterminé", "indetermine", "inconnu"}
NOT_A_NAME = {"indéterminé", "remplacement"}  # « remplacement »: a tree to replace, species not yet known
AUTHORS = {"hayne", "l.", "mill.", "ehrh."}  # botanists' names that follow some species, not varieties

_QUOTED = re.compile(r"""['"‘’“”]+\s*(.+?)\s*['"‘’“”]+""")
_ARTICLE = re.compile(r"^(?:le|la|les)\s+", re.IGNORECASE)
_SEGMENT_NUMBER = re.compile(r"\s+\d+$")  # « Rue Diderot 1 »: the street's section, not a house number


@dataclass
class Report:
    total: int = 0
    kept: int = 0
    invalid_coordinates: int = 0
    outside_coverage: int = 0
    not_trees: int = 0
    duplicate_ids: int = 0

    def __str__(self) -> str:
        return (
            f"GPSO: {self.total} records read, {self.kept} kept; dropped: {self.invalid_coordinates} invalid "
            f"coordinates, {self.outside_coverage} outside the coverage area, {self.not_trees} stumps; "
            f"{self.duplicate_ids} shared ids renumbered"
        )


def read_rows(path: Path) -> list[dict]:
    with path.open(encoding="utf-8-sig", newline="") as f:
        reader = csv.DictReader(f, delimiter=";")
        missing = [name for name in EXPECTED_FIELDS if name not in (reader.fieldnames or [])]
        if missing:
            raise ValueError(f"GPSO dataset is missing expected fields: {', '.join(missing)}")
        return list(reader)


def parse_taxon(text: str) -> tuple[str | None, str | None, str | None]:
    """« Acer platanoides 'Crimson King' » → (« Acer », « platanoides », « Crimson King »), written like the Paris
    data: a capitalised genus, a lower-case epithet (« x hispanica », « sp. »), the cultivar without quotes."""
    text = " ".join(text.split(",")[0].split())  # « Pinus montana, Pinus mugo »: the first name
    cultivar = None
    quoted = _QUOTED.search(text)
    if quoted:
        cultivar = " ".join(quoted.group(1).split())
        cultivar = cultivar[:1].upper() + cultivar[1:]
        text = text[: quoted.start()].strip()
    words = [w for w in text.split(" ") if w.lower() not in AUTHORS and w.lower() not in ("ssp.", "subsp.")]
    if not words or not words[0] or words[0].lower() in UNKNOWN_GENUS:
        return None, None, cultivar
    genus = words[0].capitalize()
    rest = words[1:]
    species = None
    if rest and rest[0].lower() in ("x", "×") and len(rest) > 1:
        species, rest = f"x {rest[1].lower()}", rest[2:]
    elif rest and (rest[0].islower() or rest[0].lower() in ("sp", "sp.")):
        species = "sp." if rest[0].lower() in ("sp", "sp.") else rest[0].lower()
        rest = rest[1:]
    elif len(rest) == 1 and rest[0][1:].islower():  # « Acer Freemanii », « Pyrus Communis »: a capitalised epithet
        species, rest = rest[0].lower(), []
    if rest and cultivar is None:  # « Acer x freemanii Autumn Blaze », « Malus red sentinel »: unquoted cultivar
        cultivar = " ".join(w[:1].upper() + w[1:] for w in rest)
    return genus, species, cultivar


def tree_id(gpso_id: str, duplicate: int = 0) -> int | None:
    """« 92040_10302 » → 192,040,010,302 (see ID_OFFSET); None when the id has another shape."""
    match = re.fullmatch(r"(\d{5})_(\d+)", gpso_id.strip())
    if not match or int(match.group(2)) >= DUPLICATE_STEP:
        return None
    return ID_OFFSET + int(match.group(1)) * 1_000_000 + int(match.group(2)) + duplicate * DUPLICATE_STEP


def _coordinates(value: str) -> tuple[float, float] | None:
    try:
        lat, lon = (float(part) for part in value.split(","))
    except ValueError:
        return None
    return None if lat == 0 and lon == 0 else (lat, lon)


def _french_name(text: str) -> str:
    """« Le Bouleau blanc de l'Himalaya » → « Bouleau blanc de l'Himalaya »; « Indéterminé » → ""."""
    name = " ".join(text.split())
    if name.lower() in NOT_A_NAME:
        return ""
    return _ARTICLE.sub("", name)


def _address(site: str) -> str:
    return _SEGMENT_NUMBER.sub("", " ".join(site.split()))


def clean(rows: list[dict], communes, common_names: dict, in_coverage) -> tuple[list[dict], Report]:
    """Rows keyed by Room column name, sorted by id.

    [communes] names the town from the INSEE code; [common_names] maps (genus, species) and (genus, "") to a French
    name, for the few trees GPSO leaves unnamed; [in_coverage] tells whether a (lat, lon) is on the app's map.
    """
    report = Report(total=len(rows))
    cleaned = []
    seen: dict[str, int] = {}
    for row in sorted(rows, key=lambda r: int(r.get("objectid") or 0)):
        coordinates = _coordinates(row.get("geo_point_2d") or "")
        duplicate = seen.get(row["id_arbre"], 0)
        identifier = tree_id(row["id_arbre"], duplicate)
        if coordinates is None or identifier is None:
            report.invalid_coordinates += 1
            continue
        lat, lon = coordinates
        if not in_coverage(lat, lon):
            report.outside_coverage += 1
            continue
        taxon = (row.get("genespvar") or "").strip()
        if taxon.lower() in NOT_A_TREE:
            report.not_trees += 1
            continue
        if duplicate:
            report.duplicate_ids += 1
        seen[row["id_arbre"]] = duplicate + 1
        genus, species, cultivar = parse_taxon(taxon)
        city = communes.name_of(row["code_insee"].strip()) or (row.get("commune") or "").title()
        name = (
            _french_name(row.get("vernaculaire") or "")
            or common_names.get((genus, species or ""))
            or common_names.get((genus, ""))
            or genus
            or "Arbre"
        )
        cleaned.append(
            {
                "id": identifier,
                "common_name": name[:1].upper() + name[1:],
                "genus": genus,
                "species": species,
                "cultivar": cultivar,
                "circumference_cm": None,
                "height_m": None,
                "stage": None,
                "remarkable": 1 if (row.get("particular") or "").strip() in REMARKABLE else 0,
                "address": _address(row.get("site") or ""),
                "arrondissement": city,
                "lat": lat,
                "lon": lon,
                "city": city,
                "source": SOURCE,
                "height_class": HEIGHT_CLASSES.get((row.get("class_hauteur") or "").strip()),
            }
        )
    cleaned.sort(key=lambda r: r["id"])
    report.kept = len(cleaned)
    return cleaned, report
