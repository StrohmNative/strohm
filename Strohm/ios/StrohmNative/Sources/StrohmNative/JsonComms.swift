import Foundation
import WebKit

class JsonComms: NSObject, WKScriptMessageHandler {
    typealias Arguments = [String:Any]
    typealias Function = (Arguments) -> Void

    var registeredFunctions = [String: Function]()

    func registerHandlerFunction(name: String, function: @escaping Function) {
        registeredFunctions[name] = function
    }

    func userContentController(_ userContentController: WKUserContentController,
                               didReceive message: WKScriptMessage) {
        print("didReceiveMessage", message.name /*, message.body*/)
        if let arguments = message.body as? [String:Any],
           let functionName = arguments["function"] as? String,
           let function = registeredFunctions[functionName] {
            function(arguments)
        }
    }

    func encode(object: [String: Any]) -> String? {
        guard let data = try? JSONSerialization.data(withJSONObject: object),
            let args = String(data: data, encoding: .utf8) else {
                return nil
        }
        let encoded = args.replacingOccurrences(of: "\"", with: "\\\"")
        return encoded
    }

    func encode(object: (String, Any)) -> String? {
        let asArray = [object.0, object.1]
        guard let data = try? JSONSerialization.data(withJSONObject: asArray),
            let args = String(data: data, encoding: .utf8) else {
                return nil
        }
        let encoded = args.replacingOccurrences(of: "\"", with: "\\\"")
        return encoded
    }

    func encode(object: [Any]) -> String? {
        guard let data = try? JSONSerialization.data(withJSONObject: object),
            let args = String(data: data, encoding: .utf8) else {
                return nil
        }
        let encoded = args.replacingOccurrences(of: "\"", with: "\\\"")
        return encoded
    }
}
