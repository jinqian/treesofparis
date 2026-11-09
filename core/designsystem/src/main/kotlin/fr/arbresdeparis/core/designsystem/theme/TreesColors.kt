package fr.arbresdeparis.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Colours the Material scheme has no slot for: map pins, the remarkable-tree gold and the flowering calendar. */
@Immutable
data class TreesColors(
    /** Gold fill of a remarkable tree's pin and badge icon on the map. */
    val remarkable: Color,
    /** Ordinary tree pin on the map. */
    val treePin: Color,
    /** Ring drawn around every pin so it reads on any map tile. */
    val pinRing: Color,
    /** The user's position dot. */
    val userLocation: Color,
    /** A flowering month of a tree with showy flowers (the guide's « arbre à fleurs »). */
    val bloomShowy: Color,
    /** A flowering month of a tree with discreet flowers: catkins, small green flowers. */
    val bloomDiscreet: Color,
    /** A month without flowers. */
    val bloomNone: Color,
    /** Wash behind the current month in the flowering calendar. */
    val currentMonth: Color,
)

internal val LightTreesColors =
    TreesColors(
        remarkable = HerbierPalette.Gold,
        treePin = HerbierPalette.TreePin,
        pinRing = HerbierPalette.Surface,
        userLocation = HerbierPalette.UserBlue,
        bloomShowy = HerbierPalette.TreePin,
        bloomDiscreet = HerbierPalette.Lichen,
        bloomNone = HerbierPalette.Stone,
        currentMonth = HerbierPalette.GoldWash,
    )

internal val DarkTreesColors =
    TreesColors(
        remarkable = HerbierPalette.NightGold,
        treePin = HerbierPalette.NightTreePin,
        pinRing = HerbierPalette.NightGround,
        userLocation = HerbierPalette.NightUserBlue,
        bloomShowy = HerbierPalette.NightTreePin,
        bloomDiscreet = HerbierPalette.NightLichen,
        bloomNone = HerbierPalette.NightStone,
        currentMonth = HerbierPalette.NightGoldWash,
    )

internal val LocalTreesColors = staticCompositionLocalOf { LightTreesColors }
