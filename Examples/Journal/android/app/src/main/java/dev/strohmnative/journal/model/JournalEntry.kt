package dev.strohmnative.journal.model

import android.os.Parcel
import android.os.Parcelable
import dev.strohmnative.ConstructableFromDictionary
import dev.strohmnative.ConvertableToDictionary
import java.time.Instant

data class JournalEntry(var id: String, var title: String, var text: String, var created: Instant):
    Parcelable, ConvertableToDictionary {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        Instant.ofEpochMilli(parcel.readLong())
    )

    override fun toDict(): Map<String, Any> {
        return mapOf(
            "entry/id" to id,
            "entry/title" to title,
            "entry/text" to text,
            "entry/created" to created.toEpochMilli()
        )
    }

    override fun toString(): String = "$title/$text"

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
