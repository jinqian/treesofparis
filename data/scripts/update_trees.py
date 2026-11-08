#!/usr/bin/env python3
"""Download the Paris Open Data "les-arbres" dataset and build the app's pre-packaged `trees.db`.

The database schema is taken from Room's exported schema JSON (core/data/schemas/), so the Room entities stay the
single source of truth. When the cleaned data changes, the data version (core/data/trees-db-version.txt) is bumped;
the app then replaces the old copy of the tree data on existing installs.

Standard library only. Usage (from the repository root):

    python3 data/scripts/update_trees.py                 # download, clean, build
    python3 data/scripts/update_trees.py --skip-download # rebuild from data/raw/les-arbres.csv

Each run also writes core/data/trees-db-meta.json: when the data was extracted, from where, the dataset's own
« last modified » date, and how many records were kept or dropped. The app shows the extraction date from it.
"""

from __future__ import annotations

import argparse
import contextlib
import csv
import hashlib
import json
import os
import sqlite3
import sys
import tempfile
import urllib.request
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
EXPORT_URL = (
    "https://opendata.paris.fr/api/explore/v2.1/catalog/datasets/les-arbres/exports/csv?delimiter=%3B"
)
METADATA_URL = "https://opendata.paris.fr/api/explore/v2.1/catalog/datasets/les-arbres"
DATASET_PAGE = "https://opendata.paris.fr/explore/dataset/les-arbres/information/"
USER_AGENT = "treesofparis-data-update (+https://github.com/jinqian/treesofparis)"

# Must match GeoBounds.COVERAGE in :core:domain (checked by the tests): south, west, north, east.
COVERAGE = (48.735, 2.200, 48.920, 2.480)

EXPECTED_FIELDS = (
    "idbase",
    "arrondissement",
    "complementadresse",
    "adresse",
    "libellefrancais",
    "genre",
    "espece",
    "varieteoucultivar",
    "circonferenceencm",
    "hauteurenm",
    "stadedeveloppement",
    "remarquable",
    "geo_point_2d",
)

STAGES = {
    "Jeune (arbre)": "Jeune",
    "Jeune (arbre)Adulte": "Jeune adulte",
    "Adulte": "Adulte",
    "Mature": "Mature",
}

COLUMNS = (
    "id",
    "common_name",
    "genus",
    "species",
    "cultivar",
    "circumference_cm",
    "height_m",
    "stage",
    "remarkable",
    "address",
    "arrondissement",
    "lat",
    "lon",
)


class SchemaError(Exception):
    """The dataset or the Room schema does not look like what this script expects."""


@dataclass
class Report:
    total: int = 0
    kept: int = 0
    invalid_coordinates: int = 0
    outside_coverage: int = 0
    duplicates: int = 0

    def __str__(self) -> str:
        return (
            f"{self.total} records read, {self.kept} kept; dropped: {self.invalid_coordinates} invalid coordinates, "
            f"{self.outside_coverage} outside the coverage area, {self.duplicates} duplicate ids"
        )


# --- download / read -----------------------------------------------------------------------------------------------


def download(dest: Path) -> Path:
    """Downloads the full CSV export to [dest] atomically and writes a SHA-256 checksum next to it."""
    dest.parent.mkdir(parents=True, exist_ok=True)
    request = urllib.request.Request(EXPORT_URL, headers={"User-Agent": USER_AGENT})
    digest = hashlib.sha256()
    with urllib.request.urlopen(request, timeout=300) as response, tempfile.NamedTemporaryFile(
        dir=dest.parent, delete=False
    ) as tmp:
        for chunk in iter(lambda: response.read(1 << 16), b""):
            digest.update(chunk)
            tmp.write(chunk)
    os.replace(tmp.name, dest)
    dest.with_suffix(dest.suffix + ".sha256").write_text(f"{digest.hexdigest()}  {dest.name}\n")
    return dest


def read_rows(path: Path) -> list[dict]:
    with path.open(encoding="utf-8-sig", newline="") as f:
        reader = csv.DictReader(f, delimiter=";")
        missing = [name for name in EXPECTED_FIELDS if name not in (reader.fieldnames or [])]
        if missing:
            raise SchemaError(f"dataset is missing expected fields: {', '.join(missing)}")
        return list(reader)


# --- cleaning ------------------------------------------------------------------------------------------------------


def _text(value: str | None) -> str | None:
    value = (value or "").strip()
    return value or None


def _positive_int(value: str | None) -> int | None:
    """0 means "unknown" in this dataset."""
    try:
        number = int(float(value or ""))
    except ValueError:
        return None
    return number if number > 0 else None


def _coordinates(value: str | None) -> tuple[float, float] | None:
    try:
        lat, lon = (float(part) for part in (value or "").split(","))
    except ValueError:
        return None
    if lat == 0 and lon == 0:
        return None
    return lat, lon


def _in_coverage(lat: float, lon: float) -> bool:
    south, west, north, east = COVERAGE
    return south <= lat <= north and west <= lon <= east


def _cultivar(value: str | None) -> str | None:
    text = _text(value)
    return _text(text.strip("'\"")) if text else None


def _address(row: dict) -> str:
    parts = [p for p in (_text(row.get("adresse")), _text(row.get("complementadresse"))) if p]
    return ", ".join(parts)


def clean(rows: list[dict]) -> tuple[list[dict], Report]:
    """Returns rows keyed by Room column name, sorted by id, and a report of what was dropped."""
    report = Report(total=len(rows))
    seen: dict[int, dict] = {}
    for row in rows:
        coordinates = _coordinates(row.get("geo_point_2d"))
        try:
            tree_id = int(row["idbase"])
        except (TypeError, ValueError):
            coordinates = None
        if coordinates is None:
            report.invalid_coordinates += 1
            continue
        lat, lon = coordinates
        if not _in_coverage(lat, lon):
            report.outside_coverage += 1
            continue
        if tree_id in seen:
            report.duplicates += 1
            continue
        genus = _text(row.get("genre"))
        seen[tree_id] = {
            "id": tree_id,
            "common_name": _text(row.get("libellefrancais")) or genus or "Arbre",
            "genus": genus,
            "species": _text(row.get("espece")),
            "cultivar": _cultivar(row.get("varieteoucultivar")),
            "circumference_cm": _positive_int(row.get("circonferenceencm")),
            "height_m": _positive_int(row.get("hauteurenm")),
            "stage": STAGES.get(_text(row.get("stadedeveloppement")) or "", _text(row.get("stadedeveloppement"))),
            "remarkable": 1 if _text(row.get("remarquable")) == "OUI" else 0,
            "address": _address(row),
            "arrondissement": _text(row.get("arrondissement")) or "",
            "lat": lat,
            "lon": lon,
        }
    cleaned = [seen[k] for k in sorted(seen)]
    report.kept = len(cleaned)
    return cleaned, report


def content_hash(rows: list[dict]) -> str:
    ordered = sorted(rows, key=lambda r: r["id"])
    return hashlib.sha256(json.dumps(ordered, sort_keys=True, ensure_ascii=False).encode()).hexdigest()


def next_version(current: int, previous_hash: str | None, new_hash: str) -> int:
    """The data version goes up only when the cleaned content changed since the last build."""
    if previous_hash is None or previous_hash == new_hash:
        return current
    return current + 1


# --- database ------------------------------------------------------------------------------------------------------


def latest_schema(schema_dir: Path, max_version: int) -> dict:
    """Room schema JSON with the highest version <= [max_version] (a data-only bump keeps the previous schema)."""
    versions = sorted(int(p.stem) for p in schema_dir.glob("*.json") if p.stem.isdigit() and int(p.stem) <= max_version)
    if not versions:
        raise SchemaError(f"no Room schema <= {max_version} in {schema_dir}; build :core:data first")
    return json.loads((schema_dir / f"{versions[-1]}.json").read_text())


def build_db(rows: list[dict], schema: dict, out: Path, version: int) -> None:
    """Builds [out] atomically with exactly Room's schema, Room's identity hash and `user_version` = [version]."""
    database = schema["database"]
    entity = next((e for e in database.get("entities", []) if e["tableName"] == "trees"), None)
    if entity is None:
        raise SchemaError("Room schema has no `trees` table")
    schema_columns = {field["columnName"] for field in entity["fields"]}
    if schema_columns != set(COLUMNS):
        raise SchemaError(f"Room columns {sorted(schema_columns)} differ from the script's {sorted(COLUMNS)}")

    out.parent.mkdir(parents=True, exist_ok=True)
    fd, tmp_name = tempfile.mkstemp(dir=out.parent, suffix=".db")
    os.close(fd)
    try:
        with contextlib.closing(sqlite3.connect(tmp_name)) as db, db:
            db.execute(entity["createSql"].replace("${TABLE_NAME}", "trees"))
            for index in entity.get("indices", []):
                db.execute(index["createSql"].replace("${TABLE_NAME}", "trees"))
            for query in database.get("setupQueries", []):
                db.execute(query)
            placeholders = ", ".join("?" for _ in COLUMNS)
            db.executemany(
                f"INSERT INTO trees ({', '.join(COLUMNS)}) VALUES ({placeholders})",
                ([row[c] for c in COLUMNS] for row in rows),
            )
            db.execute(f"PRAGMA user_version = {int(version)}")
        with contextlib.closing(sqlite3.connect(tmp_name, isolation_level=None)) as db:
            db.execute("VACUUM")
        os.chmod(tmp_name, 0o644)
        os.replace(tmp_name, out)
    finally:
        if os.path.exists(tmp_name):
            os.remove(tmp_name)


# --- CLI -----------------------------------------------------------------------------------------------------------


# --- extraction metadata -------------------------------------------------------------------------------------------


def extracted_at(raw: Path) -> str:
    """When the CSV was downloaded (its modification time), as an ISO 8601 UTC timestamp."""
    when = datetime.fromtimestamp(raw.stat().st_mtime, tz=timezone.utc)
    return when.strftime("%Y-%m-%dT%H:%M:%SZ")


def fetch_metadata() -> dict | None:
    """The dataset's Opendatasoft metadata, or None when it cannot be fetched (the build does not depend on it)."""
    request = urllib.request.Request(METADATA_URL, headers={"User-Agent": USER_AGENT})
    try:
        with urllib.request.urlopen(request, timeout=60) as response:
            return json.load(response)
    except (OSError, ValueError) as error:
        print(f"WARNING: could not read the dataset metadata ({error}); datasetModifiedAt left empty.")
        return None


def dataset_modified_at(metadata: dict | None) -> str | None:
    """The dataset's own « last modified » date from its Opendatasoft metadata."""
    return ((metadata or {}).get("metas") or {}).get("default", {}).get("modified")


def previous_dataset_modified_at(meta_file: Path) -> str | None:
    """Keeps the dataset date of the last download when the data is rebuilt without downloading."""
    if not meta_file.exists():
        return None
    return json.loads(meta_file.read_text()).get("datasetModifiedAt")


def build_meta(report: Report, version: int, extracted: str, dataset_modified: str | None) -> dict:
    return {
        "dataVersion": version,
        "datasetModifiedAt": dataset_modified,
        "dropped": {
            "duplicateIds": report.duplicates,
            "invalidCoordinates": report.invalid_coordinates,
            "outsideCoverage": report.outside_coverage,
        },
        "extractedAt": extracted,
        "license": "ODbL",
        "recordsKept": report.kept,
        "recordsRead": report.total,
        "source": DATASET_PAGE,
        "sourceExport": EXPORT_URL,
    }


def write_meta(path: Path, meta: dict) -> None:
    path.write_text(json.dumps(meta, indent=2, sort_keys=True, ensure_ascii=False) + "\n")


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--raw", type=Path, default=REPO / "data/raw/les-arbres.csv", help="downloaded CSV")
    parser.add_argument("--skip-download", action="store_true", help="reuse the CSV already in --raw")
    parser.add_argument("--output", type=Path, default=REPO / "core/data/src/main/assets/trees.db")
    parser.add_argument(
        "--schema-dir", type=Path, default=REPO / "core/data/schemas/com.treesofparis.core.data.db.TreeDatabase"
    )
    parser.add_argument("--version-file", type=Path, default=REPO / "core/data/trees-db-version.txt")
    parser.add_argument("--hash-file", type=Path, default=REPO / "core/data/trees-db-content.sha256")
    parser.add_argument("--meta-file", type=Path, default=REPO / "core/data/trees-db-meta.json")
    args = parser.parse_args(argv)

    if not args.skip_download:
        print(f"Downloading {EXPORT_URL}")
        download(args.raw)
    rows, report = clean(read_rows(args.raw))
    print(report)

    new_hash = content_hash(rows)
    previous_hash = args.hash_file.read_text().strip() if args.hash_file.exists() else None
    current = int(args.version_file.read_text().strip())
    version = next_version(current, previous_hash, new_hash)

    build_db(rows, latest_schema(args.schema_dir, version), args.output, version)
    args.version_file.write_text(f"{version}\n")
    args.hash_file.write_text(f"{new_hash}\n")

    modified = (
        previous_dataset_modified_at(args.meta_file) if args.skip_download else dataset_modified_at(fetch_metadata())
    )
    meta = build_meta(report, version, extracted_at(args.raw), modified)
    write_meta(args.meta_file, meta)
    print(f"Extraction {meta['extractedAt']} (dataset modified {modified or 'unknown'}) -> {args.meta_file.name}")

    size_mb = args.output.stat().st_size / 1_000_000
    change = "unchanged" if version == current else f"bumped {current} -> {version}"
    print(f"Wrote {args.output.relative_to(REPO)} ({size_mb:.1f} MB), data version {version} ({change})")
    if report.outside_coverage:
        print("WARNING: some trees are outside GeoBounds.COVERAGE; consider widening it (Kotlin + this script).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
