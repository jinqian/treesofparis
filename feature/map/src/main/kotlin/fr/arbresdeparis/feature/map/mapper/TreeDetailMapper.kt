package fr.arbresdeparis.feature.map.mapper

import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf
import fr.arbresdeparis.domain.format.formatArea
import fr.arbresdeparis.domain.format.formatMonth
import fr.arbresdeparis.domain.format.formatMonthRange
import fr.arbresdeparis.domain.format.formatPlace
import fr.arbresdeparis.domain.model.FloweringSeason
import fr.arbresdeparis.domain.model.Tree
import fr.arbresdeparis.feature.map.FloweringUi
import fr.arbresdeparis.feature.map.MeasureUi
import fr.arbresdeparis.feature.map.TreeDetailUi
import fr.arbresdeparis.feature.map.WikipediaUi
import java.net.URLEncoder
import java.time.Month

fun Tree.toDetailUi(
    isFavorite: Boolean,
    flowering: FloweringUi? = null,
) = TreeDetailUi(
    id = id,
    location = location,
    name = commonName,
    scientificName = scientificName(),
    area = formatArea(arrondissement),
    address = formatPlace(address),
    height = heightM?.let { MeasureUi(it.toString(), "m") },
    circumference = circumferenceCm?.let { MeasureUi(it.toString(), "cm") },
    stage = stage,
    remarkable = remarkable,
    isFavorite = isFavorite,
    leaf = TreeLeaf.forGenus(genus),
    wikipedia =
        wikipedia?.let {
            WikipediaUi(
                url = wikipediaUrl(it.title),
                title = it.title,
                genus =
                    genus.takeIf { _ ->
                        it.isGenusArticle
                    },
            )
        },
    flowering = flowering,
)

fun FloweringSeason.toFloweringUi(now: Month) =
    FloweringUi(
        months = Month.entries.map { it in months },
        showyFlowers = showyFlowers,
        range = formatMonthRange(months),
        currentMonth = now.value,
        currentMonthName = formatMonth(now),
        monthsUntilFlowering = monthsUntilFlowering(now),
    )

private const val WIKIPEDIA_FR = "https://fr.wikipedia.org/wiki/"

/** « Févier d'Amérique » → https://fr.wikipedia.org/wiki/F%C3%A9vier_d%27Am%C3%A9rique */
internal fun wikipediaUrl(title: String): String =
    // The Charset overload needs API 33; the encoding-name one works down to minSdk 26.
    WIKIPEDIA_FR + URLEncoder.encode(title.replace(' ', '_'), "UTF-8")

internal fun Tree.scientificName(): String? {
    val latin = listOfNotNull(genus, species).joinToString(" ").ifBlank { return null }
    return cultivar?.let { "$latin '$it'" } ?: latin
}
