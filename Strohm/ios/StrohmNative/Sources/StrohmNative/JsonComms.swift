import Foundation
import WebKit

public typealias CommsArguments = [String:Any]
public typealias CommsFunction = (CommsArguments) -> Void

class JsonComms: NSObject, WKScriptMessageHandler {
    var registeredFunctions = [String: CommsFunction]()

    func registerHandlerFunction(name: String, function: @escaping CommsFunction) {
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
        guard let data = try? JSONSerialization.data(withJSONObject: object, options: .withoutEscapingSlashes),
            let args = String(data: data, encoding: .utf8) else {
                return nil
        }
        return args
    }

    func encode(object: (String, Any)) -> String? {
        let asArray = [object.0, object.1]
        guard let data = try? JSONSerialization.data(withJSONObject: asArray, options: .withoutEscapingSlashes),
            let args = String(data: data, encoding: .utf8) else {
                return nil
        }
        return args
    }

    func encode(object: [Any]) -> String? {
        guard let data = try? JSONSerialization.data(withJSONObject: object, options: .withoutEscapingSlashes),
            let args = String(data: data, encoding: .utf8) else {
                return nil
        }
        return args
    }
}
