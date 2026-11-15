"""The Hauts-de-Seine remarkable trees (issue #91): cleaned into trees and their details, merged with GPSO's trees."""

import json
import sys
from pathlib import Path

import pytest

REPO = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(REPO / "data" / "scripts"))

import hauts_de_seine as hds  # noqa: E402

FIXTURE = REPO / "data/tests/fixtures/hauts-de-seine-remarquables.json"


class Towns:
    NAMES = {
        "92040": "Issy-les-Moulineaux",
        "92071": "Sceaux",
        "92051": "Neuilly-sur-Seine",
        "92019": "Châtenay-Malabry",
        "92060": "Le Plessis-Robinson",
        "92012": "Boulogne-Billancourt",
        "92036": "Gennevilliers",
    }

    def name_of(self, code):
        return self.NAMES.get(code)


def everywhere(lat, lon):
    return True


def cleaned(in_coverage=everywhere):
    return hds.clean(json.loads(FIXTURE.read_text()), Towns(), in_coverage)


def by_matricule(rows):
    return {row["id"] - hds.ID_OFFSET: row for row in rows}


# --- names --------------------------------------------------------------------------------------------------------


@pytest.mark.parametrize(
    "latin, expected",
    [
        ("Populus nigra L.", ("Populus", "nigra", None)),
        ("Cedrus atlantica (Endl.) Manetti ex Carrière", ("Cedrus", "atlantica", None)),
        ("Fagus sylvatica L. 'Purpurea'", ("Fagus", "sylvatica", "Purpurea")),
        ("Malus ×purpurea", ("Malus", "x purpurea", None)),
        ("Platanus × hispanica Mill. ex Münchh.", ("Platanus", "x hispanica", None)),
        ("Populus x canadensis Moench.", ("Populus", "x canadensis", None)),
        ("Prunus serrulata 'Kanzan'", ("Prunus", "serrulata", "Kanzan")),
        ("Cedrus atlantica (Endl.) Manetti ex Carriere 'Glauca'", ("Cedrus", "atlantica", "Glauca")),
    ],
)
def test_parse_taxon_drops_the_botanists(latin, expected):
    assert hds.parse_taxon(latin) == expected


# --- trees --------------------------------------------------------------------------------------------------------


def test_keeps_single_trees_only():
    trees, details, report = cleaned()
    rows = by_matricule(trees)
    assert 355 not in rows  # eleven horse chestnuts as one record
    assert report.groups == 1
    assert len(trees) == len(details) == 8


def test_a_tree_row_like_the_other_sources():
    peuplier = by_matricule(cleaned()[0])[1]
    assert peuplier == {
        "id": hds.ID_OFFSET + 1,
        "common_name": "Peuplier noir",
        "genus": "Populus",
        "species": "nigra",
        "cultivar": None,
        "circumference_cm": 650,
        "height_m": 28,
        "stage": None,
        "remarkable": 1,
        "address": "",
        "arrondissement": "Issy-les-Moulineaux",
        "lat": pytest.approx(48.82512, abs=1e-5),
        "lon": pytest.approx(2.25198, abs=1e-5),
        "city": "Issy-les-Moulineaux",
        "source": "hds",
        "height_class": None,
    }


def test_zero_and_impossible_measures_are_unknown():
    rows = by_matricule(cleaned()[0])
    assert rows[3281]["circumference_cm"] is None  # « 345 » m
    assert rows[3281]["height_m"] == 30  # 29.5 m, rounded like the Paris data
    assert rows[3678]["height_m"] is None and rows[3678]["circumference_cm"] is None  # 0 and 0


def test_outside_the_map_is_left_out():
    trees, _, report = cleaned(in_coverage=lambda lat, lon: lat < 48.92)
    assert 1207 not in by_matricule(trees)  # Gennevilliers
    assert report.outside_coverage == 1


# --- what the sheet says ------------------------------------------------------------------------------------------


def details_by_matricule():
    return {row["tree_id"] - hds.ID_OFFSET: row for row in cleaned()[1]}


def test_details_say_why_and_credit_the_photo():
    peuplier = details_by_matricule()[1]
    assert peuplier["qualification"] == "Dimensions"
    assert peuplier["summary"] == "Remarquable pour ses dimensions. Écorce remarquable."
    assert peuplier["story"] is None
    assert peuplier["planting_year"] == 1850
    assert peuplier["photo_url"].startswith("https://opendata.hauts-de-seine.fr/api/explore/v2.1/catalog/datasets/")
    assert peuplier["photo_credit"] == "Département des Hauts-de-Seine"
    assert peuplier["site"] is None and peuplier["sign_url"] is None and peuplier["council_decision"] is None


def test_details_label_and_qualifications():
    details = details_by_matricule()
    assert details[28]["label"] == "A.R.B.R.E.S en 2026"
    assert details[28]["qualification"] == "Port"
    assert details[1207]["qualification"] == "Paysager"  # « Pour sa situation »: its weight in the landscape
    assert details[9]["qualification"] is None and details[9]["summary"] is None  # « Général », nothing else
    assert details[9]["photo_url"] is None and details[9]["photo_credit"] is None


# --- merge with GPSO ----------------------------------------------------------------------------------------------


def gpso_tree(tree_id, genus, lat, lon, remarkable=0):
    return {"id": tree_id, "genus": genus, "lat": lat, "lon": lon, "remarkable": remarkable, "source": "gpso"}


def test_a_gpso_tree_of_the_same_genus_nearby_takes_the_details():
    trees, details, _ = cleaned()
    beech = gpso_tree(192012000777, "Fagus", 48.84975, 2.23185)  # ≈ 4 m from matricule 9
    oak = gpso_tree(192012000778, "Quercus", 48.84973, 2.23184)  # closer, but another genus
    far_beech = gpso_tree(192012000779, "Fagus", 48.8510, 2.2330)
    gpso = [beech, oak, far_beech]
    kept, merged_details, merged = hds.merge(trees, details, gpso)
    assert merged == 1
    assert hds.ID_OFFSET + 9 not in {t["id"] for t in kept}
    assert beech["remarkable"] == 1 and oak["remarkable"] == 0 and far_beech["remarkable"] == 0
    assert {d["tree_id"] for d in merged_details} >= {beech["id"]}
    assert hds.ID_OFFSET + 9 not in {d["tree_id"] for d in merged_details}
    assert len(kept) == len(trees) - 1 and len(merged_details) == len(details)
