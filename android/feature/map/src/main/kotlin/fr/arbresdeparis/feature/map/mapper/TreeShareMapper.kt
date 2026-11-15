package fr.arbresdeparis.feature.map.mapper

import fr.arbresdeparis.domain.model.TreeLink
import fr.arbresdeparis.feature.map.TreeDetailUi

/** What « Partager » sends (issue #88): « Platane commun · Quai de la Seine, Paris 19e », then the tree's link. */
fun TreeDetailUi.toShareText(): String {
    val place = listOf(address, area).filter { it.isNotBlank() }.joinToString(", ")
    return "$name · $place\n${TreeLink(id, location).url}"
}
