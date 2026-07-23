package com.powertrack.mobile.ui.common

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val shortDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("es-ES"))

/** Formatea un timestamp ISO-8601 (`Instant.toString()`, tal como lo devuelve el backend) a "dd MMM yyyy". */
fun formatShortDate(isoInstant: String): String = runCatching {
    Instant.parse(isoInstant).atZone(ZoneId.systemDefault()).format(shortDateFormatter)
}.getOrDefault(isoInstant)
