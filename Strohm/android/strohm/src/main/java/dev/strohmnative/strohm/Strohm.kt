package dev.strohmnative.strohm

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Base64
import android.util.Log
import android.webkit.*
import java.util.*

class Strohm internal constructor(val context: Context) {
    private lateinit var appJsPath: String
    private var port: Int? = null
    internal var webView: WebView = WebView(context)
    internal val comms = JsonComms()
    internal val subscriptions = Subscriptions(this)

    @SuppressLint("SetJavaScriptEnabled")
    fun install(appJsPath: String, port: Int? = null) {
        webView.settings.javaScriptEnabled = true
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        webView.addJavascriptInterface(ReceivePropsInterface(this), "strohmReceiveProps")
        webView.webViewClient = StrohmWebViewClient(this)

        if (18 < Build.VERSION.SDK_INT) {
            // 18 = JellyBean MR2, 19 = KitKat
            webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        }

        this.appJsPath = appJsPath
        this.port = port

        reload()
    }

    fun reload() {
        if (BuildConfig.DEBUG) {
            CheckShadowCLJSReachable.execute(this.context)

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
                    <script src="http://localhost:$port/$appJsPath"></script>
                </body>
            </html>
            """.trimIndent()
            val encodedHtml = Base64.encodeToString(unencodedHtml.toByteArray(), Base64.NO_PADDING)
            webView.loadData(encodedHtml, "text/html", "base64")
        } else {
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
                    <script src="file:///android_asset/$appJsPath"></script>
                </body>
            </html>
            """.trimIndent()
            webView.loadDataWithBaseURL("base_is_ignored_but_needed_for_file_urls://", unencodedHtml, "text/html", "base64", null)
        }
    }

    internal fun loadingFinished() {
        //  TODO: self.status = .ok
        subscriptions.effectuatePendingSubscriptions()
    }

    internal fun call(method: String) {
        webView.evaluateJavascript(method) {
            result -> Log.d("strohm", "cljs call result: $result")
        }
    }

    fun subscribe(
        propsSpec: PropsSpec,
        handler: HandlerFunction,
        completion: (UUID) -> Unit
    ) {
        subscriptions.addSubscriber(propsSpec, handler, completion)
    }

    fun unsubscribe(subscriptionId: UUID) {
        subscriptions.removeSubscriber(subscriptionId)
    }

    fun dispatch(type: String, payload: Map<String, Any> = mapOf()) {
        Log.d("strohm", "dispatch $type $payload")
        val action: Map<String, Any> = mapOf("type" to type, "payload" to payload)
        val serializedAction = comms.encode(action)
        val method = "globalThis.strohm.core.dispatch_from_native(\"$serializedAction\")"
        call(method)
    }

    companion object {
        private var sharedInstance: Strohm? = null

        fun getInstance(context: Context? = null): Strohm {
            if (sharedInstance == null && context == null) {
                throw RuntimeException("needs to be called with application context first")
            }

            val instance = sharedInstance ?: Strohm(context!!)
            if (sharedInstance == null) { sharedInstance = instance }
            return instance
        }
    }

    class StrohmWebViewClient(private val strohm: Strohm) : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Log.d("strohm","page finished $url")
            view?.evaluateJavascript("this.hasOwnProperty('strohm')") { result ->
                Log.d("strohm", "onload result: $result")
                val hasStrohm = result != null && result.toBoolean()
                if (!hasStrohm) {
                    Log.e("strohm", "Please make sure dev server is running")
                    return@evaluateJavascript
                }
                strohm.loadingFinished()
            }
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            val msg = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                error?.description
            } else {
                error.toString()
            }
            Log.d("strohm","error $msg")
        }

    }
}

typealias PropsSpec = Map<String, String>
typealias Props = Map<String, Any>
typealias HandlerFunction = (Props) -> Unit