import Foundation

class Subscriptions {
    let strohm: Strohm
    var pendingSubscriptions: [() -> Void]? = []
    var subscribers: [UUID: HandlerFunction] = [:]

    init(strohm: Strohm) {
        self.strohm = strohm
        strohm.comms.registerHandlerFunction(name: "subscriptionUpdate",
                                             function: self.subscriptionUpdateHandler)
    }

    func addSubscriber(propsSpec: PropsSpec,
                       handler: @escaping HandlerFunction,
                       completion: @escaping (UUID) -> Void) {
        if pendingSubscriptions != nil {
            pendingSubscriptions!.append({ [weak self] in
                self?.subscribe_(
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
        guard let encodedPropsSpec = strohm.comms.encode(object: propsSpec) else {
            return
        }
        let subscriptionId = UUID()
        subscribers[subscriptionId] = handler
        strohm.call(method: "strohm.store.subscribe_from_native(\"\(subscriptionId.uuidString)\", \"\(encodedPropsSpec)\")")
        completion(subscriptionId)
    }

    func removeSubscriber(subscriptionId: UUID) {
        subscribers.removeValue(forKey: subscriptionId)
        strohm.call(method: "strohm.store.unsubscribe_from_native(\"\(subscriptionId.uuidString)\")")
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

    func subscriptionUpdateHandler(args: JsonComms.Arguments) {
        if let subscriptionIdString = args["subscriptionId"] as? String,
           let subscriptionId = UUID(uuidString: subscriptionIdString),
           let newProps = args["new"] as? [String: Any] {
            handlePropsUpdate(props: newProps,
                              subscriptionId: subscriptionId)
        }
    }
}
