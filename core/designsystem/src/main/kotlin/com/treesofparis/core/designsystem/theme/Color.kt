package com.treesofparis.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Palette: Paris street trees — leaf greens, bark browns, a warm chestnut accent.
private val Leaf10 = Color(0xFF002110)
private val Leaf20 = Color(0xFF00391E)
private val Leaf30 = Color(0xFF16522F)
private val Leaf40 = Color(0xFF2E6B44)
private val Leaf80 = Color(0xFF95D5A6)
private val Leaf90 = Color(0xFFB1F1C1)
private val Bark20 = Color(0xFF3B2F1E)
private val Bark30 = Color(0xFF534532)
private val Bark40 = Color(0xFF6C5C48)
private val Bark80 = Color(0xFFD9C4AA)
private val Bark90 = Color(0xFFF6E0C5)
private val Chestnut30 = Color(0xFF6F2B10)
private val Chestnut40 = Color(0xFF8F4224)
private val Chestnut80 = Color(0xFFFFB599)
private val Chestnut90 = Color(0xFFFFDBCE)
private val Neutral10 = Color(0xFF191C19)
private val Neutral90 = Color(0xFFE1E3DE)
private val Neutral95 = Color(0xFFF0F1EC)
private val Neutral99 = Color(0xFFF8FAF4)
private val NeutralVariant30 = Color(0xFF414941)
private val NeutralVariant50 = Color(0xFF717970)
private val NeutralVariant80 = Color(0xFFC0C9BF)
private val NeutralVariant90 = Color(0xFFDCE5DA)
private val Error40 = Color(0xFFBA1A1A)
private val Error80 = Color(0xFFFFB4AB)

internal val LightColors =
    lightColorScheme(
        primary = Leaf40,
        onPrimary = Color.White,
        primaryContainer = Leaf90,
        onPrimaryContainer = Leaf10,
        secondary = Bark40,
        onSecondary = Color.White,
        secondaryContainer = Bark90,
        onSecondaryContainer = Bark20,
        tertiary = Chestnut40,
        onTertiary = Color.White,
        tertiaryContainer = Chestnut90,
        onTertiaryContainer = Chestnut30,
        error = Error40,
        background = Neutral99,
        onBackground = Neutral10,
        surface = Neutral99,
        onSurface = Neutral10,
        surfaceVariant = NeutralVariant90,
        onSurfaceVariant = NeutralVariant30,
        surfaceContainer = Neutral95,
        outline = NeutralVariant50,
    )

internal val DarkColors =
    darkColorScheme(
        primary = Leaf80,
        onPrimary = Leaf20,
        primaryContainer = Leaf30,
        onPrimaryContainer = Leaf90,
        secondary = Bark80,
        onSecondary = Bark20,
        secondaryContainer = Bark30,
        onSecondaryContainer = Bark90,
        tertiary = Chestnut80,
        onTertiary = Chestnut30,
        tertiaryContainer = Chestnut40,
        onTertiaryContainer = Chestnut90,
        error = Error80,
        background = Neutral10,
        onBackground = Neutral90,
        surface = Neutral10,
        onSurface = Neutral90,
        surfaceVariant = NeutralVariant30,
        onSurfaceVariant = NeutralVariant80,
        outline = NeutralVariant50,
    )
