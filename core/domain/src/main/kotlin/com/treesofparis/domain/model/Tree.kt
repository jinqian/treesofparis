package com.treesofparis.domain.model

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
)
