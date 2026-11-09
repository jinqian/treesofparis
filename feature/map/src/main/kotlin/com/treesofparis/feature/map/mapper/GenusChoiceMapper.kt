package com.treesofparis.feature.map.mapper

import com.treesofparis.core.designsystem.leaf.TreeLeaf
import com.treesofparis.domain.model.GenusSummary
import com.treesofparis.feature.map.GenusChoiceUi
import com.treesofparis.feature.map.GenusPickerUi
import java.text.Normalizer
import java.text.NumberFormat
import java.util.Locale

/** Splits [genera] (already in display order) into the drawn grid and the other genera; [selected] is highlighted. */
fun List<GenusSummary>.toGenusPicker(
    selected: String?,
    remarkableOnly: Boolean,
): GenusPickerUi {
    val choices =
        map {
            val count = if (remarkableOnly) it.remarkableCount else it.treeCount
            GenusChoiceUi(
                genus = it.genus,
                commonName = it.commonName,
                leaf = TreeLeaf.forGenus(it.genus),
                count = count,
                countLabel = NumberFormat.getIntegerInstance(Locale.FRENCH).format(count),
                selected = it.genus == selected,
            )
        }
    val (drawn, others) = choices.partition { it.leaf != TreeLeaf.GENERIC }
    return GenusPickerUi(drawn = drawn, others = others, remarkableOnly = remarkableOnly)
}

/**
 * Whether a search [query] finds this genus: any word start of its shown [name], French name or Latin genus, ignoring
 * case and accents (« erable » finds « Érable », « gink » finds Ginkgo). A blank query finds every genus.
 */
fun GenusChoiceUi.matches(
    query: String,
    name: String,
): Boolean {
    val wanted = query.normalized()
    if (wanted.isEmpty()) return true
    return listOf(name, commonName, genus).any { text ->
        val words = text.normalized()
        words.startsWith(wanted) || words.split(WORD_SEPARATORS).any { it.startsWith(wanted) }
    }
}

private val WORD_SEPARATORS = Regex("[\\s,'’-]+")
private val DIACRITICS = Regex("\\p{Mn}+")

private fun String.normalized() =
    Normalizer.normalize(trim(), Normalizer.Form.NFD).replace(DIACRITICS, "").lowercase(Locale.FRENCH)
