package dev.strohmnative.journal.model

import android.os.Parcel
import android.os.Parcelable
import dev.strohmnative.strohm.ConstructableFromDictionary
import java.time.Instant

data class JournalEntry(val id: String, val title: String, val text: String, val created: Instant):
    Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        Instant.ofEpochMilli(parcel.readLong())
    )

    override fun toString(): String = title

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(text)
        parcel.writeLong(created.toEpochMilli())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<JournalEntry>,
        ConstructableFromDictionary<JournalEntry> {
        override fun createFromParcel(parcel: Parcel): JournalEntry {
            return JournalEntry(parcel)
        }

        override fun newArray(size: Int): Array<JournalEntry?> {
            return arrayOfNulls(size)
        }

        override fun createFromDict(dict: Map<String, Any>): JournalEntry? {
            val id = dict["entry/id"] as? String
            val title = dict["entry/title"] as? String
            val text = dict["entry/text"] as? String
            val created = dict["entry/created"] as? Double

            if (id != null && title != null && text != null && created != null) {
                return JournalEntry(id, title, text, Instant.ofEpochMilli(created.toLong()))
            }

            return null
        }

    }

}
