import Foundation

open class ViewModelBase<DataType>: ObservableObject {
    var subscriptionId: UUID?
    let propName: PropName
    let propPath: PropPath

    public init() {
        self.propName = "use for preview only"
        self.propPath = []
    }

    public init(propName: PropName, propPath: PropPath) {
        self.propName = propName
        self.propPath = propPath
        StrohmNative.default.subscribe2(
            propSpec: (propName, propPath),
            handler: { [weak self] serializedProp in
                self?.receiveProp2(serializedProp: serializedProp)
            },
            completion: { [weak self] subscriptionId in
                self?.subscriptionId = subscriptionId
            }
        )
    }

    deinit {
        if let subscriptionId = self.subscriptionId {
            StrohmNative.default.unsubscribe(subscriptionId: subscriptionId)
        }
    }

    private func receiveProp2(serializedProp: String) {
        if let data = propToData2(serializedProp: serializedProp) {
            DispatchQueue.main.async { [weak self] in
                self?.store(data: data)
            }
        }
    }

    func propToData2(serializedProp: String) -> DataType? {
        fatalError("abstract method")
    }

    func store(data: DataType) {
        fatalError("abstract method")
    }

    func decoder() -> JSONDecoder {
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .millisecondsSince1970
        return decoder
    }
}

