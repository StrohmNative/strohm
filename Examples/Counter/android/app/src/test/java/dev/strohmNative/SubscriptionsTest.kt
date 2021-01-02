package dev.strohmNative

import android.content.Context
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito.mock
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

val propsSpec: PropsSpec = mapOf(
    "name" to "user.name",
    "city" to "user.address.city"
)
val noop: HandlerFunction = {}

object SubscriptionsSpec: Spek({
    describe("Subscriptions") {
        lateinit var strohm: Strohm

        beforeEachTest {
            val mockContext = mock(Context::class.java)
            strohm = Strohm(mockContext)
        }

        context("when subscribing before load finishes") {
            var subscriptionComplete: Boolean = false

            beforeEachTest {
                subscriptionComplete = false
                strohm.subscribe(propsSpec, noop) {
                    subscriptionComplete = true
                }
            }

            it("remembers pending subscription details") {
                assertEquals(1, strohm.subscriptions.pendingSubscriptions?.count())
            }

            it("is not yet complete") {
                assertEquals(false, subscriptionComplete)
            }

            it("has nothing pending after load finishes") {
                strohm.whenLoadingFinished()
                assertEquals(null, strohm.subscriptions.pendingSubscriptions)
            }

            it("calls completion handler after load finishes") {
                strohm.whenLoadingFinished()
                assertEquals(true, subscriptionComplete)
            }
        }

        context("when subscribing after load finishes") {
            var subscriptionComplete: Boolean = false

            beforeEachTest {
                strohm.whenLoadingFinished()
                subscriptionComplete = false
                strohm.subscribe(propsSpec, noop) {
                    subscriptionComplete = true
                }
            }

            it("is complete") {
                assertEquals(true, subscriptionComplete)
            }
        }

        context("when subscribed") {
            var receivedProps: Props? = null
            var subscriptionId: UUID? = null
            val handlerFn: HandlerFunction = { receivedProps = it }

            beforeEachTest {
                receivedProps = null
                subscriptionId = null
                strohm.whenLoadingFinished()
                subscriptionId = strohm.whenSubscriptionCompletes(propsSpec, handlerFn)
            }

            it("receives prop updates") {
                val props = mapOf("name" to "foo")
                strohm.whenIncoming(props, subscriptionId!!)
                assertEquals(props, receivedProps)
            }

            it("does not receive props for someone else") {
                var otherProps: Props? = null
                val otherId = strohm.whenSubscriptionCompletes(propsSpec) { props ->
                        otherProps = props
                }
                strohm.whenIncoming(mapOf("name" to "foo"), otherId)
                assertNull(receivedProps)
                assertNotNull(otherProps)
            }

            it("does not receive props after unsubscribe") {
                strohm.unsubscribe(subscriptionId!!)
                strohm.whenIncoming(mapOf("name" to "foo"), subscriptionId!!)
                assertNull(receivedProps)
            }
        }

    }
})

/* Strohm class extension methods for testing */

fun Strohm.whenLoadingFinished() {
    this.loadingFinished()
}

fun Strohm.whenSubscriptionCompletes(propsSpec: PropsSpec, handlerFn: HandlerFunction): UUID {
    val future = CompletableFuture<UUID>()
    this.subscribe(propsSpec, handlerFn) { uuid ->
        future.complete(uuid)
    }
    return future.get(1, TimeUnit.SECONDS)
}

fun Strohm.whenIncoming(props: Props, subscriptionId: UUID) {
    this.subscriptions.handlePropsUpdate(props, subscriptionId)
}