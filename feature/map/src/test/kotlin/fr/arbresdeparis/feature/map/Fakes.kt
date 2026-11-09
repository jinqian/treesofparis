package fr.arbresdeparis.feature.map

import fr.arbresdeparis.domain.model.Favorite
import fr.arbresdeparis.domain.model.FavoriteSnapshot
import fr.arbresdeparis.domain.model.FloweringSheet
import fr.arbresdeparis.domain.model.GenusSummary
import fr.arbresdeparis.domain.model.GeoBounds
import fr.arbresdeparis.domain.model.GeoPoint
import fr.arbresdeparis.domain.model.Tree
import fr.arbresdeparis.domain.model.TreeStatistics
import fr.arbresdeparis.domain.repository.FavoritesRepository
import fr.arbresdeparis.domain.repository.FloweringRepository
import fr.arbresdeparis.domain.repository.LocationRepository
import fr.arbresdeparis.domain.repository.TreeRepository
import fr.arbresdeparis.domain.repository.TreeStatisticsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
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

fun tree(
    id: Long,
    lat: Double = 48.8566,
    lon: Double = 2.3522,
    species: String? = "x hispanica",
) = Tree(
    id = id,
    commonName = "Platane",
    genus = "Platanus",
    species = species,
    cultivar = null,
    circumferenceCm = 165,
    heightM = 16,
    stage = "Adulte",
    remarkable = false,
    address = "QUAI DE LA SEINE",
    arrondissement = "PARIS 19E ARRDT",
    location = GeoPoint(lat, lon),
)

class FakeTreeRepository(
    private val trees: List<Tree>,
) : TreeRepository {
    val boundsCalls = mutableListOf<Pair<GeoBounds, Int>>()
    val remarkableOnlyCalls = mutableListOf<Boolean>()
    val genusCalls = mutableListOf<String?>()

    override suspend fun getTreesInBounds(
        bounds: GeoBounds,
        limit: Int,
        remarkableOnly: Boolean,
        genus: String?,
    ): List<Tree> {
        boundsCalls += bounds to limit
        remarkableOnlyCalls += remarkableOnly
        genusCalls += genus
        return trees
            .filter {
                bounds.contains(it.location) && (!remarkableOnly || it.remarkable) &&
                    (genus == null || it.genus == genus)
            }
            // Like the DAO: when there are more than [limit], the ones closest to the box centre win.
            .sortedBy { it.location.distanceTo(bounds.center) }
            .take(limit)
    }

    override suspend fun getTree(id: Long) = trees.firstOrNull { it.id == id }

    override suspend fun getTreesByIds(ids: List<Long>) = trees.filter { it.id in ids }

    override suspend fun getRemarkableTrees() = trees.filter { it.remarkable }
}

class FakeFavoritesRepository(
    initial: List<Long> = emptyList(),
) : FavoritesRepository {
    val ids = MutableStateFlow(initial)

    override fun observeFavorites(): Flow<List<Favorite>> = ids.map { ids -> ids.map { Favorite(it, null) } }

    override fun observeIsFavorite(id: Long): Flow<Boolean> = ids.map { id in it }

    override suspend fun toggle(
        id: Long,
        snapshot: FavoriteSnapshot?,
    ) {
        ids.value = if (id in ids.value) ids.value - id else listOf(id) + ids.value
    }

    override suspend fun updateSnapshots(snapshots: Map<Long, FavoriteSnapshot>) = Unit
}

class FakeLocationRepository(
    var location: GeoPoint?,
    var delayMs: Long = 0,
) : LocationRepository {
    override suspend fun currentLocation(): GeoPoint? {
        if (delayMs > 0) kotlinx.coroutines.delay(delayMs)
        return location
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)

    override fun finished(description: Description) = Dispatchers.resetMain()
}

/** The guide's lime and plane calendars, enough for the sheet. */
class FakeFloweringRepository : FloweringRepository {
    override fun sheets() =
        listOf(
            sheet("Tilia", setOf(Month.MAY, Month.JUNE)),
            sheet("Prunus", setOf(Month.MARCH, Month.APRIL), showy = true),
        )

    private fun sheet(
        genus: String,
        months: Set<Month>,
        showy: Boolean = false,
    ) = FloweringSheet(genus, "$genus sp.", genus, months, showy, "https://example.org/$genus.pdf")
}

/** 8 November 2026 in Paris. */
val NOVEMBER: Clock = Clock.fixed(Instant.parse("2026-11-08T10:00:00Z"), ZoneId.of("Europe/Paris"))

/** Genera as the data layer sums them: plane trees (one remarkable), limes, and Ginkgo, which has no drawing. */
class FakeStatisticsRepository : TreeStatisticsRepository {
    override suspend fun getStatistics(areaLimit: Int): TreeStatistics = error("not used by the map")

    override suspend fun getGenera() =
        listOf(
            GenusSummary("Platanus", "Platane", treeCount = 3_001, remarkableCount = 1),
            GenusSummary("Tilia", "Tilleul", treeCount = 1, remarkableCount = 0),
            GenusSummary("Ginkgo", "Arbre aux quarante écus", treeCount = 1_154, remarkableCount = 0),
        )
}
