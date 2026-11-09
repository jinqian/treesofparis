package fr.arbresdeparis.domain.format

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Paris time: an instant late in the evening UTC already belongs to the next day in Paris. */
private val PARIS = ZoneId.of("Europe/Paris")
private val DATE = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH)

/** « 6 novembre 2026 », the day [instant] falls on in Paris. */
fun formatParisDate(instant: Instant): String = DATE.format(instant.atZone(PARIS))
