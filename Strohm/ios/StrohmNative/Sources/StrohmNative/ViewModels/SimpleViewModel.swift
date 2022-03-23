import Foundation

open class SimpleViewModel<EntryType: Decodable>: ViewModelBase<EntryType> {

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

    public required init(constant: EntryType) {
        self.data = constant
        super.init()
    }

    override func propToData2(serializedProp: String) -> EntryType? {
        guard let rawData = serializedProp.data(using: .utf8) else {
            return nil
        }

        do {
            let data = try decoder().decode(PropEnvelope<EntryType?>.self, from: rawData)
            let value = data.propValue
            print("Received entry: ", value)
            return value
        }
        catch let e {
            Log.error(String(describing: e))
            return nil
        }
    }

    override func store(data: EntryType) {
        self.data = data
    }

    public static func constant(_ data: EntryType) -> Self {
        return .init(constant: data)
    }
}
