package com.treesofparis.feature.map

import com.treesofparis.domain.model.GeoBounds
import com.treesofparis.domain.model.GeoPoint
import com.treesofparis.domain.model.Tree
import com.treesofparis.domain.repository.FavoritesRepository
import com.treesofparis.domain.repository.LocationRepository
import com.treesofparis.domain.repository.TreeRepository
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
            }.take(limit)
    }

    override suspend fun getTree(id: Long) = trees.firstOrNull { it.id == id }

    override suspend fun getTreesByIds(ids: List<Long>) = trees.filter { it.id in ids }
}

class FakeFavoritesRepository : FavoritesRepository {
    val ids = MutableStateFlow(emptyList<Long>())

    override fun observeFavoriteIds(): Flow<List<Long>> = ids

    override fun observeIsFavorite(id: Long): Flow<Boolean> = ids.map { id in it }

    override suspend fun toggle(id: Long) {
        ids.value = if (id in ids.value) ids.value - id else listOf(id) + ids.value
    }
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
