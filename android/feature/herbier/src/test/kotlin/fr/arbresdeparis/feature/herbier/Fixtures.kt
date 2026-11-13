package fr.arbresdeparis.feature.herbier

import fr.arbresdeparis.domain.model.AreaCount
import fr.arbresdeparis.domain.model.FloweringSeason
import fr.arbresdeparis.domain.model.FloweringSheet
import fr.arbresdeparis.domain.model.GenusCount
import fr.arbresdeparis.domain.model.GenusSummary
import fr.arbresdeparis.domain.model.GeoBounds
import fr.arbresdeparis.domain.model.GeoPoint
import fr.arbresdeparis.domain.model.RemarkableStatistics
import fr.arbresdeparis.domain.model.RemarkableStory
import fr.arbresdeparis.domain.model.StageCount
import fr.arbresdeparis.domain.model.Tree
import fr.arbresdeparis.domain.model.TreeCluster
import fr.arbresdeparis.domain.model.TreeStatistics
import fr.arbresdeparis.domain.repository.FloweringRepository
import fr.arbresdeparis.domain.repository.RemarkableStoryRepository
import fr.arbresdeparis.domain.repository.TreeRepository
import fr.arbresdeparis.domain.repository.TreeStatisticsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.Clock
import java.time.Instant
import java.time.Month
import java.time.ZoneId

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

    override suspend fun getGenera() = emptyList<GenusSummary>()
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)

    override fun finished(description: Description) = Dispatchers.resetMain()
}

/** A few real calendars of the guide: hazel and elm open the year, the cherry is showy. */
val SEASONS =
    listOf(
        FloweringSeason("Corylus", months(2..3), showyFlowers = false),
        FloweringSeason("Ulmus", months(2..4), showyFlowers = false),
        FloweringSeason("Prunus", months(3..4), showyFlowers = true),
        FloweringSeason("Platanus", months(5..6), showyFlowers = false),
        FloweringSeason("Aesculus", months(5..6), showyFlowers = true),
        FloweringSeason("Tilia", months(5..6), showyFlowers = false),
    )

fun months(range: IntRange): Set<Month> = range.map(Month::of).toSet()

class FakeFloweringRepository : FloweringRepository {
    override fun sheets() =
        SEASONS.map { FloweringSheet(it.genus, "${it.genus} sp.", it.genus, it.months, it.showyFlowers, "https://x") }
}

/** 8 November 2026 in Paris. */
val NOVEMBER: Clock = Clock.fixed(Instant.parse("2026-11-08T10:00:00Z"), ZoneId.of("Europe/Paris"))

/** Four remarkable trees in three areas. */
val REMARKABLE_TREES =
    listOf(
        tree(11, heightM = 45, address = "ROUTE DU LAC A BAGATELLE, 16-10", area = "BOIS DE BOULOGNE"),
        tree(12, heightM = 30, address = "PARC DES BUTTES CHAUMONT / 7 RUE BOTZARIS, 19-08", area = "PARIS 19E ARRDT"),
        tree(
            13,
            heightM = null,
            address = "JARDIN DU LUXEMBOURG",
            area = "PARIS 6E ARRDT",
        ).copy(commonName = "Marronnier"),
        tree(14, heightM = 22, address = "PARC MONCEAU", area = "PARIS 6E ARRDT").copy(commonName = "Erable"),
    )

class FakeTreeRepository(
    private val trees: List<Tree> = REMARKABLE_TREES,
) : TreeRepository {
    override suspend fun getTreesInBounds(
        bounds: GeoBounds,
        limit: Int,
        remarkableOnly: Boolean,
        genus: String?,
    ) = emptyList<Tree>()

    override suspend fun getTree(id: Long) = trees.firstOrNull { it.id == id }

    override suspend fun getTreesByIds(ids: List<Long>) = trees.filter { it.id in ids }

    override suspend fun getRemarkableTrees() = trees

    override suspend fun getTreeClusters(
        bounds: GeoBounds,
        cellLat: Double,
        cellLon: Double,
    ) = emptyList<TreeCluster>()
}

/** The story of tree 13 (the Marronnier of the Jardin du Luxembourg), as « Les arbres remarquables » tells it. */
val STORIES =
    mapOf(
        13L to
            RemarkableStory(
                treeId = 13,
                site = "Jardin du Luxembourg",
                qualification = "Symbolique",
                summary = "Cet arbre est classé remarquable pour sa valeur symbolique.",
                story =
                    "Le marronnier appartient à la famille des Sapindacées. Cet arbre est issu du semis du " +
                        "véritable marronnier qu’Anne Frank voyait depuis son grenier, à Amsterdam. " +
                        "Il a été planté en 2006.",
                plantingYear = 2006,
                label = null,
                councilDecision = null,
                signUrl = null,
                photoUrl = null,
                photoCredit = null,
            ),
    )

class FakeRemarkableStoryRepository(
    private val stories: Map<Long, RemarkableStory> = STORIES,
) : RemarkableStoryRepository {
    override suspend fun getStory(treeId: Long) = stories[treeId]

    override suspend fun getStories() = stories.values.toList()
}
