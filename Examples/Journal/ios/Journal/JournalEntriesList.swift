import SwiftUI
import Strohm

struct JournalEntriesList: View {
    @ObservedObject var viewModel: ViewModel = ViewModel()

    var body: some View {
        NavigationView {
            List() {
                ForEach(viewModel.data, id: \.id) { entry in
                    NavigationLink(destination: JournalEntryDetail(entry: entry)) {
                        JournalEntryRow(entry: entry)
                    }
                }.onDelete(perform: onDelete)
            }
            .navigationTitle(Text("Journal"))
            .navigationBarItems(trailing: Button("New", action: {
                Strohm.default.dispatch(type: "new-entry")
            }))
        }
    }

    func onDelete(at offsets: IndexSet) {
        print("onDelete: \(offsets)")
        let ids = offsets.map { viewModel.data[$0].id }
        Strohm.default.dispatch(type: "remove-entry", payload: ["entry/id": ids[0]])
    }

    final class ViewModel: KeyedArrayViewModel<JournalEntry> {
        init(entries: [JournalEntry] = []) {
            super.init(initialData: entries,
                       propName: "entries",
                       propPath: ["entries"])
            sorter = { (e1, e2) -> Bool in
                e1.created > e2.created
            }
        }
    }

}

struct JournalEntriesList_Previews: PreviewProvider {
    static var previews: some View {
        let entries = [
            JournalEntry(id: "1", title: "Title 1", text: "Text 1", created: Date(timeIntervalSinceNow: -60000)),
            JournalEntry(id: "2", title: "Title 2", text: "Text 2", created: Date(timeIntervalSinceNow: -10000)),
            JournalEntry(id: "3", title: "Title 3", text: "Text 3", created: Date())
        ]
        JournalEntriesList(viewModel: JournalEntriesList.ViewModel(entries: entries))
    }
}
