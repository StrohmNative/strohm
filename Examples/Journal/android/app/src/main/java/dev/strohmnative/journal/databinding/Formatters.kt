package dev.strohmnative.journal.databinding

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

object Formatters {
    private val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault())

    @JvmStatic
    fun formatInstant(i: Instant?): String {
        return i?.let { dateFormatter.format(it) } ?: ""
    }
}