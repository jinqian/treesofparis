"""Tests for data/scripts/update_trees.py. Run: data/.venv/bin/python -I -m pytest data/tests"""

import json
import os
import re
import sqlite3
import sys
from pathlib import Path

import pytest

REPO = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(REPO / "data" / "scripts"))

import update_trees as ut  # noqa: E402

FIXTURE = Path(__file__).parent / "fixtures" / "sample.csv"
SCHEMA_DIR = REPO / "android" / "core" / "data" / "schemas" / "fr.arbresdeparis.core.data.db.TreeDatabase"


@pytest.fixture
def cleaned():
    return ut.clean(ut.read_rows(FIXTURE))


def by_id(rows):
    return {r["id"]: r for r in rows}


# --- reading -------------------------------------------------------------------------------------------------------


def test_read_rows_handles_bom_and_semicolons():
    rows = ut.read_rows(FIXTURE)
    assert len(rows) == 14
    assert rows[0]["idbase"] == "243481"  # BOM stripped from the first header


def test_read_rows_missing_field_fails(tmp_path):
    broken = tmp_path / "broken.csv"
    broken.write_text("idbase;adresse\n1;RUE X\n", encoding="utf-8")
    with pytest.raises(ut.SchemaError, match="geo_point_2d"):
        ut.read_rows(broken)


# --- cleaning ------------------------------------------------------------------------------------------------------


def test_clean_drops_invalid_coordinates(cleaned):
    rows, _ = cleaned
    assert not {900001, 900002, 900003} & by_id(rows).keys()


def test_clean_keeps_trees_managed_outside_paris(cleaned):
    rows, _ = cleaned
    areas = {by_id(rows)[i]["arrondissement"] for i in (152671, 163213, 164974)}
    assert areas == {"VAL-DE-MARNE", "HAUTS-DE-SEINE", "SEINE-SAINT-DENIS"}


def test_clean_drops_rows_outside_coverage(cleaned):
    rows, report = cleaned
    assert 900004 not in by_id(rows)
    assert report.outside_coverage == 1


def test_clean_deduplicates_ids(cleaned):
    rows, report = cleaned
    assert [r["id"] for r in rows].count(243481) == 1
    assert report.duplicates == 1


def test_clean_keeps_rows_with_missing_optional_fields(cleaned):
    rows, _ = cleaned
    tree = by_id(rows)[900005]
    assert tree["species"] is None
    assert tree["genus"] == "Tilia"


def test_clean_zero_height_and_circumference_become_null(cleaned):
    rows, _ = cleaned
    tree = by_id(rows)[163213]
    assert tree["height_m"] is None
    assert tree["circumference_cm"] is None


def test_clean_common_name_falls_back_to_genus(cleaned):
    rows, _ = cleaned
    assert by_id(rows)[164974]["common_name"] == "Prunus"


def test_clean_common_name_falls_back_to_arbre():
    row = dict(ut.read_rows(FIXTURE)[0], libellefrancais="", genre="")
    rows, _ = ut.clean([row])
    assert rows[0]["common_name"] == "Arbre"


def test_clean_normalizes_stage(cleaned):
    rows, _ = cleaned
    assert by_id(rows)[125167]["stage"] == "Jeune adulte"
    assert by_id(rows)[2038015]["stage"] == "Jeune"
    assert by_id(rows)[243481]["stage"] == "Adulte"
    assert by_id(rows)[163213]["stage"] is None


def test_clean_strips_cultivar_quotes(cleaned):
    rows, _ = cleaned
    assert by_id(rows)[164974]["cultivar"] == "Kanzan"


def test_clean_maps_remarkable_and_coordinates(cleaned):
    rows, _ = cleaned
    tree = by_id(rows)[106714]
    assert tree["remarkable"] == 1
    assert tree["lat"] == pytest.approx(48.85579301591001)
    assert tree["lon"] == pytest.approx(2.30026431835801)
    assert by_id(rows)[243481]["remarkable"] == 0


def test_clean_output_is_sorted_by_id(cleaned):
    rows, _ = cleaned
    ids = [r["id"] for r in rows]
    assert ids == sorted(ids)


def test_report_counts_dropped_rows(cleaned):
    _, report = cleaned
    assert (report.total, report.kept) == (14, 9)
    assert (report.invalid_coordinates, report.outside_coverage, report.duplicates) == (3, 1, 1)
    assert "9" in str(report)


def test_coverage_matches_kotlin_domain():
    kotlin = (REPO / "android/core/domain/src/main/kotlin/fr/arbresdeparis/domain/model/GeoBounds.kt").read_text()
    match = re.search(
        r"COVERAGE = GeoBounds\(south = ([\d.]+), west = ([\d.]+), north = ([\d.]+), east = ([\d.]+)\)", kotlin
    )
    assert match, "GeoBounds.COVERAGE not found"
    assert tuple(float(v) for v in match.groups()) == ut.COVERAGE


# --- database ------------------------------------------------------------------------------------------------------


@pytest.fixture
def schema():
    return ut.latest_schema(SCHEMA_DIR, max_version=10**6)  # the newest schema


def build(tmp_path, cleaned, schema, version=1, name="trees.db"):
    out = tmp_path / name
    ut.build_db(cleaned[0], schema, out, version)
    return out


def test_build_db_uses_room_schema(tmp_path, cleaned, schema):
    out = build(tmp_path, cleaned, schema)
    entity = next(e for e in schema["database"]["entities"] if e["tableName"] == "trees")
    with sqlite3.connect(out) as db:
        table_sql = db.execute("SELECT sql FROM sqlite_master WHERE name = 'trees'").fetchone()[0]
        index_names = {r[0] for r in db.execute("SELECT name FROM sqlite_master WHERE type = 'index'")}
    assert table_sql == entity["createSql"].replace("${TABLE_NAME}", "trees").replace("IF NOT EXISTS ", "")
    assert "index_trees_lat_lon" in index_names


def test_build_db_writes_room_identity_hash(tmp_path, cleaned, schema):
    out = build(tmp_path, cleaned, schema)
    with sqlite3.connect(out) as db:
        stored = db.execute("SELECT identity_hash FROM room_master_table WHERE id = 42").fetchone()[0]
    assert stored == schema["database"]["identityHash"]


def test_build_db_sets_user_version(tmp_path, cleaned, schema):
    out = build(tmp_path, cleaned, schema, version=7)
    with sqlite3.connect(out) as db:
        assert db.execute("PRAGMA user_version").fetchone()[0] == 7


def test_build_db_stores_all_rows(tmp_path, cleaned, schema):
    out = build(tmp_path, cleaned, schema)
    with sqlite3.connect(out) as db:
        assert db.execute("SELECT COUNT(*) FROM trees").fetchone()[0] == 9
        assert db.execute("SELECT height_m FROM trees WHERE id = 163213").fetchone()[0] is None


def test_build_db_is_idempotent(tmp_path, cleaned, schema):
    first = build(tmp_path, cleaned, schema, name="a.db").read_bytes()
    out = build(tmp_path, cleaned, schema, name="a.db")
    assert out.read_bytes() == first


def test_removed_tree_ids_lists_the_trees_gone_since_the_previous_data(tmp_path, cleaned, schema):
    previous = build(tmp_path, cleaned, schema, name="previous.db")
    rows = cleaned[0]
    assert ut.removed_tree_ids(previous, rows) == []
    assert ut.removed_tree_ids(previous, rows[1:]) == [rows[0]["id"]]


def test_removed_tree_ids_without_previous_data_is_empty(tmp_path, cleaned):
    assert ut.removed_tree_ids(tmp_path / "missing.db", cleaned[0]) == []


def test_latest_schema_picks_highest_version_not_above_max(tmp_path):
    for v in (1, 2, 5):
        (tmp_path / f"{v}.json").write_text(json.dumps({"formatVersion": 1, "database": {"version": v}}))
    assert ut.latest_schema(tmp_path, max_version=4)["database"]["version"] == 2


# --- versioning ----------------------------------------------------------------------------------------------------


def test_next_version_first_run_keeps_version():
    assert ut.next_version(current=1, previous_hash=None, new_hash="abc") == 1


def test_next_version_same_content_keeps_version():
    assert ut.next_version(current=3, previous_hash="abc", new_hash="abc") == 3


def test_next_version_changed_content_bumps_version():
    assert ut.next_version(current=3, previous_hash="abc", new_hash="def") == 4


def test_content_hash_ignores_input_order(cleaned):
    rows, _ = cleaned
    assert ut.content_hash(rows) == ut.content_hash(list(reversed(rows)))


# --- extraction metadata -------------------------------------------------------------------------------------------


def test_extracted_at_is_the_raw_csv_download_time(tmp_path):
    raw = tmp_path / "les-arbres.csv"
    raw.write_text("x")
    os.utime(raw, (1_778_088_600, 1_778_088_600))  # 2026-05-06T17:30:00Z
    assert ut.extracted_at(raw) == "2026-05-06T17:30:00Z"


def test_dataset_modified_at_reads_opendatasoft_metadata():
    payload = {"metas": {"default": {"modified": "2026-11-06T09:34:19+00:00", "records_count": 220339}}}
    assert ut.dataset_modified_at(payload) == "2026-11-06T09:34:19+00:00"
    assert ut.dataset_modified_at({"metas": {}}) is None


def test_build_meta_records_source_counts_and_version(cleaned):
    _, report = cleaned
    meta = ut.build_meta(
        report,
        version=3,
        extracted="2026-11-06T16:30:00Z",
        dataset_modified="2026-11-06T09:34:19+00:00",
    )
    assert meta == {
        "dataVersion": 3,
        "datasetModifiedAt": "2026-11-06T09:34:19+00:00",
        "dropped": {
            "duplicateIds": report.duplicates,
            "invalidCoordinates": report.invalid_coordinates,
            "outsideCoverage": report.outside_coverage,
        },
        "extractedAt": "2026-11-06T16:30:00Z",
        "license": "ODbL",
        "recordsKept": report.kept,
        "recordsRead": report.total,
        "source": ut.DATASET_PAGE,
        "sourceExport": ut.EXPORT_URL,
        "wikipediaLinks": 0,
    }


def test_previous_dataset_modified_at_survives_a_rebuild_without_download(tmp_path):
    meta_file = tmp_path / "trees-db-meta.json"
    assert ut.previous_dataset_modified_at(meta_file) is None
    meta_file.write_text(json.dumps({"datasetModifiedAt": "2026-11-06T09:34:19+00:00"}))
    assert ut.previous_dataset_modified_at(meta_file) == "2026-11-06T09:34:19+00:00"


def test_meta_file_is_stable_json(tmp_path, cleaned):
    _, report = cleaned
    meta_file = tmp_path / "trees-db-meta.json"
    meta = ut.build_meta(report, version=1, extracted="2026-11-06T16:30:00Z", dataset_modified=None)
    ut.write_meta(meta_file, meta)
    first = meta_file.read_text()
    ut.write_meta(meta_file, meta)
    assert meta_file.read_text() == first
    assert first.endswith("\n")
    assert json.loads(first)["datasetModifiedAt"] is None


# --- Wikipedia links -----------------------------------------------------------------------------------------------


@pytest.mark.parametrize(
    ("genus", "species", "expected"),
    [
        ("Platanus", "x hispanica", "Platanus × hispanica"),
        ("Aesculus", "hippocastanum", "Aesculus hippocastanum"),
        ("Gleditsia", "triacanthos f. Inermis", "Gleditsia triacanthos"),
        ("Pinus", "nigra subsp. nigra", "Pinus nigra"),
        ("Tilia", "sp.", None),
        ("Ulmus", None, None),
        ("Non spécifié", "sp.", None),
    ],
)
def test_species_name_matches_wikidata_taxon_names(genus, species, expected):
    assert ut.species_name(genus, species) == expected


def test_genus_name_skips_placeholders():
    assert ut.genus_name("Platanus") == "Platanus"
    assert ut.genus_name("Non spécifié") is None
    assert ut.genus_name(None) is None


def test_sparql_titles_reads_french_article_titles():
    response = {
        "results": {
            "bindings": [
                {"name": {"value": "Platanus × hispanica"}, "article": {"value": "https://fr.wikipedia.org/wiki/Platane_commun"}},
                {"name": {"value": "Gleditsia triacanthos"}, "article": {"value": "https://fr.wikipedia.org/wiki/F%C3%A9vier_d%27Am%C3%A9rique"}},
            ]
        }
    }
    assert ut.parse_titles(response) == {"Platanus × hispanica": "Platane commun", "Gleditsia triacanthos": "Févier d'Amérique"}


def test_resolve_titles_only_asks_for_names_missing_from_the_cache():
    asked = []

    def fetch(names, genus):
        asked.append((sorted(names), genus))
        return {"Tilia tomentosa": "Tilleul argenté"}

    cache = {"Platanus × hispanica": "Platane commun"}
    ut.resolve_titles(["Platanus × hispanica", "Tilia tomentosa", "Tilia × euchlora"], cache, fetch, genus=False)
    assert asked == [(["Tilia tomentosa", "Tilia × euchlora"], False)]
    # A name without an article is cached as None, so it is not asked again next month.
    assert cache == {"Platanus × hispanica": "Platane commun", "Tilia tomentosa": "Tilleul argenté", "Tilia × euchlora": None}


def test_taxon_links_prefer_species_then_genus_then_nothing():
    rows = [
        {"genus": "Platanus", "species": "x hispanica"},
        {"genus": "Platanus", "species": "x hispanica"},
        {"genus": "Tilia", "species": "sp."},
        {"genus": "Tilia", "species": "x euchlora"},
        {"genus": "Ulmus", "species": None},
        {"genus": "Zelkova", "species": "serrata"},
        {"genus": None, "species": None},
    ]
    cache = {
        "species": {"Platanus × hispanica": "Platane commun", "Tilia × euchlora": None, "Zelkova serrata": None},
        "genera": {"Platanus": "Platane", "Tilia": "Tilleul", "Ulmus": "Orme", "Zelkova": None},
    }
    assert ut.taxon_links(rows, cache) == [
        {"genus": "Platanus", "species": "x hispanica", "wikipedia_title": "Platane commun", "genus_only": 0},
        {"genus": "Tilia", "species": "sp.", "wikipedia_title": "Tilleul", "genus_only": 1},
        {"genus": "Tilia", "species": "x euchlora", "wikipedia_title": "Tilleul", "genus_only": 1},
        {"genus": "Ulmus", "species": "", "wikipedia_title": "Orme", "genus_only": 1},
    ]


def test_content_hash_changes_with_the_links(cleaned):
    rows, _ = cleaned
    link = [{"genus": "Platanus", "species": "", "wikipedia_title": "Platane", "genus_only": 1}]
    assert ut.content_hash(rows) != ut.content_hash(rows, link)
    assert ut.content_hash(rows, link) == ut.content_hash(rows, link)


def test_build_meta_counts_wikipedia_links(cleaned):
    _, report = cleaned
    meta = ut.build_meta(report, version=1, extracted="2026-11-06T16:30:00Z", dataset_modified=None, wikipedia_links=12)
    assert meta["wikipediaLinks"] == 12


def test_resolve_titles_survives_a_failed_batch_and_retries_it_next_time(capsys):
    def failing(names, genus):
        raise OSError("HTTP Error 504: Gateway Timeout")

    cache = {}
    ut.resolve_titles(["Tilia tomentosa"], cache, failing, genus=False)
    assert cache == {}
    assert "Wikidata" in capsys.readouterr().out


def test_build_db_writes_taxon_links(tmp_path, cleaned, schema):
    rows, _ = cleaned
    links = [{"genus": "Platanus", "species": "x hispanica", "wikipedia_title": "Platane commun", "genus_only": 0}]
    out = tmp_path / "trees.db"
    ut.build_db(rows, schema, out, 2, links)
    with sqlite3.connect(out) as db:
        assert db.execute("SELECT genus, species, wikipedia_title, genus_only FROM taxon_links").fetchall() == [
            ("Platanus", "x hispanica", "Platane commun", 0)
        ]


def test_build_db_refuses_links_without_a_taxon_links_table(tmp_path, cleaned, schema):
    rows, _ = cleaned
    database = dict(schema["database"])
    database["entities"] = [e for e in database["entities"] if e["tableName"] == "trees"]
    with pytest.raises(ut.SchemaError):
        ut.build_db(rows, {"database": database}, tmp_path / "trees.db", 2, [{"genus": "A", "species": "", "wikipedia_title": "A", "genus_only": 1}])
