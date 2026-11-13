package fr.arbresdeparis.feature.herbier.mapper

import fr.arbresdeparis.domain.format.formatArea
import fr.arbresdeparis.domain.format.storyTeaser
import fr.arbresdeparis.domain.model.RemarkableStory
import fr.arbresdeparis.domain.model.Tree
import fr.arbresdeparis.feature.herbier.RemarkableAreaUi
import fr.arbresdeparis.feature.herbier.RemarkableTreeUi
import java.text.Collator
import java.util.Locale

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
