package com.treesofparis.domain.model

import java.time.Month

/**
 * One species sheet of the « Guide des Essences de Paris » (Ville de Paris, Cerema, 2023, ODbL), the source of the
 * flowering calendar. [months] are the highlighted cells of its « Floraison » row; [showyFlowers] is the guide's
 * « arbre à fleurs » classification; [url] is the sheet's PDF.
 */
data class FloweringSheet(
    val genus: String,
    val scientificName: String,
    val commonName: String,
    val months: Set<Month>,
    val showyFlowers: Boolean,
    val url: String,
)
