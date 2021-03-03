import SwiftUI
import Strohm

struct JournalEntryDetail: View {
    static let createdFormatter: DateFormatter = {
        var f = DateFormatter()
        f.dateStyle = .medium
        f.timeStyle = .short
        return f
    }()

    @State var editMode: Bool = false
    let entry: JournalEntry
    @State var editableText: String
    @Environment(\.presentationMode) var presentationMode

    init(entry: JournalEntry) {
        self.entry = entry
        _editableText = State(initialValue: entry.text)
    }

    var body: some View {
        VStack {
            HStack {
                Text("Created: ")
                Text(entry.created, style: .date)
                Text(entry.created, style: .time)
                Spacer()
            }
            .font(.caption)
            .foregroundColor(Color(.secondaryLabel))

            Divider()

            TextEditor(text: $editableText)
        }
        .padding([.top, .leading, .trailing], nil)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .navigationTitle(Text(verbatim: entry.title))
        .navigationBarItems(
            trailing: Button("Save", action: onSave))
    }

    func onSave() {
        self.editMode = false
        var updatedEntry = entry
        updatedEntry.text = editableText
        Strohm.default.dispatch(type: "update-entry", payload: updatedEntry)
        self.presentationMode.wrappedValue.dismiss()
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
