"""Tests for data/scripts/build_web_map.py. Run: data/.venv/bin/python -I -m pytest data/tests"""

import json
import sqlite3
import sys
from pathlib import Path

import pytest

REPO = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(REPO / "data" / "scripts"))

import build_web_map as wm  # noqa: E402
import update_trees as ut  # noqa: E402


# --- place names: the same cases as PlaceNamesTest in :core:domain --------------------------------------------------


@pytest.mark.parametrize(
    "raw, expected",
    [
        ("PARIS 19E ARRDT", "Paris 19e"),
        ("PARIS 1ER ARRDT", "Paris 1er"),
        ("SEINE-SAINT-DENIS", "Seine-Saint-Denis"),
        ("VAL-DE-MARNE", "Val-de-Marne"),
        ("BOIS DE BOULOGNE", "Bois de Boulogne"),
    ],
)
def test_format_area(raw, expected):
    assert wm.format_area(raw) == expected


@pytest.mark.parametrize(
    "raw, expected",
    [
        ("SQUARE JEAN XXIII", "Square Jean XXIII"),
        ("SQUARE BARYE / 2 BOULEVARD HENRI IV", "Square Barye / 2 Boulevard Henri IV"),
        ("RUE VIVIENNE", "Rue Vivienne"),
        ("QUAI DE LA SEINE", "Quai de la Seine"),
        ("CIMETIÈRE / AVENUE DES MERISIERS / DIV 72", "Cimetière / Avenue des Merisiers / Div 72"),
        ("ROUTE DU LAC A BAGATELLE", "Route du Lac à Bagatelle"),
        ("AVENUE DE LA PORTE DE VINCENNES", "Avenue de la Porte de Vincennes"),
        ("Rue d'Estienne d'Orves", "Rue d'Estienne d'Orves"),
        ("VILLE-D'AVRAY", "Ville-d'Avray"),
        ("AVENUE DE L''EST", "Avenue de l'Est"),
        ("L'HAŸ-LES-ROSES", "L'Haÿ-les-Roses"),
    ],
)
def test_format_place(raw, expected):
    assert wm.format_place(raw) == expected


# --- cells ------------------------------------------------------------------------------------------------------------


def test_coverage_matches_the_data_script():
    assert wm.COVERAGE == ut.COVERAGE


def test_cell_of_counts_rows_and_columns_from_the_south_west_corner():
    south, west, _, _ = wm.COVERAGE
    assert wm.cell_of(south + 0.0001, west + 0.0001) == "0_0"
    assert wm.cell_of(south + 2.5 * wm.CELL_LAT, west + 3.5 * wm.CELL_LON) == "2_3"


# --- build ------------------------------------------------------------------------------------------------------------

TREES_SQL = (
    "CREATE TABLE trees (id INTEGER PRIMARY KEY, common_name TEXT NOT NULL, genus TEXT, species TEXT, cultivar TEXT, "
    "circumference_cm INTEGER, height_m INTEGER, stage TEXT, remarkable INTEGER NOT NULL, address TEXT NOT NULL, "
    "arrondissement TEXT NOT NULL, lat REAL NOT NULL, lon REAL NOT NULL, city TEXT NOT NULL, source TEXT NOT NULL, "
    "height_class TEXT)"
)
LINKS_SQL = (
    "CREATE TABLE taxon_links (genus TEXT NOT NULL, species TEXT NOT NULL, wikipedia_title TEXT NOT NULL, "
    "genus_only INTEGER NOT NULL, PRIMARY KEY (genus, species))"
)

STORIES_SQL = (
    "CREATE TABLE remarkable_trees (tree_id INTEGER PRIMARY KEY, site TEXT, qualification TEXT, summary TEXT, "
    "story TEXT, planting_year INTEGER, label TEXT, council_decision TEXT, sign_url TEXT, photo_url TEXT, "
    "photo_credit TEXT)"
)


@pytest.fixture
def small_db(tmp_path):
    db = tmp_path / "trees.db"
    con = sqlite3.connect(db)
    con.execute(TREES_SQL)
    con.execute(LINKS_SQL)
    con.executemany(
        "INSERT INTO trees VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
        [
            (1, "Platane", "Platanus", "x hispanica", None, 250, 22, "Mature", 0, "QUAI DE LA SEINE",
             "PARIS 19E ARRDT", 48.8880001, 2.3700004, "Paris", "paris", None),
            (2, "Platane", "Platanus", "x hispanica", None, None, None, None, 1, "QUAI DE LA SEINE",
             "PARIS 19E ARRDT", 48.8881, 2.3701, "Paris", "paris", None),
            (3, "Chêne", "Quercus", "robur", "Fastigiata", 90, 12, "Adulte", 0, "ROUTE DU LAC A BAGATELLE",
             "BOIS DE BOULOGNE", 48.8650, 2.2480, "Paris", "paris", None),
            (192_078_000_001, "Charme commun", "Carpinus", "betulus", None, None, None, None, 0,
             "Rue de Marnes", "Ville-d'Avray", 48.8250, 2.1900, "Ville-d'Avray", "gpso", "5 à 10"),
        ],
    )
    con.execute("INSERT INTO taxon_links VALUES ('Platanus', 'x hispanica', 'Platane commun', 0)")
    con.execute(STORIES_SQL)
    con.execute(
        "INSERT INTO remarkable_trees VALUES (?,?,?,?,?,?,?,?,?,?,?)",
        (2, "Square Maurice Gardette", "Paysager", "Cet arbre est classé remarquable pour son ampleur.",
         "Le platane appartient à la famille des Platanacées. Planté en 1879, il fait l’attraction du square.",
         1882, None, "2015 DEVE 152", "https://capgeo.sig.paris.fr/PdfEtImages/ArbresRemarquables/PDF/2.pdf",
         "https://capgeo.sig.paris.fr/PdfEtImages/ArbresRemarquables/Photos2023/1/2.jpg", "Sonia Yassa / Ville de Paris"),
    )
    con.commit()
    con.close()
    meta = tmp_path / "meta.json"
    meta.write_text(json.dumps({"extractedAt": "2026-11-06T16:30:03Z", "dataVersion": 2}))
    return db, meta


def test_build_writes_cells_remarkable_taxa_and_index(small_db, tmp_path):
    db, meta = small_db
    out = tmp_path / "out"
    index = wm.build(db, meta, out)

    assert index["treeCount"] == 4
    assert index["remarkableCount"] == 1
    assert index["extractedAt"] == "2026-11-06T16:30:03Z"
    assert index["fields"][0] == "id" and len(index["fields"]) == 15
    assert sorted(index["cells"]) == sorted(p.stem for p in (out / "cells").glob("*.json"))
    assert len(index["cells"]) == 3  # trees 1 and 2 share a cell

    rows = {r[0]: r for key in index["cells"] for r in json.loads((out / "cells" / f"{key}.json").read_text())}
    assert rows[1] == [1, 48.888, 2.37, "Platane", "Platanus", "x hispanica", None, 250, 22, "Mature", 0,
                       "Quai de la Seine", "Paris 19e", None, "paris"]
    assert rows[3][11:] == ["Route du Lac à Bagatelle", "Bois de Boulogne", None, "paris"]
    # Another town's tree: its town as the area, its height range, its source.
    assert rows[192_078_000_001][11:] == ["Rue de Marnes", "Ville-d'Avray", "5 à 10", "gpso"]
    assert [r[0] for r in json.loads((out / "remarkable.json").read_text())] == [2]
    assert json.loads((out / "taxa.json").read_text()) == {"Platanus|x hispanica": ["Platane commun", 0]}
    groups = json.loads((out / "clusters.json").read_text())
    assert sorted(groups) == ["coarse", "district", "fine", "mid"]
    assert all(sum(g[2] for g in level) == 4 for level in groups.values())


def test_clusters_count_trees_at_their_mean_position():
    levels = wm.clusters([(48.8501, 2.3501), (48.8503, 2.3503), (48.8800, 2.3000)])
    assert levels["fine"][0] == [48.8502, 2.3502, 2, 1.0]  # biggest group first, full size
    assert levels["fine"][1][2:] == [1, 0.707]  # half the trees: √½ of the size


def test_gather_merges_cells_around_the_densest_and_keeps_far_ones_apart():
    groups = wm.gather([(48.8500, 2.35, 30), (48.8504, 2.35, 10), (48.8600, 2.35, 5)], 0.001, 0.0015)
    assert [g[2] for g in groups] == [40, 5]
    assert groups[0][0] == pytest.approx(48.8501)


def test_build_replaces_an_older_output(small_db, tmp_path):
    db, meta = small_db
    out = tmp_path / "out"
    (out / "cells").mkdir(parents=True)
    (out / "cells" / "99_99.json").write_text("[]")
    wm.build(db, meta, out)
    assert not (out / "cells" / "99_99.json").exists()


# --- remarkable trees' stories (issue #74) ----------------------------------------------------------------------------


def test_build_writes_the_stories_of_the_remarkable_trees(small_db, tmp_path):
    db, meta = small_db
    out = tmp_path / "out"
    index = wm.build(db, meta, out)
    stories = json.loads((out / "stories.json").read_text())
    assert index["storyCount"] == 1
    assert stories == {
        "2": {
            "site": "Square Maurice Gardette",
            "qualification": "Paysager",
            "summary": "Cet arbre est classé remarquable pour son ampleur.",
            "story": "Le platane appartient à la famille des Platanacées. Planté en 1879, il fait l’attraction du square.",
            "teaser": "Planté en 1879, il fait l’attraction du square.",
            "plantingYear": 1882,
            "councilDecision": "2015 DEVE 152",
            "signUrl": "https://capgeo.sig.paris.fr/PdfEtImages/ArbresRemarquables/PDF/2.pdf",
            "photoUrl": "https://capgeo.sig.paris.fr/PdfEtImages/ArbresRemarquables/Photos2023/1/2.jpg",
            "photoCredit": "Sonia Yassa / Ville de Paris",
        }
    }


def test_build_without_stories_table_writes_none(tmp_path):
    db = tmp_path / "old.db"
    con = sqlite3.connect(db)
    con.execute(TREES_SQL)
    con.execute(LINKS_SQL)
    con.commit()
    con.close()
    index = wm.build(db, tmp_path / "missing.json", tmp_path / "out")
    assert index["storyCount"] == 0
    assert json.loads((tmp_path / "out" / "stories.json").read_text()) == {}


# The same cases as StoryTeaserTest in :core:domain.
@pytest.mark.parametrize(
    "story, expected",
    [
        (
            "Le platane appartient à la famille des Platanacées. Planté en 1873, cet arbre est le témoin des "
            "plantations du parc. Il mesure 30 m.",
            "Planté en 1873, cet arbre est le témoin des plantations du parc.",
        ),
        ("Cet orme a été planté en 1935. Il est vieux.", "Cet orme a été planté en 1935."),
        ("Le saule appartient à la famille des Salicacées.", "Le saule appartient à la famille des Salicacées."),
        (None, None),
        ("  ", None),
    ],
)
def test_story_teaser(story, expected):
    assert wm.story_teaser(story) == expected


# --- one genus on the map (from the Herbier, issue #84) ---------------------------------------------------------------


def test_build_writes_the_groups_of_each_genus(small_db, tmp_path):
    db, meta = small_db
    out = tmp_path / "out"
    index = wm.build(db, meta, out)

    # Drawn genera carry the app's name for them; the others their most common French name.
    assert index["genera"] == {
        "Carpinus": ["Charme", 1, "Carpinus"],
        "Platanus": ["Platane", 2, "Platanus"],
        "Quercus": ["Chêne", 1, "Quercus"],
    }
    platanus = json.loads((out / "genera" / "Platanus.json").read_text())
    assert sorted(platanus) == ["coarse", "district", "fine", "mid"]
    assert all(sum(g[2] for g in level) == 2 for level in platanus.values())


@pytest.mark.parametrize("genus, expected", [("Tilia", "Tilia"), ("x Chitalpa", "x_Chitalpa"), ("Non spécifié", "Non_sp_cifi_")])
def test_genus_file_names_are_plain(genus, expected):
    assert wm.genus_file(genus) == expected
