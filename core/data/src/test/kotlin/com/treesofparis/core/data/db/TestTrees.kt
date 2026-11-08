package com.treesofparis.core.data.db

@Suppress("LongParameterList") // a test fixture with defaults for every column
fun treeEntity(
    id: Long,
    lat: Double = 48.85,
    lon: Double = 2.35,
    commonName: String = "Platane",
    genus: String? = "Platanus",
    species: String? = null,
    stage: String? = null,
    remarkable: Boolean = false,
    heightM: Int? = null,
    circumferenceCm: Int? = null,
    arrondissement: String = "PARIS 5E ARRDT",
) = TreeEntity(
    id = id,
    commonName = commonName,
    genus = genus,
    species = species,
    cultivar = null,
    circumferenceCm = circumferenceCm,
    heightM = heightM,
    stage = stage,
    remarkable = remarkable,
    address = "Quai de la Tournelle",
    arrondissement = arrondissement,
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
