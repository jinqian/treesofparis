"""The « Les arbres remarquables » dataset: one row per remarkable tree of the app, keyed by its Paris tree id."""

import json
import sys
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(REPO / "data" / "scripts"))

import remarkable  # noqa: E402

FIXTURE = REPO / "data/tests/fixtures/arbres-remarquables.json"
# Ids of trees the app has: the Orme of place Saint-Gervais, the Chêne chevelu, the Noisetier, the Bercy Platane.
APP_IDS = {267663, 2017817, 124914, 2002365}


def rows():
    return {row["tree_id"]: row for row in remarkable.clean(json.loads(FIXTURE.read_text()), APP_IDS)}


def test_keeps_one_row_per_tree_the_app_has():
    found = rows()
    # The Mûrier of the École normale supérieure is not in « Les arbres »; the Bercy Platane is listed twice.
    assert set(found) == APP_IDS


def test_reads_what_the_sheet_and_the_story_page_show():
    orme = rows()[267663]
    assert orme["site"] == "Place Saint-Gervais"
    assert orme["qualification"] == "Historique"
    assert orme["summary"] == "Cet arbre est classé remarquable pour son caractère historique."
    assert orme["story"].startswith("L'orme appartient à la famille des Ulmacées.") or orme["story"].startswith(
        "L’orme appartient"
    )
    assert orme["label"] == "A.R.B.R.E.S en 2015"
    assert orme["council_decision"] == "2015 DEVE 152"
    assert orme["sign_url"] == "https://capgeo.sig.paris.fr/PdfEtImages/ArbresRemarquables/PDF/267663.pdf"
    assert orme["photo_url"] == "https://capgeo.sig.paris.fr/PdfEtImages/ArbresRemarquables/Photos2023/1/267663.jpg"
    assert orme["photo_credit"] == "Jean-Pierre Viguié / Ville de Paris"


def test_planting_year_is_a_year_or_unknown():
    found = rows()
    assert found[267663]["planting_year"] is None  # « Inconnue »
    assert found[124914]["planting_year"] == 1882
    assert found[2017817]["planting_year"] == 2017  # « 2017-11-09 »


def test_optional_fields_are_none_when_empty():
    noisetier = rows()[124914]
    assert noisetier["label"] is None
    assert noisetier["council_decision"] is None


def test_columns_match_the_room_entity():
    assert set(next(iter(rows().values()))) == set(remarkable.COLUMNS)
