package com.treesofparis.domain.fake

import com.treesofparis.domain.model.GeoBounds
import com.treesofparis.domain.model.GeoPoint
import com.treesofparis.domain.model.Tree
import com.treesofparis.domain.repository.FavoritesRepository
import com.treesofparis.domain.repository.LocationRepository
import com.treesofparis.domain.repository.TreeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

fun tree(
    id: Long,
    lat: Double = 48.85,
    lon: Double = 2.35,
) = Tree(
    id = id,
    commonName = "Platane",
    genus = "Platanus",
    species = null,
    cultivar = null,
    circumferenceCm = null,
    heightM = null,
    stage = null,
    remarkable = false,
    address = "Quai de la Tournelle",
    arrondissement = "PARIS 5E ARRDT",
    location = GeoPoint(lat, lon),
)

class FakeTreeRepository(
    trees: List<Tree> = emptyList(),
) : TreeRepository {
    private val byId = trees.associateBy { it.id }

    override suspend fun getTreesInBounds(
        bounds: GeoBounds,
        limit: Int,
        remarkableOnly: Boolean,
        genus: String?,
    ) = byId.values
        .filter {
            bounds.contains(it.location) && (!remarkableOnly || it.remarkable) &&
                (genus == null || it.genus == genus)
        }.take(limit)

    override suspend fun getTree(id: Long) = byId[id]

    override suspend fun getTreesByIds(ids: List<Long>) = ids.mapNotNull { byId[it] }.shuffled()
}

class FakeFavoritesRepository(
    initial: List<Long> = emptyList(),
) : FavoritesRepository {
    val ids = MutableStateFlow(initial)

    override fun observeFavoriteIds(): Flow<List<Long>> = ids

    override fun observeIsFavorite(id: Long): Flow<Boolean> = ids.map { id in it }

    override suspend fun toggle(id: Long) {
        ids.value = if (id in ids.value) ids.value - id else listOf(id) + ids.value
    }
}

class FakeLocationRepository(
    var location: GeoPoint?,
) : LocationRepository {
    override suspend fun currentLocation() = location
}
