import Foundation
import WebKit

class JsonComms: NSObject, WKScriptMessageHandler {
    func userContentController(_ userContentController: WKUserContentController,
                               didReceive message: WKScriptMessage) {
        print("jsToSwift", message.name, message.body)
    }

    func encode(object: [String: Any]) -> String? {
        guard let data = try? JSONSerialization.data(withJSONObject: object),
            let args = String(data: data, encoding: .utf8) else {
                return nil
        }
        let encoded = args.replacingOccurrences(of: "\"", with: "\\\"")
        return encoded
    }
}
