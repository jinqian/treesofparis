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
    /** A group of trees on the zoomed-out map: a disc a shade deeper than the pins... */
    val treeGroup: Color,
    /** ...and its count, in paper white (at least 4.5:1 on [treeGroup]). */
    val treeGroupLabel: Color,
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
        treeGroup = HerbierPalette.Moss,
        treeGroupLabel = HerbierPalette.Paper,
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
        // The night pins are a light green: the groups use the day pin green, which reads on the dark map and keeps
        // the white count legible.
        treeGroup = HerbierPalette.TreePin,
        treeGroupLabel = HerbierPalette.Paper,
        bloomShowy = HerbierPalette.NightTreePin,
        bloomDiscreet = HerbierPalette.NightLichen,
        bloomNone = HerbierPalette.NightStone,
        currentMonth = HerbierPalette.NightGoldWash,
    )

internal val LocalTreesColors = staticCompositionLocalOf { LightTreesColors }
