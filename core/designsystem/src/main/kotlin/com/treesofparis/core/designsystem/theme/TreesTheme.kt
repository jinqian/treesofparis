package com.treesofparis.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp

/** Soft, generous corners: chips and buttons are pills, cards 16–20 dp, sheets 28 dp. */
internal val TreesShapes =
    Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(14.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(20.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )

/** App theme (« Herbier »). Dynamic colour is off on purpose: the palette is part of the app's identity. */
@Composable
fun TreesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalTreesColors provides if (darkTheme) DarkTreesColors else LightTreesColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = TreesTypography,
            shapes = TreesShapes,
            content = content,
        )
    }
}

/** Access to the theme values Material has no slot for. */
object TreesTheme {
    val colors: TreesColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTreesColors.current
}
