package com.treesofparis.feature.favorites

import com.treesofparis.domain.model.GeoBounds
import com.treesofparis.domain.model.GeoPoint
import com.treesofparis.domain.model.Tree
import com.treesofparis.domain.repository.FavoritesRepository
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
    name: String = "Platane",
    area: String = "PARIS 19E ARRDT",
) = Tree(
    id = id,
    commonName = name,
    genus = "Platanus",
    species = null,
    cultivar = null,
    circumferenceCm = null,
    heightM = null,
    stage = null,
    remarkable = false,
    address = "QUAI DE LA SEINE",
    arrondissement = area,
    location = GeoPoint(48.88, 2.37),
)

class FakeTreeRepository(
    private val trees: List<Tree>,
) : TreeRepository {
    override suspend fun getTreesInBounds(
        bounds: GeoBounds,
        limit: Int,
    ) = emptyList<Tree>()

    override suspend fun getTree(id: Long) = trees.firstOrNull { it.id == id }

    override suspend fun getTreesByIds(ids: List<Long>) = trees.filter { it.id in ids }
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

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)

    override fun finished(description: Description) = Dispatchers.resetMain()
}
