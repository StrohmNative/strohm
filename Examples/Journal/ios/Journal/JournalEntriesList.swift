import SwiftUI
import Strohm

struct JournalEntriesList: View {
    @ObservedObject var viewModel: ViewModel = ViewModel()

    var body: some View {
        NavigationView {
            List(viewModel.entries) { entry in
                NavigationLink(destination: JournalEntryDetail(entry: entry)) {
                    JournalEntryRow(entry: entry)
                }
            }
            .navigationTitle(Text("Journal"))
        }
    }

    class ViewModel: ArrayViewModel<JournalEntry> {
        init(entries: [JournalEntry] = []) {
            super.init(initialData: entries, propName: "entries", propPath: ["entries"])
        }
    }

}

struct JournalEntriesList_Previews: PreviewProvider {
    static var previews: some View {
        let entries = [
            JournalEntry(id: 1, title: "Title 1", text: "Text 1", created: Date(timeIntervalSinceNow: -60000)),
            JournalEntry(id: 2, title: "Title 2", text: "Text 2", created: Date(timeIntervalSinceNow: -10000)),
            JournalEntry(id: 3, title: "Title 3", text: "Text 3", created: Date())
        ]
        JournalEntriesList(viewModel: JournalEntriesList.ViewModel(entries: entries))
    }
}

