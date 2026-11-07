package com.treesofparis.core.designsystem.icon

import androidx.compose.ui.graphics.vector.ImageVector

/** Botanical line drawings, tinted by the caller (`Icon`/`Image` with a colour filter). */
object TreesIllustrations {
    /** A sprig with four leaves on a ground line: empty states. */
    val Sprig: ImageVector =
        lineDrawing(
            "Sprig",
            width = 140f,
            height = 160f,
            strokeWidth = 1.4f,
            "M70 150C70 120 68 90 72 40",
            "M71 118C52 112 36 98 30 78C50 80 64 94 71 118Z",
            "M71 118C50 104 40 92 30 78",
            "M70 96C90 90 104 74 110 54C90 58 76 72 70 96Z",
            "M70 96C88 82 100 68 110 54",
            "M71 70C56 64 46 50 44 32C60 36 70 50 71 70Z",
            "M71 70C58 56 50 44 44 32",
            "M72 40C76 30 82 22 92 16C92 28 84 36 72 40Z",
            "M50 150L90 150",
        )

    /** A plane-tree leaf with its veins: a faint watermark on the tree sheet. */
    val PlaneLeaf: ImageVector =
        lineDrawing(
            "PlaneLeaf",
            width = 100f,
            height = 100f,
            strokeWidth = 1.2f,
            "M50 92L50 52",
            "M50 52C40 50 26 54 14 46C22 44 26 36 22 26C32 30 38 26 40 16C44 22 48 22 50 10" +
                "C52 22 56 22 60 16C62 26 68 30 78 26C74 36 78 44 86 46C74 54 60 50 50 52Z",
            "M50 52L30 30M50 52L50 18M50 52L70 30M50 52L22 44M50 52L78 44",
        )
}
