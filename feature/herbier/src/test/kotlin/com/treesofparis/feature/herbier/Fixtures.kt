package com.treesofparis.feature.herbier

import com.treesofparis.domain.model.AreaCount
import com.treesofparis.domain.model.GenusCount
import com.treesofparis.domain.model.GeoPoint
import com.treesofparis.domain.model.RemarkableStatistics
import com.treesofparis.domain.model.StageCount
import com.treesofparis.domain.model.Tree
import com.treesofparis.domain.model.TreeStatistics
import com.treesofparis.domain.repository.TreeStatisticsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** Real figures from the dataset, trimmed to a dozen genera. */
val PARIS =
    TreeStatistics(
        total = 220_339,
        genusCount = 202,
        taxonCount = 1_004,
        genera =
            listOf(
                GenusCount("Platanus", 42_591),
                GenusCount("Aesculus", 24_795),
                GenusCount("Tilia", 23_072),
                GenusCount("Acer", 21_157),
                GenusCount("Styphnolobium", 11_307),
                GenusCount("Ginkgo", 9_500),
                GenusCount("Prunus", 9_246),
                GenusCount("Quercus", 6_412),
                GenusCount("Fraxinus", 6_324),
                GenusCount("Celtis", 5_212),
                GenusCount("Pinus", 5_208),
                GenusCount("Pyrus", 4_801),
                GenusCount("Fagus", 1_928),
            ),
        stages =
            listOf(
                StageCount("Adulte", 89_118),
                StageCount("Jeune", 45_781),
                StageCount(null, 36_239),
                StageCount("Jeune adulte", 41_708),
                StageCount("Mature", 7_493),
            ),
        remarkable =
            RemarkableStatistics(
                count = 181,
                leadingGenus = GenusCount("Platanus", 44),
                tallest = tree(1, heightM = 45, address = "ROUTE DU LAC A BAGATELLE, 16-10", area = "BOIS DE BOULOGNE"),
                widest =
                    tree(
                        2,
                        circumferenceCm = 740,
                        address = "PARC DES BUTTES CHAUMONT / 7 RUE BOTZARIS, 19-08",
                        area = "PARIS 19E ARRDT",
                    ),
            ),
        mostWoodedAreas =
            listOf(
                AreaCount("PARIS 15E ARRDT", 18_622),
                AreaCount("PARIS 13E ARRDT", 18_528),
                AreaCount("PARIS 16E ARRDT", 18_045),
            ),
    )

fun tree(
    id: Long,
    heightM: Int? = null,
    circumferenceCm: Int? = null,
    address: String,
    area: String,
) = Tree(
    id = id,
    commonName = "Platane",
    genus = "Platanus",
    species = "x hispanica",
    cultivar = null,
    circumferenceCm = circumferenceCm,
    heightM = heightM,
    stage = "Adulte",
    remarkable = true,
    address = address,
    arrondissement = area,
    location = GeoPoint(48.86, 2.33),
)

class FakeStatisticsRepository(
    private val statistics: TreeStatistics = PARIS,
) : TreeStatisticsRepository {
    override suspend fun getStatistics(areaLimit: Int) = statistics
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)

    override fun finished(description: Description) = Dispatchers.resetMain()
}
