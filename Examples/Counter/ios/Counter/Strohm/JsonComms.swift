import Foundation
import WebKit

class JsonComms: NSObject, WKScriptMessageHandler {
    func userContentController(_ userContentController: WKUserContentController,
                               didReceive message: WKScriptMessage) {
        print("jsToSwift", message.name, message.body)
    }
}
