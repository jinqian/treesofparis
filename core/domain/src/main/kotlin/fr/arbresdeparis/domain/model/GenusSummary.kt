package fr.arbresdeparis.domain.model

/**
 * A genus of the dataset (« Tilia »), named by the French name most of its trees carry (« Tilleul »), with how many
 * trees it has and how many of them are remarkable.
 */
data class GenusSummary(
    val genus: String,
    val commonName: String,
    val treeCount: Int,
    val remarkableCount: Int,
)
