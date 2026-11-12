package fr.arbresdeparis.domain.model

import fr.arbresdeparis.domain.repository.TreeRepository

/**
 * A tree from one of the open datasets ([source]): the City of Paris's, or a neighbouring town's. Optional fields are
 * null when the dataset does not know them.
 */
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
    /** The town it stands in: « Paris », « Issy-les-Moulineaux »... */
    val city: String = PARIS,
    val source: TreeSource = TreeSource.PARIS,
    /** A height range in metres (« 5 à 10 ») when the source has no exact [heightM]. */
    val heightClass: String? = null,
) {
    companion object {
        const val PARIS = "Paris"
    }
}

/** The open dataset a tree comes from. [id] is the value stored in the tree database. */
enum class TreeSource(
    val id: String,
) {
    /** « Les arbres », Ville de Paris (Paris Data), ODbL. */
    PARIS("paris"),

    /** « Arbres », Grand Paris Seine Ouest (data.seineouest.fr), Licence Ouverte. */
    GPSO("gpso"),
    ;

    companion object {
        fun fromId(id: String): TreeSource = entries.firstOrNull { it.id == id } ?: PARIS
    }
}

/** A French Wikipedia article: the species's, or the genus's when [isGenusArticle] (no article for the species). */
data class WikipediaArticle(
    val title: String,
    val isGenusArticle: Boolean,
)
