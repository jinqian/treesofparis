package com.treesofparis.core.data.db

fun treeEntity(
    id: Long,
    lat: Double = 48.85,
    lon: Double = 2.35,
    commonName: String = "Platane",
) = TreeEntity(
    id = id,
    commonName = commonName,
    genus = "Platanus",
    species = null,
    cultivar = null,
    circumferenceCm = null,
    heightM = null,
    stage = null,
    remarkable = false,
    address = "Quai de la Tournelle",
    arrondissement = "PARIS 5E ARRDT",
    lat = lat,
    lon = lon,
)

/** The production DAO is read-only (the data ships pre-packaged), so tests insert through raw SQL. */
fun TreeDatabase.insert(vararg trees: TreeEntity) {
    val db = openHelper.writableDatabase
    trees.forEach { t ->
        db.execSQL(
            "INSERT INTO trees (id, common_name, genus, species, cultivar, circumference_cm, height_m, stage, " +
                "remarkable, address, arrondissement, lat, lon) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
            arrayOf<Any?>(
                t.id,
                t.commonName,
                t.genus,
                t.species,
                t.cultivar,
                t.circumferenceCm,
                t.heightM,
                t.stage,
                if (t.remarkable) 1 else 0,
                t.address,
                t.arrondissement,
                t.lat,
                t.lon,
            ),
        )
    }
}
