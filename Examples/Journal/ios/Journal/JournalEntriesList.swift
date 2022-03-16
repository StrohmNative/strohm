import SwiftUI
import StrohmNative
import UIKit

struct JournalEntriesList: View {
    typealias ViewModel = KeyedArrayViewModel<JournalEntry>
    @StateObject var viewModel: ViewModel

    init(entries: [JournalEntry] = []) {
        let vm = ViewModel(initialEntries: entries,
                           propName: "entries",
                           propPath: ["entries"])
        vm.sorter = { (e1, e2) -> Bool in
            e1.created > e2.created
        }
        _viewModel = StateObject(wrappedValue: vm)
    }

    var body: some View {
        NavigationView {
            List() {
                ForEach(viewModel.entries, id: \.id) { entry in
                    NavigationLink(destination: JournalEntryDetail(entry: entry)) {
                        JournalEntryRow(entry: entry)
                    }
                }.onDelete(perform: onDelete)
            }
            .listStyle(PlainListStyle())
            .navigationTitle(Text("Journal"))
            .navigationBarItems(trailing: Button("New", action: {
                StrohmNative.default.dispatch(type: "new-entry")
            }))
        }
    }

    func onDelete(at offsets: IndexSet) {
        print("onDelete: \(offsets)")
        let ids = offsets.map { viewModel.entries[$0].id }
        try! StrohmNative.default.dispatch(type: "remove-entry", payload: ["entry/id": ids[0]])
    }
}

struct JournalEntriesList_Previews: PreviewProvider {
    static var previews: some View {
        let entries = [
            JournalEntry(id: "1", title: "Title 1", text: "Text 1", created: Date(timeIntervalSinceNow: -60000)),
            JournalEntry(id: "2", title: "Title 2", text: "Text 2", created: Date(timeIntervalSinceNow: -10000)),
            JournalEntry(id: "3", title: "Title 3", text: "Text 3", created: Date())
        ]
        JournalEntriesList(entries: entries)
    }
}
