package dev.strohmnative.strohm

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import java.util.UUID
import java.util.concurrent.atomic.AtomicMarkableReference

class Subscriptions {
    val strohm: Strohm

    internal var _pendingSubscriptions: AtomicMarkableReference<PersistentList<() -> Unit>>
    private var subscribers: MutableMap<UUID, HandlerFunction>

    internal constructor(strohm: Strohm) {
        this.strohm = strohm
        this._pendingSubscriptions = AtomicMarkableReference(persistentListOf(), true)
        this.subscribers = mutableMapOf()
        strohm.comms.registerHandlerFunction("subscriptionUpdate") { args ->
            this.subscriptionUpdateHandler(args)
        }
    }

    fun addSubscriber(
        propsSpec: PropsSpec,
        handler: HandlerFunction,
        completion: (UUID) -> Unit
    ) {
        var succeeded = false
        while (!succeeded) {
            val holder = BooleanArray(1)
            val pending = _pendingSubscriptions.get(holder)
            val isUsingPending = holder[0]

            if (isUsingPending) {
                val newPending = pending.add { this.subscribe_(propsSpec, handler, completion) }
                succeeded = _pendingSubscriptions.compareAndSet(pending, newPending, true, true)
            } else {
                subscribe_(propsSpec, handler, completion)
                succeeded = true
            }
        }
    }

    private fun subscribe_(
        propsSpec: PropsSpec,
        handler: HandlerFunction,
        completion: (UUID) -> Unit
    ) {
        val subscriptionId = UUID.randomUUID()
        subscribers[subscriptionId] = handler
        val encodedPropsSpec = strohm.comms.encode(propsSpec)
        strohm.call("strohm.native.subscribe_from_native(\"$subscriptionId\", \"$encodedPropsSpec\")")
        completion(subscriptionId)
    }

    internal fun removeSubscriber(subscriptionId: UUID) {
        subscribers.remove(subscriptionId)
        strohm.call("strohm.native.unsubscribe_from_native(\"$subscriptionId\")")
    }

    internal fun effectuatePendingSubscriptions() {
        var isUsingPending: Boolean
        var pending: PersistentList<() -> Unit>
        var clearSucceeded: Boolean
        do {
            val holder = BooleanArray(1)
            pending = _pendingSubscriptions.get(holder)
            isUsingPending = holder[0]

            clearSucceeded = _pendingSubscriptions.compareAndSet(pending, persistentListOf(), true, false)
        } while (!clearSucceeded && isUsingPending)

        if (isUsingPending) {
            pending.forEach { it() }
        }
    }

    internal fun handlePropsUpdate(props: Props, subscriptionId: UUID) {
        subscribers[subscriptionId]?.let { it(props) }
    }

    internal fun subscriptionUpdateHandler(args: CommsHandlerArguments) {
        val subscriptionIdString = args["subscriptionId"] as? String
        @Suppress("UNCHECKED_CAST")
        val newProps = args["new"] as? Props
        if (subscriptionIdString != null && newProps != null) {
            val subscriptionId = UUID.fromString(subscriptionIdString)
            handlePropsUpdate(newProps, subscriptionId)
        }
    }
}

