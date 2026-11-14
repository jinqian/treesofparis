"""Tests for data/scripts/build_herbier.py. Run: python3 -I -m pytest data/tests"""

import json
import re
import sqlite3
import sys
from pathlib import Path

import pytest

REPO = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(REPO / "data" / "scripts"))

import build_herbier as hb  # noqa: E402
from test_build_web_map import LINKS_SQL, STORIES_SQL, TREES_SQL  # noqa: E402

NNBSP = " "


# --- the app's sources ------------------------------------------------------------------------------------------------


def test_leaves_are_the_21_drawn_genera_of_the_app_in_its_order():
    leaves = hb.leaves()
    assert len(leaves) == 21
    assert [leaf.genus for leaf in leaves[:3]] == ["Platanus", "Aesculus", "Tilia"]
    platanus = leaves[0]
    assert (platanus.label, platanus.plural) == ("Platane", "platanes")
    assert platanus.path.startswith("M50 92L50 52M50 52C40 50 26 54")
    assert platanus.path.endswith("L50 18M50 52L70 30M50 52L22 44M50 52L78 44")
    assert all(re.fullmatch(r"[MmLlCcQqAaZz0-9. -]+", leaf.path) for leaf in leaves)
    assert hb.leaves()[5].label == "Cerisier, prunier"


def test_flowering_seasons_merge_the_sheets_of_a_genus_in_the_order_of_the_year():
    seasons = {s.genus: s for s in hb.flowering_seasons()}
    assert seasons["Acer"] == hb.Season("Acer", frozenset({5}), showy=False)
    assert seasons["Aesculus"] == hb.Season("Aesculus", frozenset({5, 6}), showy=True)
    assert seasons["Ulmus"].months == frozenset({2, 3, 4})
    order = [s.genus for s in hb.flowering_seasons()]
    assert order.index("Corylus") < order.index("Ulmus") < order.index("Acer")  # Feb–Mar, Feb–Apr, May


# --- French formatting (as in the app) --------------------------------------------------------------------------------


def test_format_count_groups_thousands_with_a_narrow_space():
    assert hb.format_count(220339) == f"220{NNBSP}339"
    assert hb.format_count(42) == "42"


def test_format_share():
    assert hb.format_share(0.19312) == f"19,3{NNBSP}%"


@pytest.mark.parametrize(
    "months, expected",
    [({5}, "mai"), ({2, 3, 4}, "février – avril"), ({6, 7}, "juin – juillet")],
)
def test_format_month_range(months, expected):
    assert hb.format_month_range(months) == expected


@pytest.mark.parametrize(
    "items, expected",
    [(["if"], "if"), (["noisetier", "orme"], "noisetier et orme"), (["a", "b", "c"], "a, b et c")],
)
def test_french_list(items, expected):
    assert hb.french_list(items) == expected


@pytest.mark.parametrize(
    "address, expected",
    [
        ("PARC DES BUTTES CHAUMONT / 7 RUE BOTZARIS, 19-08", "Parc des Buttes Chaumont"),
        ("ROUTE DU LAC A BAGATELLE", "Route du Lac à Bagatelle"),
    ],
)
def test_short_place(address, expected):
    assert hb.short_place(address) == expected


# --- figures ----------------------------------------------------------------------------------------------------------


@pytest.fixture
def herbier_db(tmp_path):
    db = tmp_path / "trees.db"
    con = sqlite3.connect(db)
    con.execute(TREES_SQL)
    con.execute(LINKS_SQL)
    con.execute(STORIES_SQL)
    row = (
        "INSERT INTO trees (id, common_name, genus, species, circumference_cm, height_m, stage, remarkable, address, "
        "arrondissement, lat, lon, city, source) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
    )
    trees = []
    next_id = iter(range(1, 1000))
    for _ in range(6):
        trees.append((next(next_id), "Platane", "Platanus", "x hispanica", 120, 15, "Adulte", 0, "RUE A",
                      "PARIS 15E ARRDT", 48.84, 2.29, "Paris", "paris"))
    for _ in range(3):
        trees.append((next(next_id), "Tilleul", "Tilia", "tomentosa", 80, 10, "Jeune", 0, "RUE B",
                      "PARIS 12E ARRDT", 48.84, 2.39, "Paris", "paris"))
    trees.append((next(next_id), "Ginkgo", "Ginkgo", "biloba", 60, 8, None, 0, "RUE C", "PARIS 1ER ARRDT",
                  48.86, 2.34, "Paris", "paris"))
    # Remarkable: the tallest is a plane tree in the Bois de Boulogne, the widest a lime tree in the 12e.
    trees.append((100, "Platane commun", "Platanus", "x hispanica", 500, 40, "Mature", 1,
                  "ROUTE DU LAC A BAGATELLE", "BOIS DE BOULOGNE", 48.86, 2.25, "Paris", "paris"))
    trees.append((101, "Platane d'Orient", "Platanus", "orientalis", 400, 30, "Mature", 1,
                  "PARC DE BERCY / 41 RUE PAUL BELMONDO, 12-01", "PARIS 12E ARRDT", 48.83, 2.38, "Paris", "paris"))
    trees.append((102, "Tilleul argenté", "Tilia", "tomentosa", 740, 25, "Mature", 1,
                  "SQUARE TROUSSEAU", "PARIS 12E ARRDT", 48.85, 2.38, "Paris", "paris"))
    # Another town: on the map, but not in the Herbier (its figures are about Paris).
    trees.append((900, "Chêne", "Quercus", "robur", 100, 20, "Adulte", 1, "RUE D", "Meudon", 48.81, 2.23,
                  "Meudon", "gpso"))
    con.executemany(row, trees)
    con.execute(
        "INSERT INTO remarkable_trees (tree_id, story) VALUES (?, ?)",
        (102, "Le tilleul appartient à la famille des Malvacées. Planté vers 1860, il ombrage le square."),
    )
    con.commit()
    con.close()
    meta = tmp_path / "meta.json"
    meta.write_text(json.dumps({"extractedAt": "2026-11-06T16:30:03Z", "dataVersion": 2}))
    return db, meta


def figures(db):
    con = sqlite3.connect(db)
    con.row_factory = sqlite3.Row
    try:
        return hb.herbier(con)
    finally:
        con.close()


def test_totals_count_paris_only(herbier_db):
    h = figures(herbier_db[0])
    assert (h["total"], h["genusCount"], h["taxonCount"]) == ("13", "3", "4")


def test_headline_is_the_most_planted_drawn_genus(herbier_db):
    headline = figures(herbier_db[0])["headline"]
    assert headline == {
        "genus": "Platanus",
        "oneIn": 2,  # 13 / 8 rounds to 2
        "name": "platane",
        "count": "8",
        "plural": "platanes",
        "runnerUp": {"count": "4", "plural": "tilleuls"},
    }


def test_genera_are_drawn_only_with_their_share_and_bar(herbier_db):
    h = figures(herbier_db[0])
    assert [g["genus"] for g in h["genera"]] == ["Platanus", "Tilia"]  # Ginkgo is not drawn
    assert h["genera"][0]["share"] == f"61,5{NNBSP}%"
    assert h["genera"][1]["fraction"] == 0.5
    assert h["drawnShare"] == f"92{NNBSP}%"


def test_stages_youngest_first_and_unknown_apart(herbier_db):
    h = figures(herbier_db[0])
    assert [(s["label"], s["count"]) for s in h["stages"]] == [("Jeune", "3"), ("Adulte", "6"), ("Mature", "3")]
    assert h["stages"][1]["fraction"] == 1.0
    assert h["unknownStageCount"] == "1"


def test_remarkable_records(herbier_db):
    r = figures(herbier_db[0])["remarkable"]
    assert r["count"] == "3"
    assert r["leading"] == {"count": 2, "plural": "platanes"}
    assert r["tallest"] == {"name": "Platane commun", "value": "40 m",
                            "place": "Bois de Boulogne · Route du Lac à Bagatelle", "id": 100}
    assert r["widest"]["value"] == "7,40 m"
    assert r["widest"]["place"] == "Paris 12e · Square Trousseau"


def test_most_wooded_arrondissements_leave_the_woods_out(herbier_db):
    areas = figures(herbier_db[0])["areas"]
    assert [(a["rank"], a["name"], a["count"]) for a in areas] == [
        (1, "Paris 15e", "6"),
        (2, "Paris 12e", "5"),
        (3, "Paris 1er", "1"),
    ]


def test_remarkable_trees_by_area_arrondissements_first(herbier_db):
    areas = figures(herbier_db[0])["remarkableAreas"]
    assert [a["name"] for a in areas] == ["Paris 12e", "Bois de Boulogne"]
    assert [t["name"] for t in areas[0]["trees"]] == ["Platane d'Orient", "Tilleul argenté"]
    assert areas[0]["trees"][0]["detail"] == "Parc de Bercy · 30 m"
    assert areas[0]["trees"][1]["teaser"] == "Planté vers 1860, il ombrage le square."
    assert areas[0]["trees"][0]["teaser"] is None


def test_flowering_rows_are_the_drawn_genera(herbier_db):
    rows = figures(herbier_db[0])["flowering"]
    assert all(hb.leaf_for(r["genus"]) for r in rows)
    aesculus = next(r for r in rows if r["genus"] == "Aesculus")
    assert aesculus["months"] == [5, 6]
    assert aesculus["range"] == "mai – juin"
    assert aesculus["showy"] is True


# --- the page ---------------------------------------------------------------------------------------------------------


def test_build_writes_the_page(herbier_db, tmp_path):
    db, meta = herbier_db
    out = tmp_path / "herbier" / "index.html"
    hb.build(db, meta, out)
    page = out.read_text(encoding="utf-8")

    assert page.startswith("<!doctype html>")
    assert "$" not in re.sub(r"<script.*?</script>", "", page, flags=re.S)  # every placeholder filled
    assert '<link rel="canonical" href="https://arbresdeparis.fr/herbier/">' in page
    assert "1 arbre sur 2 à Paris est un platane" in page
    assert 'href="../maps/?genre=Platanus"' in page
    assert 'href="../maps/?arbre=102"' in page
    assert "Planté vers 1860, il ombrage le square." in page
    assert "Paris 15e" in page
    assert "Ginkgo" not in page
    assert "6 novembre 2026" in page  # the extraction date
    assert 'data-months="5 6"' in page  # Aesculus, for the « en fleur » script


def test_build_escapes_the_dataset_text(herbier_db, tmp_path):
    db, meta = herbier_db
    con = sqlite3.connect(db)
    con.execute("UPDATE trees SET common_name = '<b>Tilleul</b>' WHERE id = 102")
    con.commit()
    con.close()
    out = tmp_path / "index.html"
    hb.build(db, meta, out)
    page = out.read_text(encoding="utf-8")
    assert "<b>Tilleul</b>" not in page
    assert "&lt;b&gt;Tilleul&lt;/b&gt;" in page
