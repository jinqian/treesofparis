package com.treesofparis.feature.map.mapper

import com.treesofparis.core.designsystem.leaf.TreeLeaf
import com.treesofparis.domain.format.formatArea
import com.treesofparis.domain.format.formatPlace
import com.treesofparis.domain.model.Tree
import com.treesofparis.feature.map.MeasureUi
import com.treesofparis.feature.map.TreeDetailUi
import com.treesofparis.feature.map.WikipediaUi
import java.net.URLEncoder

fun Tree.toDetailUi(isFavorite: Boolean) =
    TreeDetailUi(
        id = id,
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
    )

private const val WIKIPEDIA_FR = "https://fr.wikipedia.org/wiki/"

/** « Févier d'Amérique » → https://fr.wikipedia.org/wiki/F%C3%A9vier_d%27Am%C3%A9rique */
internal fun wikipediaUrl(title: String): String =
    // The Charset overload needs API 33; the encoding-name one works down to minSdk 26.
    WIKIPEDIA_FR + URLEncoder.encode(title.replace(' ', '_'), "UTF-8")

private fun Tree.scientificName(): String? {
    val latin = listOfNotNull(genus, species).joinToString(" ").ifBlank { return null }
    return cultivar?.let { "$latin '$it'" } ?: latin
}
