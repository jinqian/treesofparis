package com.treesofparis.core.data.db

import androidx.room.ColumnInfo

/** Rows of the aggregate queries in [TreeDao]. */
internal data class GenusCountRow(
    val genus: String,
    val count: Int,
)

internal data class StageCountRow(
    val stage: String?,
    val count: Int,
)

internal data class AreaCountRow(
    @ColumnInfo(name = "arrondissement") val area: String,
    val count: Int,
)

/** Trees of one genus that carry one French name, and how many of them are remarkable. */
internal data class GenusNameCountRow(
    val genus: String,
    @ColumnInfo(name = "common_name") val commonName: String,
    val count: Int,
    @ColumnInfo(name = "remarkable_count") val remarkableCount: Int,
)
