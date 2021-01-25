package dev.strohmnative.strohm

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class CheckShadowCLJSReachable {
    companion object {
        fun execute(context: Context) {
            if (BuildConfig.DEBUG) {
                val queue = Volley.newRequestQueue(context)
                val shadowCljsUrl = "http://localhost:9630"
                val stringRequest =
                    StringRequest(
                        Request.Method.OPTIONS,
                        shadowCljsUrl,
                        { _ ->
                            Log.i("strohm", "shadow-cljs is reachable")
                        },
                        { _ ->
                            Log.e(
                                "strohm",
                                "shadow-cljs is not reachable; run `adb reverse tcp:8080 tcp:8080` and `adb reverse tcp:9630 tcp:9630` and restart this app."
                            )
                        }
                    )
                queue.add(stringRequest)
            }
        }
    }
}