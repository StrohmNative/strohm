import Foundation

open class ViewModelBase<DataType>: ObservableObject {
    var subscriptionId: UUID?
    let propName: String
    let propPath: PropPath
    @Published public var data: DataType

    public init(forPreviewWith data: DataType) {
        self.data = data
        self.propName = "use for preview only"
        self.propPath = []
    }

    public init(initialData: DataType, propName: String, propPath: [Any]) {
        self.data = initialData
        self.propName = propName
        self.propPath = propPath
        Strohm.default.subscribe(
            propsSpec: [propName: propPath],
            handler: receiveProps) { subscriptionId in
            self.subscriptionId = subscriptionId
        }
    }

    deinit {
        if let subscriptionId = self.subscriptionId {
            Strohm.default.unsubscribe(subscriptionId: subscriptionId)
        }
    }

    func receiveProps(props: Props) {
        if let data = propsToData(props: props) {
            DispatchQueue.main.async {
                self.data = data
            }
        }
    }

    func propsToData(props: Props) -> DataType? {
        fatalError("abstract method")
    }
}

protocol PropsHandler {
    associatedtype Data

    func propsToData(props: Props) -> Data?
}
