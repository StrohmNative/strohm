import Foundation
import Strohm

struct JournalEntry: Identifiable, ConstructableFromDictionary, ConvertableToDictionary {
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

    init?(from dict: [String:Any]) {
        guard let id = dict["entry/id"] as? String,
           let title = dict["entry/title"] as? String,
           let text = dict["entry/text"] as? String,
           let created = dict["entry/created"] as? Double else { return nil }

        self.id = id
        self.title = title
        self.text = text
        self.created = Date(timeIntervalSince1970: created / 1000.0)
    }

    func toDict() -> [String:Any] {
        return [
            "entry/id": id,
            "entry/title": title,
            "entry/text": text,
            "entry/created": created.timeIntervalSince1970 * 1000
        ]
    }
}
