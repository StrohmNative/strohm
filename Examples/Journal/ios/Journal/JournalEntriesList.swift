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
}

struct JournalEntriesList_Previews: PreviewProvider {
    static var previews: some View {
        let entries = [
            JournalEntry(id: 1, title: "Title 1", text: "Text 1", created: Date(timeIntervalSinceNow: -60000)),
            JournalEntry(id: 2, title: "Title 2", text: "Text 2", created: Date(timeIntervalSinceNow: -10000)),
            JournalEntry(id: 3, title: "Title 3", text: "Text 3", created: Date())
        ]
        JournalEntriesList(viewModel: ViewModel(entries: entries))
    }
}

class ViewModel: ObservableObject {
    var subscriptionId: UUID?
    @Published var entries: [JournalEntry]

    init() {
        entries = []
        Strohm.default.subscribe(
            propsSpec: ["entries": ["entries"]],
            handler: receiveProps) { subscriptionId in
            self.subscriptionId = subscriptionId
        }
    }

    init(entries: [JournalEntry]) {
        self.entries = entries
    }

    deinit {
        if let subscriptionId = self.subscriptionId {
            Strohm.default.unsubscribe(subscriptionId: subscriptionId)
        }
    }

    func receiveProps(props: [String:Any]) {
        if let entries = props["entries"] as? [[String:Any]] {
            print("Received props: ", entries)
            let journalEntries = entries.compactMap(JournalEntry.init(from:))
            print("Received journal entries: ", journalEntries)
            self.entries = journalEntries
        }
    }
}
