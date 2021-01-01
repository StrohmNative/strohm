package dev.strohmNative

import android.webkit.JavascriptInterface
import org.json.JSONObject

class JsToJavaInterface(private val strohm: Strohm) {

    @JavascriptInterface
    fun receiveProps(props: String?) {
        if (props == null) {
            return
        }

    }

}