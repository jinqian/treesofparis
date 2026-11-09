package fr.arbresdeparis.feature.favorites

import fr.arbresdeparis.domain.model.Favorite
import fr.arbresdeparis.domain.model.FavoriteSnapshot
import fr.arbresdeparis.domain.model.GeoBounds
import fr.arbresdeparis.domain.model.GeoPoint
import fr.arbresdeparis.domain.model.Tree
import fr.arbresdeparis.domain.repository.FavoritesRepository
import fr.arbresdeparis.domain.repository.TreeRepository
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
        remarkableOnly: Boolean,
        genus: String?,
    ) = emptyList<Tree>()

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

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)

    override fun finished(description: Description) = Dispatchers.resetMain()
}
