import Foundation

open class SimpleViewModel<EntryType: ConstructableFromDictionary>: ViewModelBase<EntryType>, PropsHandler {

    public var data: EntryType {
        willSet {
            // https://stackoverflow.com/questions/57615920/published-property-wrapper-not-working-on-subclass-of-observableobject
            self.objectWillChange.send()
        }
    }

    public init(initialData: EntryType, propName: PropName, propPath: PropPath) {
        self.data = initialData
        super.init(propName: propName, propPath: propPath)
    }

    override func propsToData(props: Props) -> EntryType? {
        guard let rawData = props[self.propName] as? [String:Any],
           let data = EntryType.init(from: rawData) else {
            return nil
        }

        print("Received entry: ", data)
        return data
    }

    override func store(data: EntryType) {
        self.data = data
    }
}
