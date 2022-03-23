import Foundation
import StrohmNative

struct JournalEntry: Identifiable, Codable {
    let id: String
    var title: String
    var text: String
    let created: Date

    init(id: String, title: String, text: String, created: Date) {
        self.id = id
        self.title = title
        self.text = text
        self.created = created
    }

    enum CodingKeys: String, CodingKey {
        case id = "entry/id"
        case title = "entry/title"
        case text = "entry/text"
        case created = "entry/created"
    }
}
