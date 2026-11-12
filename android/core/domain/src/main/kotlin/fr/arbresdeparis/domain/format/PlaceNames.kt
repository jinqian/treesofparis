package fr.arbresdeparis.domain.format

import java.util.Locale

private val FRENCH = Locale.FRENCH
private val PARTICLES = setOf("de", "du", "des", "la", "le", "les", "et", "sur", "sous", "au", "aux", "en", "à")

/** « Henri IV », « Jean XXIII »: words of I, V and X only. No French place word is spelt that way. */
private val ROMAN_NUMERAL = Regex("^[ivx]{2,}$")
private val ARRONDISSEMENT = Regex("""^PARIS (\d{1,2})(?:ER|E) ARRDT$""")

/** « d'estienne », « l'est »: an elided article or preposition, then the word it is glued to. */
private val ELISION = Regex("""^([dl])'(.+)$""")

/**
 * Display name for the dataset's area field: "PARIS 19E ARRDT" → "Paris 19e", "PARIS 1ER ARRDT" → "Paris 1er",
 * "SEINE-SAINT-DENIS" → "Seine-Saint-Denis". Shared by the features that list or show trees.
 */
fun formatArea(raw: String): String {
    val match = ARRONDISSEMENT.matchEntire(raw.trim()) ?: return formatPlace(raw)
    val number = match.groupValues[1].toInt()
    return "Paris $number${if (number == 1) "er" else "e"}"
}

/**
 * Dataset text to French title case: "QUAI DE LA SEINE" → "Quai de la Seine", "RUE D'ESTIENNE D'ORVES" → "Rue
 * d'Estienne d'Orves", "VILLE-D'AVRAY" → "Ville-d'Avray". The doubled apostrophes of the Paris data (« L''EST ») count
 * as one.
 */
fun formatPlace(raw: String): String =
    raw
        .trim()
        .replace(Regex("['’]+"), "'")
        .lowercase(FRENCH)
        .split(" ")
        .mapIndexed { index, word ->
            when {
                // The dataset has no accents: a lone « A » is the preposition « à ».
                index > 0 && word == "a" -> "à"

                index > 0 && word in PARTICLES -> word

                index > 0 && ROMAN_NUMERAL.matches(word) -> word.uppercase(FRENCH)

                else -> word.titleCaseCompound(first = index == 0)
            }
        }.joinToString(" ")

private fun String.titleCaseCompound(first: Boolean): String =
    split("-")
        .mapIndexed { index, part ->
            val elision = ELISION.matchEntire(part)
            when {
                // « d'Avray », « l'Est »; the article keeps its capital only at the very start: « L'Haÿ-les-Roses ».
                elision != null -> {
                    val (article, word) = elision.destructured
                    val head = if (first && index == 0) article.uppercase(FRENCH) else article
                    "$head'${word.capitalized()}"
                }

                index > 0 && part in PARTICLES -> {
                    part
                }

                else -> {
                    part.capitalized()
                }
            }
        }.joinToString("-")

private fun String.capitalized(): String = replaceFirstChar { it.titlecase(FRENCH) }
