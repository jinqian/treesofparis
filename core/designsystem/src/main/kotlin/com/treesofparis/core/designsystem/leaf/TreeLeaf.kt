package com.treesofparis.core.designsystem.leaf

import androidx.annotation.StringRes
import com.treesofparis.core.designsystem.R
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
    internal val pathData: String,
) {
    PLATANUS("Platanus", R.string.leaf_platanus, PLATANUS_PATH),
    AESCULUS("Aesculus", R.string.leaf_aesculus, AESCULUS_PATH),
    TILIA("Tilia", R.string.leaf_tilia, TILIA_PATH),
    ACER("Acer", R.string.leaf_acer, ACER_PATH),
    STYPHNOLOBIUM("Styphnolobium", R.string.leaf_styphnolobium, STYPHNOLOBIUM_PATH),
    PRUNUS("Prunus", R.string.leaf_prunus, PRUNUS_PATH),
    QUERCUS("Quercus", R.string.leaf_quercus, QUERCUS_PATH),
    FRAXINUS("Fraxinus", R.string.leaf_fraxinus, FRAXINUS_PATH),
    CELTIS("Celtis", R.string.leaf_celtis, CELTIS_PATH),
    PINUS("Pinus", R.string.leaf_pinus, PINUS_PATH),
    PYRUS("Pyrus", R.string.leaf_pyrus, PYRUS_PATH),
    CARPINUS("Carpinus", R.string.leaf_carpinus, CARPINUS_PATH),
    CORYLUS("Corylus", R.string.leaf_corylus, CORYLUS_PATH),
    MALUS("Malus", R.string.leaf_malus, MALUS_PATH),
    POPULUS("Populus", R.string.leaf_populus, POPULUS_PATH),
    TAXUS("Taxus", R.string.leaf_taxus, TAXUS_PATH),
    ULMUS("Ulmus", R.string.leaf_ulmus, ULMUS_PATH),
    GLEDITSIA("Gleditsia", R.string.leaf_gleditsia, GLEDITSIA_PATH),
    ROBINIA("Robinia", R.string.leaf_robinia, ROBINIA_PATH),
    BETULA("Betula", R.string.leaf_betula, BETULA_PATH),
    FAGUS("Fagus", R.string.leaf_fagus, FAGUS_PATH),
    GENERIC(null, R.string.leaf_generic, GENERIC_PATH),
    ;

    companion object {
        private val byGenus = entries.filter { it.genus != null }.associateBy { it.genus!!.lowercase(Locale.ROOT) }

        /** The drawing for a dataset genus; [GENERIC] when the genus is unknown or not drawn. */
        fun forGenus(genus: String?): TreeLeaf = byGenus[genus?.trim()?.lowercase(Locale.ROOT)] ?: GENERIC
    }
}
