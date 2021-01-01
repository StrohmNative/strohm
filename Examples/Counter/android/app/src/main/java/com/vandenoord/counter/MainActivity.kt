package com.vandenoord.counter

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.LinearLayout

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val topContainer = findViewById<LinearLayout>(R.id.topContainer)
        val webView = WebView(applicationContext)
        webView.settings.javaScriptEnabled = true
        topContainer.addView(webView)

        val layoutParams = webView.layoutParams
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        webView.layoutParams = layoutParams
        webView.requestLayout()

        this.webView = webView
    }

    fun reload(src: android.view.View) {
        val unencodedHtml = """
            <html>
                <body style='background-color: #ddd'>
                    <h1>Hi!</h1><div id='content'></div>
                   <script type="text/javascript">
                        console.debug('script')
                        window.onload = function(e) {
                            console.debug('onload')
                            document.getElementById('content').innerHTML += 'onload<br />'
                            // globalThis.app.main.init()
                        }
                    </script>
                </body>
            </html>
            """.trimIndent()
        val encodedHtml = Base64.encodeToString(unencodedHtml.toByteArray(), Base64.NO_PADDING)
        webView.loadData(encodedHtml, "text/html", "base64")
    }
}