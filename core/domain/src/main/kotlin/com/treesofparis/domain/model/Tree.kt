package com.treesofparis.domain.model

import com.treesofparis.domain.repository.TreeRepository

/** A tree managed by the City of Paris. Optional fields are null when the dataset does not know them. */
data class Tree(
    val id: Long,
    val commonName: String,
    val genus: String?,
    val species: String?,
    val cultivar: String?,
    val circumferenceCm: Int?,
    val heightM: Int?,
    val stage: String?,
    val remarkable: Boolean,
    val address: String,
    val arrondissement: String,
    val location: GeoPoint,
    /** Its French Wikipedia article, when known. Only loaded for a single tree ([TreeRepository.getTree]). */
    val wikipedia: WikipediaArticle? = null,
)

/** A French Wikipedia article: the species's, or the genus's when [isGenusArticle] (no article for the species). */
data class WikipediaArticle(
    val title: String,
    val isGenusArticle: Boolean,
)
