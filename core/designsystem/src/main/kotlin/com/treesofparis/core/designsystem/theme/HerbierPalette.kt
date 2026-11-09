package com.treesofparis.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * The « Herbier » palette: calm paper, forest ink, moss green, and a single touch of gold reserved for remarkable
 * trees. Raw values; screens use [androidx.compose.material3.MaterialTheme.colorScheme] or [TreesTheme.colors].
 */
object HerbierPalette {
    // Light
    val Paper = Color(0xFFF4F3EC)
    val Surface = Color(0xFFFBFAF6)
    val Ink = Color(0xFF1E2620)
    val InkMuted = Color(0xFF5C665D)
    val Moss = Color(0xFF3F5F45)
    val MossDeep = Color(0xFF2C4631)
    val Sage = Color(0xFFE1E8DA)
    val Stone = Color(0xFFECEBE2)
    val Hairline = Color(0xFFE2E1D8)
    val Outline = Color(0xFF8C9488)
    val Gold = Color(0xFFC9962E)
    val GoldInk = Color(0xFF8A5E0F)
    val GoldWash = Color(0xFFF3E7CC)
    val GoldDeep = Color(0xFF5E3F07)
    val TreePin = Color(0xFF4F7556)
    val Lichen = Color(0xFFA9BBA5)
    val UserBlue = Color(0xFF4A7FA8)
    val Error = Color(0xFFB3261E)

    // Dark
    val NightGround = Color(0xFF141815)
    val NightSurface = Color(0xFF1C211D)
    val NightRaised = Color(0xFF262C27)
    val NightInk = Color(0xFFE6E8E1)
    val NightInkMuted = Color(0xFFA3AB9F)
    val NightMoss = Color(0xFFA8C3A0)
    val NightMossInk = Color(0xFF1C2B1F)
    val NightSage = Color(0xFF2E3D30)
    val NightSageInk = Color(0xFFC9DCC2)
    val NightStone = Color(0xFF2A302B)
    val NightHairline = Color(0xFF2E352F)
    val NightOutline = Color(0xFF6E766D)
    val NightGold = Color(0xFFD4A443)
    val NightGoldInk = Color(0xFFE0B65A)
    val NightGoldWash = Color(0xFF4A3810)
    val NightGoldPale = Color(0xFFF3E0B0)
    val NightGoldDeep = Color(0xFF3A2800)
    val NightTreePin = Color(0xFF7FA286)
    val NightLichen = Color(0xFF4C5F4F)
    val NightUserBlue = Color(0xFF7FB0D6)
    val NightError = Color(0xFFF2B8B5)
}

internal val LightColors =
    with(HerbierPalette) {
        lightColorScheme(
            primary = Moss,
            onPrimary = Surface,
            primaryContainer = Sage,
            onPrimaryContainer = MossDeep,
            secondary = InkMuted,
            onSecondary = Surface,
            secondaryContainer = Stone,
            onSecondaryContainer = Ink,
            tertiary = GoldInk,
            onTertiary = Surface,
            tertiaryContainer = GoldWash,
            onTertiaryContainer = GoldDeep,
            error = Error,
            background = Paper,
            onBackground = Ink,
            surface = Paper,
            onSurface = Ink,
            surfaceVariant = Stone,
            onSurfaceVariant = InkMuted,
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Surface,
            surfaceContainer = Surface,
            surfaceContainerHigh = Surface,
            surfaceContainerHighest = Stone,
            outline = Outline,
            outlineVariant = Hairline,
        )
    }

internal val DarkColors =
    with(HerbierPalette) {
        darkColorScheme(
            primary = NightMoss,
            onPrimary = NightMossInk,
            primaryContainer = NightSage,
            onPrimaryContainer = NightSageInk,
            secondary = NightInkMuted,
            onSecondary = NightGround,
            secondaryContainer = NightStone,
            onSecondaryContainer = NightInk,
            tertiary = NightGoldInk,
            onTertiary = NightGoldDeep,
            tertiaryContainer = NightGoldWash,
            onTertiaryContainer = NightGoldPale,
            error = NightError,
            background = NightGround,
            onBackground = NightInk,
            surface = NightGround,
            onSurface = NightInk,
            surfaceVariant = NightStone,
            onSurfaceVariant = NightInkMuted,
            surfaceContainerLowest = NightGround,
            surfaceContainerLow = NightSurface,
            surfaceContainer = NightSurface,
            surfaceContainerHigh = NightSurface,
            surfaceContainerHighest = NightRaised,
            outline = NightOutline,
            outlineVariant = NightHairline,
        )
    }
