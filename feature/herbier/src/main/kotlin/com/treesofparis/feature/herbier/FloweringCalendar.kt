package com.treesofparis.feature.herbier

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.treesofparis.core.designsystem.component.TreesCardGroup
import com.treesofparis.core.designsystem.component.TreesDivider
import com.treesofparis.core.designsystem.leaf.LeafDrawing
import com.treesofparis.core.designsystem.leaf.TreeLeaf
import com.treesofparis.core.designsystem.theme.TreesSpacing
import com.treesofparis.core.designsystem.theme.TreesTheme
import java.util.Locale

/** French month initials, January first. */
private const val INITIALS = "JFMAMJJASOND"
private const val MONTHS = 12
private const val SEASON_LEAVES = 6
private val NAME_WIDTH = 96.dp
private val CELL_GAP = 2.dp
private val GRID_PADDING = 14.dp

/** « En fleur en novembre »: what flowers now, or when the next flowers come. Opens the calendar. */
@Composable
internal fun SeasonCard(
    flowering: FloweringCalendarUi,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = TreesSpacing.m),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(R.string.herbier_flowering_now, flowering.currentMonthName).uppercase(Locale.FRENCH),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.herbier_flowering_approx),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
            }
            if (flowering.inFlower.isEmpty()) Resting(flowering.next) else InFlower(flowering.inFlower)
        }
    }
}

@Composable
private fun Resting(next: NextFloweringUi?) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(R.string.herbier_flowering_rest), style = MaterialTheme.typography.titleMedium)
        next?.let {
            val names = it.leaves.map { leaf -> stringResource(leaf.label).lowercase(Locale.FRENCH) }
            Text(
                stringResource(R.string.herbier_flowering_next, it.monthName, frenchList(names)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InFlower(leaves: List<TreeLeaf>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            pluralStringResource(R.plurals.herbier_flowering_count, leaves.size, leaves.size),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(TreesSpacing.s)) {
            leaves.take(SEASON_LEAVES).forEach { LeafDrawing(it, Modifier.size(32.dp), strokeWidth = 1.3.dp) }
        }
        Text(
            leaves.map { stringResource(it.label) }.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** « noisetier », « noisetier et orme », « noisetier, orme et if ». */
private fun frenchList(names: List<String>): String =
    if (names.size < 2) names.joinToString() else names.dropLast(1).joinToString(", ") + " et " + names.last()

/** The full calendar: one row per genus, the current month's column washed in gold. */
@Composable
internal fun FloweringCalendar(flowering: FloweringCalendarUi) {
    val wash = TreesTheme.colors.currentMonth
    TreesCardGroup(contentPadding = PaddingValues()) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .drawBehind { drawMonthColumn(flowering.currentMonth, wash) }
                    .padding(horizontal = GRID_PADDING, vertical = 12.dp),
        ) {
            MonthHeader(flowering.currentMonth)
            TreesDivider()
            flowering.rows.forEachIndexed { index, row ->
                CalendarRow(row)
                if (index < flowering.rows.lastIndex) TreesDivider()
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMonthColumn(
    month: Int,
    color: Color,
) {
    val gap = CELL_GAP.toPx()
    val inner = size.width - 2 * GRID_PADDING.toPx()
    val cell = (inner - NAME_WIDTH.toPx() - MONTHS * gap) / MONTHS
    val inset = 6.dp.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(GRID_PADDING.toPx() + NAME_WIDTH.toPx() + gap + (month - 1) * (cell + gap), inset),
        size = Size(cell, size.height - 2 * inset),
        cornerRadius = CornerRadius(8.dp.toPx()),
    )
}

@Composable
private fun MonthHeader(current: Int) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp).clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(CELL_GAP),
    ) {
        Box(Modifier.width(NAME_WIDTH))
        INITIALS.forEachIndexed { index, initial ->
            val isCurrent = index + 1 == current
            Text(
                initial.toString(),
                fontSize = 11.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrent) colors.onTertiaryContainer else colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CalendarRow(row: FloweringRowUi) {
    val name = stringResource(row.leaf.label)
    val kind =
        stringResource(if (row.showyFlowers) R.string.herbier_flowering_showy else R.string.herbier_flowering_discreet)
    val description = stringResource(R.string.herbier_flowering_row, name, row.range, kind.lowercase(Locale.FRENCH))
    val fill = if (row.showyFlowers) TreesTheme.colors.bloomShowy else TreesTheme.colors.bloomDiscreet
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                // Room above and below a name on two lines (« Cerisier, prunier »), off the dividers.
                .padding(vertical = 6.dp)
                .clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(CELL_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            name,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, lineHeight = 18.sp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(NAME_WIDTH),
        )
        row.months.forEach { flowers ->
            Box(
                Modifier
                    .weight(1f)
                    .height(16.dp)
                    .background(if (flowers) fill else Color.Transparent, RoundedCornerShape(4.dp)),
            )
        }
    }
}

/** « Fleurs voyantes », « Fleurs discrètes ». */
@Composable
internal fun FloweringLegend() {
    Row(
        modifier = Modifier.padding(horizontal = TreesSpacing.s),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        LegendItem(TreesTheme.colors.bloomShowy, stringResource(R.string.herbier_flowering_showy))
        LegendItem(TreesTheme.colors.bloomDiscreet, stringResource(R.string.herbier_flowering_discreet))
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(width = 14.dp, height = 10.dp).background(color, RoundedCornerShape(3.dp)))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

/** A few rows of the calendar, small, for the Floraisons card. */
@Composable
internal fun MiniCalendar(
    rows: List<FloweringRowUi>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { row ->
            val fill = if (row.showyFlowers) TreesTheme.colors.bloomShowy else TreesTheme.colors.bloomDiscreet
            Row(horizontalArrangement = Arrangement.spacedBy(CELL_GAP)) {
                row.months.forEach { flowers ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(9.dp)
                            .background(if (flowers) fill else TreesTheme.colors.bloomNone, RoundedCornerShape(2.dp)),
                    )
                }
            }
        }
    }
}
