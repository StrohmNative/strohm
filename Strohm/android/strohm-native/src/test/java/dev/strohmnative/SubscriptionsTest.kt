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

val propSpec = PropSpec("name" , arrayListOf("user", "name"))
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
                strohmNative.subscribe(propSpec, noop) {
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
                strohmNative.subscribe(propSpec, noop) {
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
                val matchedSerializedPropSpec = matchResult!!.groups[1]!!.value
                val unescaped = matchedSerializedPropSpec.replace("\\\"", "\"")
                val parsed = Gson().fromJson(unescaped, ArrayList::class.java)
                assertEquals(propSpec.first, parsed[0])
                assertEquals(propSpec.second, parsed[1])
            }
        }

        context("when subscribed") {
            var receivedProp: Prop? = null
            var subscriptionId: UUID? = null
            val handlerFn: HandlerFunction = { receivedProp = it }

            beforeEachTest {
                receivedProp = null
                subscriptionId = null
                strohmNative.whenLoadingFinished()
                subscriptionId = strohmNative.whenSubscriptionCompletes(propSpec, handlerFn)
            }

            it("receives prop updates") {
                val prop = Prop("name", "foo")
                strohmNative.whenIncoming(prop, subscriptionId!!)
                assertEquals(prop, receivedProp)
            }

            it("does not receive prop for someone else") {
                var otherProp: Prop? = null
                val otherId = strohmNative.whenSubscriptionCompletes(propSpec) { prop ->
                        otherProp = prop
                }
                strohmNative.whenIncoming(Prop("name", "foo"), otherId)
                assertNull(receivedProp)
                assertNotNull(otherProp)
            }

            it("does not receive prop after unsubscribe") {
                strohmNative.unsubscribe(subscriptionId!!)
                strohmNative.whenIncoming(Prop("name", "foo"), subscriptionId!!)
                assertNull(receivedProp)
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

fun StrohmNative.whenSubscriptionCompletes(propSpec: PropSpec, handlerFn: HandlerFunction): UUID {
    val future = CompletableFuture<UUID>()
    this.subscribe(propSpec, handlerFn) { uuid ->
        future.complete(uuid)
    }
    return future.get(1, TimeUnit.SECONDS)
}

fun StrohmNative.whenIncoming(prop: Prop, subscriptionId: UUID) {
    this.subscriptions.handlePropUpdate(prop, subscriptionId)
}

/* Subscriptions class extension methods for testing */

internal val Subscriptions.pendingSubscriptions: PersistentList<() -> Unit>?
    get() {
        val holder = BooleanArray(1)
        val pending = _pendingSubscriptions.get(holder)
        val isUsingPending = holder[0]
        return if (isUsingPending) pending else null
    }
