package dev.strohmnative

import android.content.Context
import android.webkit.WebView
import com.google.gson.Gson
import kotlinx.collections.immutable.PersistentList
import org.junit.jupiter.api.Assertions.*
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

val propsSpec: PropsSpec = mapOf(
    "name" to arrayListOf("user", "name"),
    "city" to arrayListOf("user", "address", "city")
)
val noop: HandlerFunction = {}

object SubscriptionsSpec: Spek({
    describe("Subscriptions") {
        lateinit var strohmNative: StrohmNative
        lateinit var webViewMock: WebView

        beforeEachTest {
            val mockContext = mock(Context::class.java)
            strohmNative = StrohmNative(mockContext)

            webViewMock = mock(WebView::class.java)
            strohmNative.webView = webViewMock
        }

        context("when subscribing before load finishes") {
            var subscriptionComplete: Boolean = false

            beforeEachTest {
                subscriptionComplete = false
                strohmNative.subscribe(propsSpec, noop) {
                    subscriptionComplete = true
                }
            }

            it("remembers pending subscription details") {
                assertEquals(1, strohmNative.subscriptions.pendingSubscriptions?.count())
            }

            it("is not yet complete") {
                assertEquals(false, subscriptionComplete)
            }

            it("has nothing pending after load finishes") {
                strohmNative.whenLoadingFinished()
                assertEquals(null, strohmNative.subscriptions.pendingSubscriptions)
            }

            it("calls completion handler after load finishes") {
                strohmNative.whenLoadingFinished()
                assertEquals(true, subscriptionComplete)
            }
        }

        context("when subscribing after load finishes") {
            var subscriptionComplete: Boolean = false

            beforeEachTest {
                strohmNative.whenLoadingFinished()
                subscriptionComplete = false
                strohmNative.subscribe(propsSpec, noop) {
                    subscriptionComplete = true
                }
            }

            it("is complete") {
                assertEquals(true, subscriptionComplete)
            }

            it("has called subscribe on the web view") {
                val jsCodeCaptor = ArgumentCaptor.forClass(String::class.java)
                verify(webViewMock, times(1))
                    .evaluateJavascript(jsCodeCaptor.capture(), any())
                val expected = """strohm_native\.flow\.subscribe_from_native\(".*", ?"(.*)"\)"""
                assertLinesMatch(listOf(expected), listOf(jsCodeCaptor.value))
                val matchResult = expected.toRegex().matchEntire(jsCodeCaptor.value)
                val matchedSerializedPropsSpec = matchResult!!.groups[1]!!.value
                val unescaped = matchedSerializedPropsSpec.replace("\\\"", "\"")
                val parsed = Gson().fromJson(unescaped, HashMap::class.java)
                assertEquals(propsSpec["name"], parsed["name"])
                assertEquals(propsSpec["city"], parsed["city"])
            }
        }

        context("when subscribed") {
            var receivedProps: Props? = null
            var subscriptionId: UUID? = null
            val handlerFn: HandlerFunction = { receivedProps = it }

            beforeEachTest {
                receivedProps = null
                subscriptionId = null
                strohmNative.whenLoadingFinished()
                subscriptionId = strohmNative.whenSubscriptionCompletes(propsSpec, handlerFn)
            }

            it("receives prop updates") {
                val props = mapOf("name" to "foo")
                strohmNative.whenIncoming(props, subscriptionId!!)
                assertEquals(props, receivedProps)
            }

            it("does not receive props for someone else") {
                var otherProps: Props? = null
                val otherId = strohmNative.whenSubscriptionCompletes(propsSpec) { props ->
                        otherProps = props
                }
                strohmNative.whenIncoming(mapOf("name" to "foo"), otherId)
                assertNull(receivedProps)
                assertNotNull(otherProps)
            }

            it("does not receive props after unsubscribe") {
                strohmNative.unsubscribe(subscriptionId!!)
                strohmNative.whenIncoming(mapOf("name" to "foo"), subscriptionId!!)
                assertNull(receivedProps)
            }

            it("calls unsubscribe on the web view when unsubscribing") {
                reset(webViewMock)

                strohmNative.unsubscribe(subscriptionId!!)

                val jsCodeCaptor = ArgumentCaptor.forClass(String::class.java)
                verify(webViewMock, times(1))
                    .evaluateJavascript(jsCodeCaptor.capture(), any())
                val expected = """strohm_native\.flow\.unsubscribe_from_native\("(.*)"\)"""
                assertLinesMatch(listOf(expected), listOf(jsCodeCaptor.value))
                val matchResult = expected.toRegex().matchEntire(jsCodeCaptor.value)
                val matchedSubscriptionId = matchResult!!.groups[1]!!.value
                assertEquals(subscriptionId!!.toString(), matchedSubscriptionId)
            }
        }
    }
})

/* Strohm class extension methods for testing */

fun StrohmNative.whenLoadingFinished() {
    this.loadingFinished()
}

fun StrohmNative.whenSubscriptionCompletes(propsSpec: PropsSpec, handlerFn: HandlerFunction): UUID {
    val future = CompletableFuture<UUID>()
    this.subscribe(propsSpec, handlerFn) { uuid ->
        future.complete(uuid)
    }
    return future.get(1, TimeUnit.SECONDS)
}

fun StrohmNative.whenIncoming(props: Props, subscriptionId: UUID) {
    this.subscriptions.handlePropsUpdate(props, subscriptionId)
}

/* Subscriptions class extension methods for testing */

internal val Subscriptions.pendingSubscriptions: PersistentList<() -> Unit>?
    get() {
        val holder = BooleanArray(1)
        val pending = _pendingSubscriptions.get(holder)
        val isUsingPending = holder[0]
        return if (isUsingPending) pending else null
    }
