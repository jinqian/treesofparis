package com.treesofparis.domain.format

import java.util.Locale

private val FRENCH = Locale.FRENCH
private val PARTICLES = setOf("de", "du", "des", "la", "le", "les", "et", "sur", "sous", "au", "aux", "en", "à")
private val ARRONDISSEMENT = Regex("""^PARIS (\d{1,2})(?:ER|E) ARRDT$""")

/**
 * Display name for the dataset's area field: "PARIS 19E ARRDT" → "Paris 19e", "PARIS 1ER ARRDT" → "Paris 1er",
 * "SEINE-SAINT-DENIS" → "Seine-Saint-Denis". Shared by the features that list or show trees.
 */
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
