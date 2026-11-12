package fr.arbresdeparis.core.designsystem.leaf

import androidx.annotation.StringRes
import fr.arbresdeparis.core.designsystem.R
import java.util.Locale

/**
 * A fine-line leaf drawing for a tree genus (the dataset's `genus`), drawn after the genus's most planted species in
 * Paris. The 21 drawn genera cover about 85 % of the trees; every other genus gets [GENERIC].
 */
enum class TreeLeaf(
    /** Latin genus as written in the dataset, or null for [GENERIC]. */
    val genus: String?,
    /** French common name for the genus. */
    @StringRes val label: Int,
    /** Lower-case plural, for sentences: « 42 591 platanes ». */
    @StringRes val pluralLabel: Int,
    internal val pathData: String,
) {
    PLATANUS("Platanus", R.string.leaf_platanus, R.string.leaf_platanus_plural, PLATANUS_PATH),
    AESCULUS("Aesculus", R.string.leaf_aesculus, R.string.leaf_aesculus_plural, AESCULUS_PATH),
    TILIA("Tilia", R.string.leaf_tilia, R.string.leaf_tilia_plural, TILIA_PATH),
    ACER("Acer", R.string.leaf_acer, R.string.leaf_acer_plural, ACER_PATH),
    STYPHNOLOBIUM("Styphnolobium", R.string.leaf_styphnolobium, R.string.leaf_styphnolobium_plural, STYPHNOLOBIUM_PATH),
    PRUNUS("Prunus", R.string.leaf_prunus, R.string.leaf_prunus_plural, PRUNUS_PATH),
    QUERCUS("Quercus", R.string.leaf_quercus, R.string.leaf_quercus_plural, QUERCUS_PATH),
    FRAXINUS("Fraxinus", R.string.leaf_fraxinus, R.string.leaf_fraxinus_plural, FRAXINUS_PATH),
    CELTIS("Celtis", R.string.leaf_celtis, R.string.leaf_celtis_plural, CELTIS_PATH),
    PINUS("Pinus", R.string.leaf_pinus, R.string.leaf_pinus_plural, PINUS_PATH),
    PYRUS("Pyrus", R.string.leaf_pyrus, R.string.leaf_pyrus_plural, PYRUS_PATH),
    CARPINUS("Carpinus", R.string.leaf_carpinus, R.string.leaf_carpinus_plural, CARPINUS_PATH),
    CORYLUS("Corylus", R.string.leaf_corylus, R.string.leaf_corylus_plural, CORYLUS_PATH),
    MALUS("Malus", R.string.leaf_malus, R.string.leaf_malus_plural, MALUS_PATH),
    POPULUS("Populus", R.string.leaf_populus, R.string.leaf_populus_plural, POPULUS_PATH),
    TAXUS("Taxus", R.string.leaf_taxus, R.string.leaf_taxus_plural, TAXUS_PATH),
    ULMUS("Ulmus", R.string.leaf_ulmus, R.string.leaf_ulmus_plural, ULMUS_PATH),
    GLEDITSIA("Gleditsia", R.string.leaf_gleditsia, R.string.leaf_gleditsia_plural, GLEDITSIA_PATH),
    ROBINIA("Robinia", R.string.leaf_robinia, R.string.leaf_robinia_plural, ROBINIA_PATH),
    BETULA("Betula", R.string.leaf_betula, R.string.leaf_betula_plural, BETULA_PATH),
    FAGUS("Fagus", R.string.leaf_fagus, R.string.leaf_fagus_plural, FAGUS_PATH),
    GENERIC(null, R.string.leaf_generic, R.string.leaf_generic_plural, GENERIC_PATH),
    ;

    companion object {
        private val byGenus = entries.filter { it.genus != null }.associateBy { it.genus!!.lowercase(Locale.ROOT) }

        /** The drawing for a dataset genus; [GENERIC] when the genus is unknown or not drawn. */
        fun forGenus(genus: String?): TreeLeaf = byGenus[genus?.trim()?.lowercase(Locale.ROOT)] ?: GENERIC
    }
}
