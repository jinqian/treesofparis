package com.treesofparis.feature.map.mapper

import com.treesofparis.domain.format.formatArea
import com.treesofparis.domain.format.formatPlace
import com.treesofparis.domain.model.Tree
import com.treesofparis.feature.map.MeasureUi
import com.treesofparis.feature.map.TreeDetailUi

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
    )

private fun Tree.scientificName(): String? {
    val latin = listOfNotNull(genus, species).joinToString(" ").ifBlank { return null }
    return cultivar?.let { "$latin '$it'" } ?: latin
}
