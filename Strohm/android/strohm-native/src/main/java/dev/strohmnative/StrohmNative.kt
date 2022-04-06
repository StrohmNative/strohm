package dev.strohmnative

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log as AndroidLog
import android.util.Base64
import android.webkit.*
import java.util.*
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

typealias StatusChangeListener = (property: KProperty<*>, oldValue: StrohmNative.Companion.Status, newValue: StrohmNative.Companion.Status) -> Unit

/**
 * Strohm Native main class.
 */
class StrohmNative internal constructor(val context: Context) {
    private lateinit var appJsPath: String
    private var port: Int? = null
    internal var webView: WebView = WebView(context)
    internal val comms = JsonComms()
    internal val subscriptions = Subscriptions(this)
    internal val statePersister = StatePersister(this)
    var onStatusChange: StatusChangeListener? = null
    var status: Status by Delegates.observable(Status.UNINITIALIZED, {
            prop, old, new -> onStatusChange?.let { it(prop, old, new) }
    })
        private set

    @SuppressLint("SetJavaScriptEnabled")
    fun install(appJsPath: String, port: Int? = null) {
        webView.settings.javaScriptEnabled = true
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        webView.addJavascriptInterface(ReceivePropsInterface(this), "strohmNativeReceiveProps")
        webView.webViewClient = StrohmNativeWebViewClient(this)

        if (18 < Build.VERSION.SDK_INT) {
            // 18 = JellyBean MR2, 19 = KitKat
            webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        }

        this.appJsPath = appJsPath
        this.port = port

        reload()
    }

    fun reload() {
        status = Status.LOADING
        val initialStateVar = statePersister.loadState()?.let {
            val escaped = it.replace("\"", "\\\"")
            "var strohmNativePersistedState=\"$escaped\";"
        } ?: ""

        if (BuildConfig.DEBUG) {
            CheckShadowCLJSReachable.execute(this.context)

            val unencodedHtml = """
            <html>
                <body style='background-color: #ddd'>
                    <h1>Hi!</h1><div id='content'></div>
                    <script>$initialStateVar</script>
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
                    <script>$initialStateVar</script>
                    <script src="file:///android_asset/$appJsPath"></script>
                </body>
            </html>
            """.trimIndent()
            webView.loadDataWithBaseURL("base_is_ignored_but_needed_for_file_urls://", unencodedHtml, "text/html", "base64", null)
        }
    }

    internal fun loadingFinished() {
        status = Status.OK
        subscriptions.effectuatePendingSubscriptions()
    }

    internal fun loadingFailed() {
        status = Status.SERVER_NOT_RUNNING
        AndroidLog.e("strohm-native", "Please make sure dev server is running and that you enabled adb (reverse) proxy")
    }

    internal fun call(method: String) {
        webView.evaluateJavascript(method) {
            result -> AndroidLog.d("strohm-native", "cljs call result: $result")
        }
    }

    fun subscribe(
        propSpec: PropSpec,
        handler: HandlerFunction,
        completion: (UUID) -> Unit
    ) {
        subscriptions.addSubscriber(propSpec, handler, completion)
    }

    fun unsubscribe(subscriptionId: UUID) {
        subscriptions.removeSubscriber(subscriptionId)
    }

    fun dispatch(type: String, payload: Map<String, Any> = mapOf()) {
        Log.debug("strohm-native", "dispatch-kotlin", type, payload)
        val action: Map<String, Any> = mapOf("type" to type, "payload" to payload)
        val serializedAction = comms.encode(action)
        val method = "globalThis.strohm_native.flow.dispatch_from_native(\"$serializedAction\")"
        call(method)
    }

    fun dispatch(type: String, payload: ConvertableToDictionary) {
        dispatch(type, payload.toDict())
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var sharedInstance: StrohmNative? = null

        fun getInstance(context: Context? = null, onStatusChange: StatusChangeListener? = null): StrohmNative {
            if (sharedInstance == null && context == null) {
                throw RuntimeException("StrohmNative.getInstance needs to be called with application context first; make sure you call Strohm.getInstance(applicationContext) in your main activity or Application subclass")
            }

            val instance = sharedInstance ?: StrohmNative(context!!.applicationContext)
            if (sharedInstance == null) {
                onStatusChange?.let { instance.onStatusChange = it }
                instance.install("main.js", 8080)
                sharedInstance = instance
            }
            return instance
        }

        enum class Status(val rawValue: String) {
            UNINITIALIZED("uninitialized"),
            LOADING("loading"),
            SERVER_NOT_RUNNING("server not running"),
            OK("ok")
        }
    }

    private class StrohmNativeWebViewClient(private val strohmNative: StrohmNative) : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            view?.evaluateJavascript("this.hasOwnProperty('strohm_native')") { result ->
                val hasStrohmNative = result != null && result.toBoolean()
                if (!hasStrohmNative) {
                    strohmNative.loadingFailed()
                    return@evaluateJavascript
                }
                strohmNative.loadingFinished()
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
            AndroidLog.d("strohm-native","error $msg")
            Log.debug("strohm-native","received error", msg ?: "?")
        }
    }
}

typealias PropName = String
typealias PropPath = List<Any>
typealias PropSpec = Pair<PropName, PropPath>
typealias Prop = Pair<PropName, Any>
typealias HandlerFunction = (Prop) -> Unit
