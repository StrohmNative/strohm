import SwiftUI

struct JournalEntriesList: View {
    var entries: [JournalEntry]

    var body: some View {
        NavigationView {
            List(entries) { entry in
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
        JournalEntriesList(entries: entries)
    }
}
