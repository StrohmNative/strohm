import Foundation

open class ArrayViewModel<EntryType: ConstructableFromDictionary>: ObservableObject {
    @Published public private(set) var entries: [EntryType]
    let propName: String
    let propPath: PropPath
    private var subscriptionId: UUID?

    public init(initialData: [EntryType], propName: String, propPath: [Any]) {
        entries = initialData
        self.propName = propName
        self.propPath = propPath
        Strohm.default.subscribe(
            propsSpec: [propName: propPath],
            handler: receiveProps) { subscriptionId in
            self.subscriptionId = subscriptionId
        }
    }

    public init(forPreviewWith data: [EntryType]) {
        self.entries = data
        self.propName = "use for preview only"
        self.propPath = []
    }

    deinit {
        if let subscriptionId = self.subscriptionId {
            Strohm.default.unsubscribe(subscriptionId: subscriptionId)
        }
    }

    func receiveProps(props: Props) {
        if let rawData = props[self.propName] as? [[String:Any]] {
            let data = rawData.compactMap(EntryType.init(from:))
            print("Received entries: ", data)
            self.entries = data
        }
    }
}
