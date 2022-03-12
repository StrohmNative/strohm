import Foundation

open class KeyedArrayViewModel<EntryType: ConstructableFromDictionary & Codable>: ViewModelBase<[EntryType]> {
    public var sorter: ((EntryType, EntryType) -> Bool)?

    public var entries: [EntryType] {
        willSet {
            // https://stackoverflow.com/questions/57615920/published-property-wrapper-not-working-on-subclass-of-observableobject
            self.objectWillChange.send()
        }
    }

    public init(initialEntries: [EntryType], propName: PropName, propPath: PropPath,
                sorter: ((EntryType, EntryType) -> Bool)? = nil) {
        self.entries = initialEntries
        self.sorter = sorter
        super.init(propName: propName, propPath: propPath)
    }

    public required init(constantEntries: [EntryType]) {
        self.entries = constantEntries
        super.init()
    }

    override func propsToData(props: Props) -> [EntryType]? {
        guard let rawData = props[self.propName] as? [PropName: Props] else {
            return nil
        }

        var data = rawData.values.compactMap(EntryType.init(from:))
        if let sorter = self.sorter {
            data = data.sorted(by: sorter)
        }
        print("Received entries: ", data.count)
        return data
    }

    override func propsToData2(serializedProps: String) -> [EntryType]? {
        guard let rawData = serializedProps.data(using: .utf8) else {
            return nil
        }

        do {
            let data = try JSONDecoder().decode(PropEnvelope<[String:EntryType]>.self, from: rawData)
            let entries = [EntryType](data.propValue.values)

            print("Received entries: ", entries.count)

            if let sorter = self.sorter {
                return entries.sorted(by: sorter)
            } else {
                return entries
            }
        }
        catch let e {
            Log.error(String(describing: e))
            return nil
        }
    }

    override func store(data: [EntryType]) {
        self.entries = data
    }

    public static func constant(_ entries: [EntryType]) -> Self {
        return .init(constantEntries: entries)
    }
}

struct PropEnvelope<T: Decodable>: Decodable {
    let propName: PropName
    let propValue: T

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        propName = try container.decode(PropName.self, forKey: .propName)
        propValue = try container.decode(T.self, forKey: CodingKeys.make(key: propName))
    }
}

private struct CodingKeys: CodingKey {
    var intValue: Int?
    var stringValue: String

    init?(intValue: Int) { self.intValue = intValue; self.stringValue = "\(intValue)" }
    init?(stringValue: String) { self.stringValue = stringValue }

    static let propName = CodingKeys.make(key: "prop-name")

    static func make(key: String) -> CodingKeys {
        return CodingKeys(stringValue: key)!
    }
}
