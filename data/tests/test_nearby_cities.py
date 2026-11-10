"""Tests for the neighbouring towns' data: data/scripts/gpso.py, communes.py and how update_trees.py merges them.
Run: data/.venv/bin/python -I -m pytest data/tests"""

import json
import sys
from pathlib import Path

import pytest

REPO = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(REPO / "data" / "scripts"))

import communes  # noqa: E402
import gpso  # noqa: E402
import update_trees as ut  # noqa: E402

TOWNS = communes.Communes.load(REPO / "data" / "communes.geojson")


# --- communes ---------------------------------------------------------------------------------------------------------


@pytest.mark.parametrize(
    ("lat", "lon", "town"),
    [
        (48.8566, 2.3522, "Paris"),  # Hôtel de Ville
        (48.9016, 2.4075, "Pantin"),  # the Parisian cemetery of Pantin
        (48.8240, 2.2710, "Issy-les-Moulineaux"),
        (48.8250, 2.1900, "Ville-d'Avray"),
        (45.7640, 4.8357, None),  # Lyon: outside the four départements
    ],
)
def test_locate_finds_the_town_of_a_point(lat, lon, town):
    assert TOWNS.locate(lat, lon) == town


def test_name_of_reads_the_insee_code():
    assert TOWNS.name_of("92040") == "Issy-les-Moulineaux"
    assert TOWNS.name_of("00000") is None


def test_locate_respects_holes(tmp_path):
    square = [[0, 0], [10, 0], [10, 10], [0, 10], [0, 0]]
    hole = [[4, 4], [6, 4], [6, 6], [4, 6], [4, 4]]
    path = tmp_path / "towns.geojson"
    feature = {"properties": {"code": "1", "nom": "Donut"}, "geometry": {"type": "Polygon", "coordinates": [square, hole]}}
    path.write_text(json.dumps({"type": "FeatureCollection", "features": [feature]}))
    towns = communes.Communes.load(path)
    assert towns.locate(lat=2, lon=2) == "Donut"
    assert towns.locate(lat=5, lon=5) is None


# --- GPSO names -------------------------------------------------------------------------------------------------------


@pytest.mark.parametrize(
    ("text", "expected"),
    [
        ("Platanus x hispanica", ("Platanus", "x hispanica", None)),
        ("Acer platanoides 'Crimson King'", ("Acer", "platanoides", "Crimson King")),
        ("Prunus serrulata 'kanzan'", ("Prunus", "serrulata", "Kanzan")),
        ('Tilia  europaea "pallida"', ("Tilia", "europaea", "Pallida")),
        ("Quercus sp.", ("Quercus", "sp.", None)),
        ("fraxinus ornus 'Obelisk'", ("Fraxinus", "ornus", "Obelisk")),
        ("Acer x freemanii Autumn Blaze", ("Acer", "x freemanii", "Autumn Blaze")),
        ("Acer Freemanii", ("Acer", "freemanii", None)),
        ("Tilia x vulgaris Hayne", ("Tilia", "x vulgaris", None)),
        ("Pyrus communis ssp.", ("Pyrus", "communis", None)),
        ("Pinus montana, Pinus mugo", ("Pinus", "montana", None)),
        ("CORYLUS Red Majestic", ("Corylus", None, "Red Majestic")),
        ("Indéterminé", (None, None, None)),
    ],
)
def test_parse_taxon_writes_names_like_the_paris_data(text, expected):
    assert gpso.parse_taxon(text) == expected


def test_tree_id_keeps_sources_apart():
    assert gpso.tree_id("92040_10302") == 192_040_010_302
    assert gpso.tree_id("92040_10302", duplicate=1) == 192_040_510_302
    assert gpso.tree_id("not an id") is None
    assert gpso.tree_id("92040_10302") > 3_000_000  # above every Paris id


# --- GPSO cleaning ----------------------------------------------------------------------------------------------------


def gpso_row(**fields):
    row = {
        "id_arbre": "92040_1",
        "code_insee": "92040",
        "commune": "ISSY-LES-MOULINEAUX",
        "site": "Rue Diderot 1",
        "genespvar": "Platanus x hispanica",
        "vernaculaire": "Platane commun",
        "particular": "Normal",
        "class_hauteur": "De 5 à 10 m",
        "geo_point_2d": "48.8254, 2.2719",
        "objectid": "1",
    }
    return row | fields


def clean_gpso(*rows, names=None):
    return gpso.clean(list(rows), TOWNS, names or {}, ut._in_coverage)


def test_gpso_clean_maps_every_column():
    (tree,), report = clean_gpso(gpso_row(particular="Remarquable"))
    assert tree == {
        "id": 192_040_000_001,
        "common_name": "Platane commun",
        "genus": "Platanus",
        "species": "x hispanica",
        "cultivar": None,
        "circumference_cm": None,
        "height_m": None,
        "stage": None,
        "remarkable": 1,
        "address": "Rue Diderot",  # the street's section number is not a house number
        "arrondissement": "Issy-les-Moulineaux",
        "lat": 48.8254,
        "lon": 2.2719,
        "city": "Issy-les-Moulineaux",
        "source": "gpso",
        "height_class": "5 à 10",
    }
    assert set(tree) == set(ut.COLUMNS)
    assert report.kept == 1


def test_gpso_clean_drops_stumps_and_renumbers_shared_ids():
    rows, report = clean_gpso(
        gpso_row(objectid="1"),
        gpso_row(objectid="2", geo_point_2d="48.8260, 2.2725"),  # another tree, same GPSO id
        gpso_row(id_arbre="92040_2", objectid="3", genespvar="Souche"),
    )
    assert [r["id"] for r in rows] == [192_040_000_001, 192_040_500_001]
    assert (report.not_trees, report.duplicate_ids, report.kept) == (1, 1, 2)


def test_gpso_clean_names_unnamed_trees():
    rows, _ = clean_gpso(
        gpso_row(id_arbre="92040_1", vernaculaire="", genespvar="Tilia cordata"),
        gpso_row(id_arbre="92040_2", vernaculaire="Indéterminé", genespvar="Indéterminé"),
        gpso_row(id_arbre="92040_3", vernaculaire="le Chêne du Japon", genespvar="Quercus acutissima"),
        names={("Tilia", "cordata"): "Tilleul", ("Tilia", ""): "Tilleul"},
    )
    assert [r["common_name"] for r in rows] == ["Tilleul", "Arbre", "Chêne du Japon"]


def test_gpso_clean_drops_trees_outside_the_map():
    rows, report = clean_gpso(gpso_row(geo_point_2d="45.76, 4.83"))
    assert rows == [] and report.outside_coverage == 1


def test_gpso_read_rows_needs_the_expected_fields(tmp_path):
    path = tmp_path / "gpso.csv"
    path.write_text("id_arbre;commune\n92040_1;ISSY\n", encoding="utf-8")
    with pytest.raises(ValueError, match="missing expected fields"):
        gpso.read_rows(path)


# --- merging --------------------------------------------------------------------------------------------------------


def test_paris_city_names_the_town_of_the_trees_outside_paris():
    assert ut.paris_city("PARIS 12E ARRDT", 48.84, 2.39, TOWNS) == "Paris"
    assert ut.paris_city("BOIS DE BOULOGNE", 48.87, 2.25, TOWNS) == "Paris"  # even where it laps over Neuilly
    assert ut.paris_city("SEINE-SAINT-DENIS", 48.9016, 2.4075, TOWNS) == "Pantin"
    assert ut.paris_city("SEINE-SAINT-DENIS", 48.9016, 2.4075) == "Seine-Saint-Denis"  # without boundaries


def test_common_names_pick_the_most_used_paris_name():
    rows = [
        {"genus": "Tilia", "species": "cordata", "common_name": "Tilleul"},
        {"genus": "Tilia", "species": "cordata", "common_name": "Tilleul"},
        {"genus": "Tilia", "species": "tomentosa", "common_name": "Tilleul argenté"},
        {"genus": None, "species": None, "common_name": "Arbre"},
    ]
    names = ut.common_names(rows)
    assert names[("Tilia", "cordata")] == "Tilleul"
    assert names[("Tilia", "tomentosa")] == "Tilleul argenté"
    assert names[("Tilia", "")] == "Tilleul"


def test_drop_near_duplicates_keeps_one_record_per_tree():
    paris = [{"lat": 48.8400000, "lon": 2.2500000, "genus": "Platanus"}]
    gpso_rows = [
        {"lat": 48.8400040, "lon": 2.2500040, "genus": "Platanus"},  # about 50 cm away: the same tree
        {"lat": 48.8400040, "lon": 2.2500040, "genus": "Tilia"},  # same place, another genus: kept
        {"lat": 48.8401000, "lon": 2.2500000, "genus": "Platanus"},  # 11 m away: kept
    ]
    kept, dropped = ut.drop_near_duplicates(gpso_rows, paris)
    assert dropped == 1
    assert [r["genus"] for r in kept] == ["Tilia", "Platanus"]
