"""« Les arbres remarquables », Ville de Paris (ODbL): the story of each remarkable tree of Paris (issue #74).

One record per remarkable tree, keyed by the same id as « Les arbres » (`arbres_idbase` = our tree id): why it is
remarkable, a summary, its story, its planting year, its national label and council decision when it has one, and
links to its sign (PDF) and photo, both hosted by the City. The 166 remarkable trees of the GPSO towns have none.
Standard library only.
"""

from __future__ import annotations

import re

SOURCE = "remarquables"
DATASET_PAGE = "https://opendata.paris.fr/explore/dataset/arbresremarquablesparis/information/"
EXPORT_URL = "https://opendata.paris.fr/api/explore/v2.1/catalog/datasets/arbresremarquablesparis/exports/json"
METADATA_URL = "https://opendata.paris.fr/api/explore/v2.1/catalog/datasets/arbresremarquablesparis"
LICENSE = "ODbL"

# Room's `remarkable_trees` columns (RemarkableTreeEntity), in insertion order.
COLUMNS = (
    "tree_id",
    "site",
    "qualification",
    "summary",
    "story",
    "planting_year",
    "label",
    "council_decision",
    "sign_url",
    "photo_url",
    "photo_credit",
)

_YEAR = re.compile(r"^(\d{4})(?:-\d{2}-\d{2})?$")


def _text(value) -> str | None:
    if value is None:
        return None
    text = " ".join(str(value).split())
    return text or None


def planting_year(value) -> int | None:
    """« 1882 » or « 2017-11-09 » → the year; « Inconnue » or anything else → None."""
    match = _YEAR.match((_text(value) or ""))
    return int(match.group(1)) if match else None


def clean(records: list[dict], tree_ids: set[int]) -> list[dict]:
    """One row per remarkable tree the app has ([tree_ids]), the first record winning when a tree is listed twice."""
    rows: dict[int, dict] = {}
    for record in records:
        raw_id = record.get("arbres_idbase")
        if raw_id is None:
            continue
        tree_id = int(raw_id)
        if tree_id not in tree_ids or tree_id in rows:
            continue
        rows[tree_id] = {
            "tree_id": tree_id,
            "site": _text(record.get("com_site")),
            "qualification": _text(record.get("com_qualification_rem")),
            "summary": _text(record.get("com_resume")),
            "story": _text(record.get("com_descriptif")),
            "planting_year": planting_year(record.get("com_annee_plantation")),
            "label": _text(record.get("com_label_arbres")),
            "council_decision": _text(record.get("com_delib_num")),
            "sign_url": _text(record.get("com_url_pdf")),
            "photo_url": _text(record.get("com_url_photo1")),
            "photo_credit": _text(record.get("com_copyright1")),
        }
    return sorted(rows.values(), key=lambda row: row["tree_id"])
