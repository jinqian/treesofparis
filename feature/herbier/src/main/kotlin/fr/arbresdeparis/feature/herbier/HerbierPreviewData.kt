package fr.arbresdeparis.feature.herbier

import fr.arbresdeparis.core.designsystem.leaf.TreeLeaf
import fr.arbresdeparis.domain.format.formatMonth
import java.time.Month

/** Sample figures for the Herbier previews. */
@Suppress("MagicNumber")
internal fun previewContent(month: Month): HerbierUiState.Content {
    val herbier =
        HerbierUi(
            totalTrees = "220 339",
            genusCount = "202",
            taxonCount = "1 004",
            headline =
                HeadlineUi(
                    TreeLeaf.PLATANUS,
                    5,
                    "42 591",
                    GenusCountUi(TreeLeaf.AESCULUS, "24 795"),
                ),
            genera =
                listOf(
                    GenusUi(TreeLeaf.PLATANUS, "Platanus", "19,3 %", "42 591", 1f),
                    GenusUi(TreeLeaf.AESCULUS, "Aesculus", "11,3 %", "24 795", 0.58f),
                ),
            drawnShare = "85 %",
            stages = listOf(StageUi("Jeune", "45 781", 0.51f), StageUi("Adulte", "89 118", 1f)),
            unknownStageCount = "36 239",
            remarkable =
                RemarkableUi(
                    count = "181",
                    leading = LeadingGenusUi(TreeLeaf.PLATANUS, 44),
                    tallest = RecordUi("Platane", "45 m", "Bois de Boulogne · Route du Lac à Bagatelle"),
                    widest = RecordUi("Platane", "7,40 m", "Paris 19e · Parc des Buttes Chaumont"),
                ),
            areas = listOf(AreaUi(1, "Paris 15e", "18 622")),
        )
    val bloom = { leaf: TreeLeaf, from: Int, to: Int, showy: Boolean ->
        FloweringRowUi(leaf, List(12) { it + 1 in from..to }, showy, "")
    }
    val rows =
        listOf(
            bloom(TreeLeaf.CORYLUS, 2, 3, false),
            bloom(TreeLeaf.PRUNUS, 3, 4, true),
            bloom(TreeLeaf.PLATANUS, 5, 6, false),
            bloom(TreeLeaf.AESCULUS, 5, 6, true),
            bloom(TreeLeaf.STYPHNOLOBIUM, 7, 7, true),
        )
    val inFlower = rows.filter { it.months[month.ordinal] }.map { it.leaf }
    val flowering =
        FloweringCalendarUi(
            currentMonth = month.value,
            currentMonthName = formatMonth(month),
            rows = rows,
            inFlower = inFlower,
            next = NextFloweringUi("février", listOf(TreeLeaf.CORYLUS)).takeIf { inFlower.isEmpty() },
        )
    return HerbierUiState.Content(herbier, flowering)
}
