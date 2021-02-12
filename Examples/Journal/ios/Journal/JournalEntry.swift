import Foundation

struct JournalEntry: Identifiable {
    let id: Int
    let title: String
    let text: String
    let created: Date
}
