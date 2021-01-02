package dev.strohmNative

import java.util.*

class Subscriptions internal constructor(val strohm: Strohm) {
    internal var pendingSubscriptions: MutableList<() -> Unit>? = Collections.synchronizedList(mutableListOf())
    private var subscribers: MutableMap<UUID, HandlerFunction> = mutableMapOf()

    fun addSubscriber(
        propsSpec: PropsSpec,
        handler: HandlerFunction,
        completion: (UUID) -> Unit
    ) {
        pendingSubscriptions = pendingSubscriptions?.let { pending ->
            pending.add { this.subscribe_(propsSpec, handler, completion) }
            pending
        }
        if (pendingSubscriptions == null) {
            subscribe_(propsSpec, handler, completion)
        }
    }

    private fun subscribe_(
        propsSpec: PropsSpec,
        handler: HandlerFunction,
        completion: (UUID) -> Unit
    ) {
        val subscriptionId = UUID.randomUUID()
        subscribers[subscriptionId] = handler
        completion(subscriptionId)
    }

    internal fun removeSubscriber(subscriptionId: UUID) {
        subscribers.remove(subscriptionId)
    }

    internal fun effectuatePendingSubscriptions() {
        pendingSubscriptions = pendingSubscriptions?.let { pending ->
            pending.forEach { it() }
            null
        }
    }

    internal fun handlePropsUpdate(props: Props, subscriptionId: UUID) {
        subscribers[subscriptionId]?.let { it(props) }
    }
}

