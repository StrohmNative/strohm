import Foundation

open class SimpleViewModel<EntryType: ConstructableFromDictionary>: ViewModelBase<EntryType>, PropsHandler {

    override func propsToData(props: Props) -> EntryType? {
        guard let rawData = props[self.propName] as? [String:Any],
           let data = EntryType.init(from: rawData) else {
            return nil
        }

        print("Received entry: ", data)
        return data
    }
}
