import SwiftUI

struct JournalEntryRow: View {
    var entry: JournalEntry

    var body: some View {
        VStack {
            HStack {
                Text(verbatim: entry.title)
                Spacer()
            }
            HStack {
                Text(entry.created, style: .date)
                Text(entry.created, style: .time)
                Spacer()
            }
            .font(.subheadline)
            .foregroundColor(Color(.secondaryLabel))
            .padding(.top, 1)
        }
    }
}

struct JournalEntryRow_Previews: PreviewProvider {
    static var previews: some View {
        let previewEntry = JournalEntry(
            id: "1",
            title: "Lorem Ipsum",
            text: "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
            created: Date())
        JournalEntryRow(entry: previewEntry)
            .previewLayout(.fixed(width: 300, height: 70))
    }
}
