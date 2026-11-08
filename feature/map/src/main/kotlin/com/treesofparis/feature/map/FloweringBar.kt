package com.treesofparis.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.treesofparis.core.designsystem.theme.TreesTheme
import java.util.Locale

/** French month initials, January first. */
private const val INITIALS = "JFMAMJJASOND"

/** Style A of the design: twelve rounded cells, the flowering months filled, a dot under the current month. */
@Composable
internal fun FloweringSection(
    flowering: FloweringUi,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val kind = if (flowering.showyFlowers) R.string.map_flowering_showy else R.string.map_flowering_discreet
    val status =
        when (flowering.monthsUntilFlowering) {
            0 -> {
                stringResource(R.string.map_flowering_now)
            }

            1 -> {
                stringResource(R.string.map_flowering_next_month, flowering.currentMonthName)
            }

            else -> {
                stringResource(R.string.map_flowering_next, flowering.currentMonthName, flowering.monthsUntilFlowering)
            }
        }
    Column(
        modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.map_flowering_title).uppercase(Locale.FRENCH),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
            Text(stringResource(kind, flowering.range), style = MaterialTheme.typography.bodyMedium)
        }
        MonthCells(flowering, Modifier.clearAndSetSemantics {})
        Text(status, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
    }
}

@Composable
private fun MonthCells(
    flowering: FloweringUi,
    modifier: Modifier = Modifier,
) {
    val fill = if (flowering.showyFlowers) TreesTheme.colors.bloomShowy else TreesTheme.colors.bloomDiscreet
    val ink = MaterialTheme.colorScheme.onSurface
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        flowering.months.forEachIndexed { index, flowers ->
            val current = index + 1 == flowering.currentMonth
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(22.dp)
                        .background(if (flowers) fill else TreesTheme.colors.bloomNone, RoundedCornerShape(6.dp)),
                )
                Text(
                    INITIALS[index].toString(),
                    fontSize = 11.sp,
                    fontWeight = if (current) FontWeight.Bold else FontWeight.Medium,
                    color = if (current) ink else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    Modifier
                        .size(4.dp)
                        .background(if (current) ink else Color.Transparent, CircleShape),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
@Suppress("MagicNumber") // sample months
private fun FloweringSectionPreview() {
    TreesTheme {
        FloweringSection(
            FloweringUi(
                months = List(12) { it in 4..5 },
                showyFlowers = false,
                range = "mai – juin",
                currentMonth = 11,
                currentMonthName = "novembre",
                monthsUntilFlowering = 6,
            ),
        )
    }
}
