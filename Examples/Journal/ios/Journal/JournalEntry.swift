import Foundation
import Strohm

struct JournalEntry: Identifiable, ConstructableFromDictionary {
    let id: Int
    let title: String
    let text: String
    let created: Date

    init(id: Int, title: String, text: String, created: Date) {
        self.id = id
        self.title = title
        self.text = text
        self.created = created
    }

    init?(from dict: [String:Any]) {
        guard let id = dict["id"] as? Int,
           let title = dict["title"] as? String,
           let text = dict["text"] as? String,
           let created = dict["created"] as? Double else { return nil }

        self.id = id
        self.title = title
        self.text = text
        self.created = Date(timeIntervalSince1970: created)
    }
}
