package dev.strohmnative.journal.dummy

import java.time.Instant
import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    val ITEMS: MutableList<JournalEntry> = ArrayList()

    /**
     * A map of sample (dummy) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, JournalEntry> = HashMap()

    private val COUNT = 25

    private val LIPSUM: String =
        """Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor
            | incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud
            | exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure
            | dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
            | Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt
            | mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipisicing elit,
            | sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim
            | veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo
            | consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum
            | dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident,
            | sunt in culpa qui officia deserunt mollit anim id est laborum.""".trimMargin()

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createDummyItem(i))
        }
    }

    private fun addItem(item: JournalEntry) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
    }

    private fun createDummyItem(position: Int): JournalEntry {
        val text = if (position == 1) { LIPSUM } else { "Text $position" }
        return JournalEntry(
            position.toString(),
            "Title $position",
            text,
            Instant.now())
    }

    data class JournalEntry(val id: String, val title: String, val text: String, val created: Instant) {
        override fun toString(): String = title
    }
}