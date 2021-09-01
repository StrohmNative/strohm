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
        StrohmNative.default.subscribe(
            propsSpec: [propName: propPath],
            handler: receiveProps) { subscriptionId in
            self.subscriptionId = subscriptionId
        }
    }

    deinit {
        if let subscriptionId = self.subscriptionId {
            StrohmNative.default.unsubscribe(subscriptionId: subscriptionId)
        }
    }

    func receiveProps(props: Props) {
        if let data = propsToData(props: props) {
            DispatchQueue.main.async { [weak self] in
                self?.store(data: data)
            }
        }
    }

    func propsToData(props: Props) -> DataType? {
        fatalError("abstract method")
    }

    func store(data: DataType) {
        fatalError("abstract method")
    }
}

protocol PropsHandler {
    associatedtype Data

    func propsToData(props: Props) -> Data?
}
