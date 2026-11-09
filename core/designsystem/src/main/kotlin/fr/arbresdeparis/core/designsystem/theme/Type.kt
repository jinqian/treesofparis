package fr.arbresdeparis.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import fr.arbresdeparis.core.designsystem.R

private fun variable(
    resId: Int,
    weight: FontWeight,
    style: FontStyle = FontStyle.Normal,
) = Font(
    resId = resId,
    weight = weight,
    style = style,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

/** Newsreader (SIL OFL): tree names, screen titles and figures. */
val Newsreader =
    FontFamily(
        variable(R.font.newsreader, FontWeight.Normal),
        variable(R.font.newsreader, FontWeight.Medium),
        variable(R.font.newsreader_italic, FontWeight.Normal, FontStyle.Italic),
    )

/** Inter (SIL OFL): everything else in the interface. */
val Inter =
    FontFamily(
        variable(R.font.inter, FontWeight.Normal),
        variable(R.font.inter, FontWeight.Medium),
        variable(R.font.inter, FontWeight.SemiBold),
    )

private val Base = Typography()

private fun serif(
    size: Int,
    lineHeight: Int,
    tracking: Double = 0.0,
) = TextStyle(
    fontFamily = Newsreader,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.em,
)

private fun sans(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
    tracking: Double = 0.0,
) = TextStyle(
    fontFamily = Inter,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.em,
)

/**
 * Serif for what you read about a tree (names, titles), sans for the interface. `displaySmall` is the screen title
 * and the tree name; `labelMedium` is the small uppercase eyebrow (section headers, « Arbre remarquable »).
 */
internal val TreesTypography =
    Base.copy(
        displaySmall = serif(size = 40, lineHeight = 44, tracking = -0.02),
        headlineMedium = serif(size = 28, lineHeight = 32, tracking = -0.01),
        headlineSmall = serif(size = 26, lineHeight = 32),
        titleLarge = serif(size = 24, lineHeight = 30),
        titleMedium = serif(size = 20, lineHeight = 24),
        titleSmall = sans(size = 14, lineHeight = 20, weight = FontWeight.SemiBold),
        bodyLarge = sans(size = 16, lineHeight = 24),
        bodyMedium = sans(size = 15, lineHeight = 22),
        bodySmall = sans(size = 13, lineHeight = 18),
        labelLarge = sans(size = 14, lineHeight = 20, weight = FontWeight.Medium),
        labelMedium = sans(size = 12, lineHeight = 16, weight = FontWeight.SemiBold, tracking = 0.08),
        labelSmall = sans(size = 12, lineHeight = 16, weight = FontWeight.Medium),
    )

/** Latin species name under a tree's common name: italic serif. */
val ScientificNameStyle = serif(size = 19, lineHeight = 24).copy(fontStyle = FontStyle.Italic)
