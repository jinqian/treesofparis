package fr.arbresdeparis.feature.herbier.mapper

import fr.arbresdeparis.domain.format.formatArea
import fr.arbresdeparis.domain.model.RemarkableStory
import fr.arbresdeparis.domain.model.Tree
import fr.arbresdeparis.feature.herbier.RemarkableAreaUi
import fr.arbresdeparis.feature.herbier.RemarkableTreeUi
import java.text.Collator
import java.util.Locale

private val SENTENCE_END = Regex("""(?<=[.!?…])\s+(?=\p{Lu}|[«“"])""")
private const val BOTANICAL_OPENING = "famille des "

private val ARRONDISSEMENT = Regex("""^PARIS (\d{1,2})(?:ER|E) ARRDT$""")

/**
 * The remarkable trees by area: Paris 1er to 20e first, then the woods and the other places, by name. Within an
 * area, by name: « Parc Monceau · 22 m ».
 */
fun List<Tree>.toRemarkableAreasUi(stories: Map<Long, RemarkableStory> = emptyMap()): List<RemarkableAreaUi> {
    val collator = Collator.getInstance(Locale.FRENCH)
    return groupBy { it.arrondissement }
        .entries
        .sortedWith(
            compareBy<Map.Entry<String, List<Tree>>> { arrondissementNumber(it.key) ?: Int.MAX_VALUE }
                .thenBy(collator) { formatArea(it.key) },
        ).map { (area, trees) ->
            RemarkableAreaUi(
                name = formatArea(area),
                trees =
                    trees
                        .sortedWith(compareBy(collator, Tree::commonName))
                        .map { tree ->
                            RemarkableTreeUi(
                                id = tree.id,
                                name = tree.commonName,
                                detail =
                                    listOfNotNull(
                                        shortPlace(tree.address),
                                        tree.heightM?.let { "$it m" },
                                    ).joinToString(" · "),
                                teaser = storyTeaser(stories[tree.id]?.story),
                            )
                        },
            )
        }
}

private fun arrondissementNumber(raw: String): Int? =
    ARRONDISSEMENT
        .matchEntire(raw.trim())
        ?.groupValues
        ?.get(1)
        ?.toInt()

/**
 * The first sentence of a story that tells the tree: about a third of the stories open with its botanical family
 * (« Le marronnier appartient à la famille des Sapindacées. », « Appartenant à la famille des Pinacées, … »), which
 * the list skips.
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
