package com.treesofparis.domain.format

import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

/** « mai », or « février – avril » for a run of months. */
fun formatMonthRange(months: Set<Month>): String =
    listOf(months.min(), months.max()).distinct().joinToString(" – ", transform = ::formatMonth)

/** « novembre ». */
fun formatMonth(month: Month): String = month.getDisplayName(TextStyle.FULL, Locale.FRENCH)
