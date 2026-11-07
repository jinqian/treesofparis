package com.treesofparis.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Colours the Material scheme has no slot for: map pins and the remarkable-tree gold. */
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
)

internal val LightTreesColors =
    TreesColors(
        remarkable = HerbierPalette.Gold,
        treePin = HerbierPalette.TreePin,
        pinRing = HerbierPalette.Surface,
        userLocation = HerbierPalette.UserBlue,
    )

internal val DarkTreesColors =
    TreesColors(
        remarkable = HerbierPalette.NightGold,
        treePin = HerbierPalette.NightTreePin,
        pinRing = HerbierPalette.NightGround,
        userLocation = HerbierPalette.NightUserBlue,
    )

internal val LocalTreesColors = staticCompositionLocalOf { LightTreesColors }
