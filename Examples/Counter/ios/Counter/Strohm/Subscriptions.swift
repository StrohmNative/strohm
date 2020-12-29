import Foundation

var pendingSubscriptions: [() -> Void]? = []
var subscribers: [UUID: HandlerFunction] = [:]

func addSubscriber(propsSpec: PropsSpec,
                   handler: @escaping HandlerFunction,
                   completion: @escaping (UUID) -> Void) {
    if pendingSubscriptions != nil {
        pendingSubscriptions!.append({
            subscribe_(
                propsSpec: propsSpec,
                handler: handler,
                completion: completion)
        })
    } else {
        subscribe_(propsSpec: propsSpec,
                   handler: handler,
                   completion: completion)
    }
}

private func subscribe_(propsSpec: PropsSpec,
                        handler: @escaping HandlerFunction,
                        completion: (UUID) -> Void) {
    guard let encodedPropsSpec = Strohm.default.comms.encode(object: propsSpec) else {
        return
    }
    let subscriptionId = UUID()
    subscribers[subscriptionId] = handler
    Strohm.default.call(method: "strohm.store.subscribe_from_native(\"\(subscriptionId.uuidString)\", \"\(encodedPropsSpec)\")")
    completion(subscriptionId)
}

func removeSubscriber(subscriptionId: UUID) {
    subscribers.removeValue(forKey: subscriptionId)
}

func effectuatePendingSubscriptions() {
    if let pending = pendingSubscriptions {
        pendingSubscriptions = nil
        for subscriber in pending { subscriber() }
    }
}

func handlePropsUpdate(props: Props, subscriptionId: UUID) {
    subscribers[subscriptionId]?(props)
}
