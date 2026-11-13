package fr.arbresdeparis.domain.format

private val SENTENCE_END = Regex("""(?<=[.!?…])\s+(?=\p{Lu}|[«“"])""")
private const val BOTANICAL_OPENING = "famille des "

/**
 * The first sentence of a remarkable tree's story that tells the tree: about a third of the stories open with its
 * botanical family (« Le marronnier appartient à la famille des Sapindacées. », « Appartenant à la famille des
 * Pinacées, … »), which is skipped. Shown in the tree sheet and the Herbier's list.
 */
fun storyTeaser(story: String?): String? {
    val sentences =
        story
            ?.split(SENTENCE_END)
            ?.map(String::trim)
            ?.filter(String::isNotEmpty)
            .orEmpty()
    return sentences.firstOrNull { !it.contains(BOTANICAL_OPENING) } ?: sentences.firstOrNull()
}
