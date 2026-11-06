package com.treesofparis.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.ui.graphics.vector.ImageVector

/** Every icon the app uses. Features never import an icon library directly, so it can be swapped here. */
object TreesIcons {
    val Map: ImageVector = Icons.Filled.Place
    val MapOutlined: ImageVector = Icons.Outlined.Place
    val Favorite: ImageVector = Icons.Filled.Favorite
    val FavoriteOutlined: ImageVector = Icons.Outlined.FavoriteBorder
    val About: ImageVector = Icons.Filled.Info
    val AboutOutlined: ImageVector = Icons.Outlined.Info
    val MyLocation: ImageVector = Icons.Filled.LocationOn
    val Remarkable: ImageVector = Icons.Filled.Star
    val Error: ImageVector = Icons.Filled.Warning
}
