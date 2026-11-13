package fr.arbresdeparis.core.designsystem.theme

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

    // Dark (« Crépuscule », 2026-11-13): a warm olive night, one step lighter than the first near-black one
    val NightGround = Color(0xFF26241F)
    val NightSurface = Color(0xFF2F2C27)
    val NightRaised = Color(0xFF3A3630)
    val NightInk = Color(0xFFEFEBE0)
    val NightInkMuted = Color(0xFFB8B1A2)
    val NightMoss = Color(0xFFB6CB9E)
    val NightMossInk = Color(0xFF222C17)
    val NightSage = Color(0xFF3A4430)
    val NightSageInk = Color(0xFFD7E4C6)
    val NightStone = Color(0xFF34312B)
    val NightHairline = Color(0xFF433F37)
    val NightOutline = Color(0xFF837C6F)
    val NightGold = Color(0xFFE0B252)
    val NightGoldInk = Color(0xFFEAC26D)
    val NightGoldWash = Color(0xFF4D3A12)
    val NightGoldPale = Color(0xFFF5E3B5)
    val NightGoldDeep = Color(0xFF3A2800)
    val NightTreePin = Color(0xFF93AE84)
    val NightLichen = Color(0xFF565E45)
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
