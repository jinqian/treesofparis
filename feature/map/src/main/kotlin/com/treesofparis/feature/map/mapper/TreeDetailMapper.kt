package com.treesofparis.feature.map.mapper

import com.treesofparis.domain.model.Tree
import com.treesofparis.feature.map.TreeDetailUi
import java.util.Locale

private val FRENCH = Locale.FRENCH
private val PARTICLES = setOf("de", "du", "des", "la", "le", "les", "et", "sur", "sous", "au", "aux", "en", "à")
private val ARRONDISSEMENT = Regex("""^PARIS (\d{1,2})(?:ER|E) ARRDT$""")

fun Tree.toDetailUi(isFavorite: Boolean) =
    TreeDetailUi(
        id = id,
        name = commonName,
        scientificName = scientificName(),
        area = formatArea(arrondissement),
        address = formatPlace(address),
        height = heightM?.let { "$it m" },
        circumference = circumferenceCm?.let { "$it cm" },
        stage = stage,
        remarkable = remarkable,
        isFavorite = isFavorite,
    )

private fun Tree.scientificName(): String? {
    val latin = listOfNotNull(genus, species).joinToString(" ").ifBlank { return null }
    return cultivar?.let { "$latin '$it'" } ?: latin
}

/** "PARIS 19E ARRDT" → "Paris 19e", "PARIS 1ER ARRDT" → "Paris 1er", "SEINE-SAINT-DENIS" → "Seine-Saint-Denis". */
fun formatArea(raw: String): String {
    val match = ARRONDISSEMENT.matchEntire(raw.trim()) ?: return formatPlace(raw)
    val number = match.groupValues[1].toInt()
    return "Paris $number${if (number == 1) "er" else "e"}"
}

/** Upper-case dataset text to French title case: "QUAI DE LA SEINE" → "Quai de la Seine". */
fun formatPlace(raw: String): String =
    raw
        .trim()
        .lowercase(FRENCH)
        .split(" ")
        .mapIndexed { index, word -> if (index > 0 && word in PARTICLES) word else word.titleCaseCompound() }
        .joinToString(" ")

private fun String.titleCaseCompound(): String =
    split("-")
        .mapIndexed { index, part ->
            if (index > 0 && part in PARTICLES) part else part.replaceFirstChar { it.titlecase(FRENCH) }
        }.joinToString("-")
