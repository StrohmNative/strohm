package com.vandenoord.counter

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.ViewGroup
import android.webkit.WebSettings
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

        if (18 < Build.VERSION.SDK_INT ){
            //18 = JellyBean MR2, KITKAT=19
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        }

        this.webView = webView
    }

    fun reload(src: android.view.View) {
        val port = 8080
        val appJsPath = "main.js"
        val unencodedHtml = """
            <html>
                <body style='background-color: #ddd'>
                    <h1>Hi!</h1><div id='content'></div>
                   <script type="text/javascript">
                        console.debug('script')
                        window.onload = function(e) {
                            console.debug('onload')
                            document.getElementById('content').innerHTML += 'onload<br />'
                            globalThis.app.main.init()
                        }
                    </script>
                    <script src="http://10.0.2.2:$port/$appJsPath"></script>
                </body>
            </html>
            """.trimIndent()
        val encodedHtml = Base64.encodeToString(unencodedHtml.toByteArray(), Base64.NO_PADDING)
        webView.loadData(encodedHtml, "text/html", "base64")
    }
}