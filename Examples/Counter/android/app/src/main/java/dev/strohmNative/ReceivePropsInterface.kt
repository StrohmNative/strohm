package dev.strohmNative

import android.util.Log
import android.webkit.JavascriptInterface

internal class ReceivePropsInterface(private val strohm: Strohm) {
    @JavascriptInterface
    fun receiveProps(props: String?) {
        if (props == null) {
            return
        }
        Log.d("receiveProps", props)
    }
}