import Foundation

open class KeyedArrayViewModel<EntryType: ConstructableFromDictionary>: ViewModelBase<[EntryType]> {
    public var sorter: ((EntryType, EntryType) -> Bool)?

    public var entries: [EntryType] {
        willSet {
            // https://stackoverflow.com/questions/57615920/published-property-wrapper-not-working-on-subclass-of-observableobject
            self.objectWillChange.send()
        }
    }

    public init(initialEntries: [EntryType], propName: PropName, propPath: PropPath) {
        self.entries = initialEntries
        super.init(propName: propName, propPath: propPath)
    }

    override func propsToData(props: Props) -> [EntryType]? {
        guard let rawData = props[self.propName] as? [PropName: Props] else {
            return nil
        }

        var data = rawData.values.compactMap(EntryType.init(from:))
        if let sorter = self.sorter {
            data = data.sorted(by: sorter)
        }
        print("Received entries: ", data)
        return data
    }

    override func store(data: [EntryType]) {
        self.entries = data
    }
}
