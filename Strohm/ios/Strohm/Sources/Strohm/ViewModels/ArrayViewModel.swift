import Foundation

open class ArrayViewModel<EntryType: ConstructableFromDictionary>: ViewModelBase<[EntryType]> {

    override func propsToData(props: Props) -> [EntryType]? {
        guard let rawData = props[self.propName] as? [[String:Any]] else {
            return nil
        }

        let data = rawData.compactMap(EntryType.init(from:))
        print("Received entries: ", data)
        return data
    }
}
