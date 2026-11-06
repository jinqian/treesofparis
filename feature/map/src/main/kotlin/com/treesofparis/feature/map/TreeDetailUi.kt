package com.treesofparis.feature.map

/** What the detail sheet shows. Null fields are unknown in the dataset; the UI shows a placeholder. */
data class TreeDetailUi(
    val id: Long,
    val name: String,
    val scientificName: String?,
    val area: String,
    val address: String,
    val height: String?,
    val circumference: String?,
    val stage: String?,
    val remarkable: Boolean,
    val isFavorite: Boolean,
)
