package dev.strohmnative

import com.google.gson.Gson
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import java.util.UUID
import java.util.concurrent.atomic.AtomicMarkableReference

class Subscriptions {
    val strohmNative: StrohmNative

    internal var _pendingSubscriptions: AtomicMarkableReference<PersistentList<() -> Unit>>
    private var subscribers: MutableMap<UUID, HandlerFunction>

    internal constructor(strohmNative: StrohmNative) {
        this.strohmNative = strohmNative
        this._pendingSubscriptions = AtomicMarkableReference(persistentListOf(), true)
        this.subscribers = mutableMapOf()
        strohmNative.comms.registerHandlerFunction("subscriptionUpdate") { args ->
            this.subscriptionUpdateHandler(args)
        }
    }

    fun addSubscriber(
        propSpec: PropSpec,
        handler: HandlerFunction,
        completion: (UUID) -> Unit
    ) {
        var succeeded = false
        while (!succeeded) {
            val holder = BooleanArray(1)
            val pending = _pendingSubscriptions.get(holder)
            val isUsingPending = holder[0]

            if (isUsingPending) {
                val newPending = pending.add { this.subscribe_(propSpec, handler, completion) }
                succeeded = _pendingSubscriptions.compareAndSet(pending, newPending, true, true)
            } else {
                subscribe_(propSpec, handler, completion)
                succeeded = true
            }
        }
    }

    private fun subscribe_(
        propSpec: PropSpec,
        handler: HandlerFunction,
        completion: (UUID) -> Unit
    ) {
        val subscriptionId = UUID.randomUUID()
        subscribers[subscriptionId] = handler
        val encodedPropsSpec = strohmNative.comms.encode(propSpec)
        strohmNative.call("strohm_native.flow.subscribe_from_native(\"$subscriptionId\", \"$encodedPropsSpec\")")
        completion(subscriptionId)
    }

    internal fun removeSubscriber(subscriptionId: UUID) {
        subscribers.remove(subscriptionId)
        strohmNative.call("strohm_native.flow.unsubscribe_from_native(\"$subscriptionId\")")
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

    internal fun handlePropUpdate(prop: Prop, subscriptionId: UUID) {
        subscribers[subscriptionId]?.let { it(prop) }
    }

    internal fun subscriptionUpdateHandler(args: CommsHandlerArguments) {
        val subscriptionIdString = args["subscriptionId"] as? String ?: return

        val newEnc = args["new"] as? String
        val new = Gson().fromJson(newEnc, ArrayList::class.java)
        (new[0] as? PropName)?.let { propName ->
            @Suppress("UNCHECKED_CAST")
            val newProp = Prop(propName, new[1])
            val subscriptionId = UUID.fromString(subscriptionIdString)
            handlePropUpdate(newProp, subscriptionId)
        }
    }
}

