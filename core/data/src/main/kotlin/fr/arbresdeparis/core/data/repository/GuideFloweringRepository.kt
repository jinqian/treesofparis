package fr.arbresdeparis.core.data.repository

import fr.arbresdeparis.domain.model.FloweringSheet
import fr.arbresdeparis.domain.repository.FloweringRepository
import java.time.Month
import javax.inject.Inject

/** Where the guide's sheets are published: the « fiches-essences-du-guide-des-essences-de-paris » dataset. */
internal const val GUIDE_FILES_URL =
    "https://opendata.paris.fr/api/explore/v2.1/catalog/datasets/fiches-essences-du-guide-des-essences-de-paris/files/"

/**
 * The flowering calendars of the « Guide des Essences de Paris » (Ville de Paris, Cerema, 2023, ODbL), read from
 * the « Floraison » row of each species sheet: the most planted species of each genus drawn in the Herbier.
 * Checked by hand against the PDFs in November 2026.
 */
internal class GuideFloweringRepository
    @Inject
    constructor() : FloweringRepository {
        override fun sheets(): List<FloweringSheet> = SHEETS

        private companion object {
            @Suppress("LongMethod", "MagicNumber") // A table of reference data, one call per sheet.
            val SHEETS =
                listOf(
                    sheet("Acer", "Acer campestre", "Érable champêtre", 5..5, "69ff6f300269ac71c06eaad17ca3a7f9"),
                    sheet("Acer", "Acer platanoides", "Érable plane", 5..5, "89d241ac1c8307272bb70852187ffb22"),
                    sheet("Acer", "Acer pseudoplatanus", "Érable sycomore", 5..5, "6e0c59ed71ae1e461dd1a033c2c20004"),
                    sheet(
                        "Aesculus",
                        "Aesculus hippocastanum",
                        "Marronnier d’Inde",
                        5..6,
                        "a67c6ac8695f67e3c045dae38bdd0795",
                        showy = true,
                    ),
                    sheet("Betula", "Betula pendula", "Bouleau pendant", 4..5, "31b2bf1b13a7c805a3b2826b9b2cf104"),
                    sheet("Carpinus", "Carpinus betulus", "Charme commun", 4..5, "d605811d4f99ee50a557bd59aa090c25"),
                    sheet(
                        "Celtis",
                        "Celtis australis",
                        "Micocoulier de Provence",
                        4..5,
                        "0c7fd9f8ec4f6425f49e83a383de83f7",
                    ),
                    sheet(
                        "Corylus",
                        "Corylus colurna",
                        "Noisetier de Byzance",
                        2..3,
                        "3859b73411e96791557e3a292d08707e",
                    ),
                    sheet("Fagus", "Fagus sylvatica", "Hêtre commun", 5..5, "e53c4c60803721c0e8bdcaf80c00b611"),
                    sheet("Fraxinus", "Fraxinus excelsior", "Frêne commun", 5..5, "28ef916546d17f5c17734ffa060aed5e"),
                    sheet(
                        "Gleditsia",
                        "Gleditsia triacanthos",
                        "Févier d’Amérique",
                        6..7,
                        "d1536f28e9d0322e6b12fda17cd8f441",
                        showy = true,
                    ),
                    sheet(
                        "Malus",
                        "Malus sylvestris",
                        "Pommier des bois",
                        5..5,
                        "5efb4eb552817eea613a3317c9624655",
                        showy = true,
                    ),
                    sheet(
                        "Pinus",
                        "Pinus nigra subsp. nigra",
                        "Pin noir d’Autriche",
                        5..6,
                        "801682da612ab129e4cae0ae2a76ccdf",
                    ),
                    sheet(
                        "Platanus",
                        "Platanus × hispanica",
                        "Platane commun",
                        5..6,
                        "150d4fe3baf7cf4ee9a55073bbf4a826",
                    ),
                    sheet(
                        "Populus",
                        "Populus nigra ‘Italica’",
                        "Peuplier noir d’Italie",
                        3..4,
                        "ce5ea15707028fcaf5ddffb7cae36f8f",
                    ),
                    sheet(
                        "Populus",
                        "Populus × canadensis",
                        "Peuplier hybride euraméricain",
                        4..4,
                        "6dd6e3ba03b546b3f8bcdc8c27bf0bce",
                    ),
                    sheet("Prunus", "Prunus avium", "Merisier", 3..4, "9d3e075675c7be24c918f2a3045783b3", showy = true),
                    sheet(
                        "Pyrus",
                        "Pyrus calleryana",
                        "Poirier d’ornement",
                        3..4,
                        "25a9a184b045fe5f48960a0a703bd595",
                        showy = true,
                    ),
                    sheet("Quercus", "Quercus ilex", "Chêne vert", 4..5, "09a13db60969fafa746098056a71ff31"),
                    sheet("Quercus", "Quercus robur", "Chêne pédonculé", 4..5, "1304ed311377a1531c4cb4ced0b416dc"),
                    sheet(
                        "Robinia",
                        "Robinia pseudoacacia",
                        "Robinier faux-acacia",
                        6..6,
                        "1a967cc0d889a1f1d436228a6637dd6c",
                        showy = true,
                    ),
                    sheet(
                        "Styphnolobium",
                        "Styphnolobium japonicum",
                        "Sophora du Japon",
                        7..7,
                        "abfcf4410522bed8333dc10d6b7e6978",
                        showy = true,
                    ),
                    sheet("Taxus", "Taxus baccata", "If commun", 3..4, "12e3ee1aa11e584cc798a25df93751d0"),
                    sheet(
                        "Tilia",
                        "Tilia cordata",
                        "Tilleul à petites feuilles",
                        5..6,
                        "f20101f94276b28ce10ca06875e81a66",
                    ),
                    sheet(
                        "Tilia",
                        "Tilia platyphyllos",
                        "Tilleul à grandes feuilles",
                        6..6,
                        "4a9f73ce0cdb41028c655ca9167df563",
                    ),
                    sheet("Tilia", "Tilia tomentosa", "Tilleul argenté", 6..6, "d5cead01f863e12168caa54c788dcc41"),
                    sheet("Ulmus", "Ulmus minor", "Orme champêtre", 2..4, "13ac1f0074af0bd8591e81315fbad080"),
                )

            @Suppress("LongParameterList")
            fun sheet(
                genus: String,
                scientificName: String,
                commonName: String,
                months: IntRange,
                fileId: String,
                showy: Boolean = false,
            ) = FloweringSheet(
                genus = genus,
                scientificName = scientificName,
                commonName = commonName,
                months = months.map(Month::of).toSet(),
                showyFlowers = showy,
                url = GUIDE_FILES_URL + fileId,
            )
        }
    }
