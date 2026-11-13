package fr.arbresdeparis.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import fr.arbresdeparis.R
import fr.arbresdeparis.core.designsystem.icon.TreesIcons
import fr.arbresdeparis.feature.herbier.HerbierSection
import kotlinx.serialization.Serializable

/**
 * The map. [treeId] opens a tree (coming from Favoris); [request] makes every "open" a new entry with its own
 * ViewModel, so opening the same tree twice still recentres the map.
 */
@Serializable
data class MapKey(
    val treeId: Long? = null,
    val genus: String? = null,
    val request: Long = 0,
) : NavKey

/** « Son histoire » of a remarkable tree, pushed on the Carte back stack over the map (issue #74). */
@Serializable
data class RemarkableStoryKey(
    val treeId: Long,
) : NavKey

@Serializable
data object HerbierKey : NavKey

/** A level-2 Herbier screen, pushed on the Herbier back stack. */
@Serializable
data class HerbierSectionKey(
    val section: HerbierSection,
) : NavKey

@Serializable
data object FavoritesKey : NavKey

@Serializable
data object AboutKey : NavKey

/** The open source libraries, pushed on the À propos back stack. */
@Serializable
data object LibrariesKey : NavKey

/** The flowering sources (the guide's species sheets), pushed on the À propos back stack. */
@Serializable
data object FloweringSourcesKey : NavKey

/** The bottom bar tabs. Each one has its own back stack, starting at [startKey]. */
enum class TopLevelDestination(
    @StringRes val label: Int,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
    val startKey: NavKey,
) {
    MAP(R.string.tab_map, TreesIcons.Map, TreesIcons.MapOutlined, MapKey()),
    HERBIER(R.string.tab_herbier, TreesIcons.Leaf, TreesIcons.Leaf, HerbierKey),
    FAVORITES(R.string.tab_favorites, TreesIcons.Favorite, TreesIcons.FavoriteOutlined, FavoritesKey),
    ABOUT(R.string.tab_about, TreesIcons.About, TreesIcons.AboutOutlined, AboutKey),
}
