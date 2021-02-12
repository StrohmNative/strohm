import SwiftUI

struct JournalEntryDetail: View {
    static let createdFormatter: DateFormatter = {
        var f = DateFormatter()
        f.dateStyle = .medium
        f.timeStyle = .short
        return f
    }()

    var entry: JournalEntry
    
    var body: some View {
        ScrollView {
            VStack {
                HStack {
                    Text("Created: ")
                    Text(entry.created, style: .date)
                    Text(entry.created, style: .time)
                    Spacer()
                }.font(.caption).foregroundColor(Color(.secondaryLabel))
                Divider()

                Text(verbatim: entry.text)
                    .frame(maxWidth: .infinity, alignment: .topLeading)
            }
            .padding()
            .frame(maxWidth: .infinity)
        }
        .navigationTitle(Text(verbatim: entry.title))
    }
}

struct JournalEntryDetail_Previews: PreviewProvider {
    static var previews: some View {
        let previewEntry1 = JournalEntry(
            id: 1,
            title: "Lorem Ipsum",
            text: "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
            created: Date())
        let previewEntry2 = JournalEntry(id: 1, title: "Title 1", text: "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", created: Date(timeIntervalSinceNow: -60000))
        let previewEntry3 = JournalEntry(id: 2, title: "Title 2", text: "Text 2", created: Date(timeIntervalSinceNow: -10000))
        Group {
            NavigationView {
                JournalEntryDetail(entry: previewEntry1)
            }
            NavigationView {
                JournalEntryDetail(entry: previewEntry2)
            }
            NavigationView {
                JournalEntryDetail(entry: previewEntry3)
            }
        }
    }
}
