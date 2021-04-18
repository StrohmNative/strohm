package dev.strohmnative.journal

import android.app.Application
import dev.strohmnative.strohm.Strohm

class JournalApplication: Application() {
    lateinit var strohm: Strohm

    override fun onCreate() {
        super.onCreate()
        strohm = Strohm.getInstance(applicationContext)
    }
}