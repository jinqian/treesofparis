package com.treesofparis.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * The French Wikipedia article for a genus + species of the tree data, built by `data/scripts/update_trees.py` from
 * Wikidata. [species] is the dataset's species as stored in `trees` (empty when unknown). [genusOnly] is true when the
 * species has no article and the link is the genus's.
 */
@Entity(tableName = "taxon_links", primaryKeys = ["genus", "species"])
data class TaxonLinkEntity(
    val genus: String,
    val species: String,
    @ColumnInfo(name = "wikipedia_title") val wikipediaTitle: String,
    @ColumnInfo(name = "genus_only") val genusOnly: Boolean,
)
