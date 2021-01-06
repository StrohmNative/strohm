package dev.strohmNative

import android.util.Log
import android.webkit.JavascriptInterface

internal class ReceivePropsInterface(private val strohm: Strohm) {
    @JavascriptInterface
    fun receiveProps(message: String?) { // TODO: rename to "jsToNative" or something like that
        if (message == null) {
            return
        }
        strohm.comms.jsToNative(message)
    }
}