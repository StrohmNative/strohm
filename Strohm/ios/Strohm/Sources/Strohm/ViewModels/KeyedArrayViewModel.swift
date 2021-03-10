import Foundation

open class KeyedArrayViewModel<EntryType: ConstructableFromDictionary>: ViewModelBase<[EntryType]> {

    override func propsToData(props: Props) -> [EntryType]? {
        guard let rawData = props[self.propName] as? [String: [String:Any]] else {
            return nil
        }

        let data = rawData.values.compactMap(EntryType.init(from:))
        print("Received entries: ", data)
        return data
    }
}
