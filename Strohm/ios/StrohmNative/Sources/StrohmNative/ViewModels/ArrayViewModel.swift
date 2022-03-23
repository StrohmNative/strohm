import Foundation

open class ArrayViewModel<EntryType: Codable>: ViewModelBase<[EntryType]> {
    public var sorter: ((EntryType, EntryType) -> Bool)?

    override func propsToData2(serializedProps: String) -> [EntryType]? {
        guard let rawData = serializedProps.data(using: .utf8) else {
            return nil
        }

        do {
            let data = try decoder().decode(PropEnvelope<[EntryType]>.self, from: rawData)
            let entries = [EntryType](data.propValue)

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
}
