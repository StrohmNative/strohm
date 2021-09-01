package dev.strohmnative

import android.webkit.JavascriptInterface

internal class ReceivePropsInterface(private val strohmNative: StrohmNative) {
    @JavascriptInterface
    fun receiveProps(message: String?) { // TODO: rename to "jsToNative" or something like that
        if (message == null) {
            return
        }
        strohmNative.comms.jsToNative(message)
    }
}
