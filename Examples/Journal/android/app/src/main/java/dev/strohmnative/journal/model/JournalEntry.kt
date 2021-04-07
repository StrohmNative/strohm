package dev.strohmnative.journal.model

import java.time.Instant

data class JournalEntry(val id: String, val title: String, val text: String, val created: Instant) {
    override fun toString(): String = title
}
