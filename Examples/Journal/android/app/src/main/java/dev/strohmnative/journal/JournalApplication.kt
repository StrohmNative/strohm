package dev.strohmnative.journal

import android.app.Application
import dev.strohmnative.StrohmNative

class JournalApplication: Application() {
    lateinit var strohmNative: StrohmNative

    override fun onCreate() {
        super.onCreate()
        strohmNative = StrohmNative.getInstance(applicationContext)
    }
}
