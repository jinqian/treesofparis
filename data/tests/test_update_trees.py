"""Tests for data/scripts/update_trees.py. Run: data/.venv/bin/python -I -m pytest data/tests"""

import json
import re
import sqlite3
import sys
from pathlib import Path

import pytest

REPO = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(REPO / "data" / "scripts"))

import update_trees as ut  # noqa: E402

FIXTURE = Path(__file__).parent / "fixtures" / "sample.csv"
SCHEMA_DIR = REPO / "core" / "data" / "schemas" / "com.treesofparis.core.data.db.TreeDatabase"


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
    kotlin = (REPO / "core/domain/src/main/kotlin/com/treesofparis/domain/model/GeoBounds.kt").read_text()
    match = re.search(
        r"COVERAGE = GeoBounds\(south = ([\d.]+), west = ([\d.]+), north = ([\d.]+), east = ([\d.]+)\)", kotlin
    )
    assert match, "GeoBounds.COVERAGE not found"
    assert tuple(float(v) for v in match.groups()) == ut.COVERAGE


# --- database ------------------------------------------------------------------------------------------------------


@pytest.fixture
def schema():
    return ut.latest_schema(SCHEMA_DIR, max_version=1)


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
