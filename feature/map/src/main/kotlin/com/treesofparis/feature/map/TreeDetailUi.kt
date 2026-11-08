package com.treesofparis.feature.map

import com.treesofparis.core.designsystem.leaf.TreeLeaf

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
    /** The genus drawing shown as the sheet's watermark. */
    val leaf: TreeLeaf = TreeLeaf.GENERIC,
    /** The French Wikipedia article to open, or null when the species and genus have none. */
    val wikipedia: WikipediaUi? = null,
)

/** A French Wikipedia article. [genus] is set when it is the genus's article (the species has none). */
data class WikipediaUi(
    val url: String,
    val title: String,
    val genus: String?,
)

/** A figure and its unit, shown large and small: « 16 » « m ». */
data class MeasureUi(
    val value: String,
    val unit: String,
)
