package com.treesofparis.feature.map

/** What the detail sheet shows. Null fields are unknown in the dataset; the UI shows a placeholder. */
data class TreeDetailUi(
    val id: Long,
    val name: String,
    val scientificName: String?,
    val area: String,
    val address: String,
    val height: MeasureUi?,
    val circumference: MeasureUi?,
    val stage: String?,
    val remarkable: Boolean,
    val isFavorite: Boolean,
)

/** A figure and its unit, shown large and small: « 16 » « m ». */
data class MeasureUi(
    val value: String,
    val unit: String,
)
