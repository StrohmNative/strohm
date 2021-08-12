import Foundation

@objc class JsonComms: NSObject {
    static let shared = JsonComms()
    private override init() {}

    typealias Arguments = [String:Any]
    typealias Function = (Arguments) -> Void

    var registeredFunctions = [String: Function]()

    func registerHandlerFunction(name: String, function: @escaping Function) {
        registeredFunctions[name] = function
    }

    static let postMessageBlock: @convention(block) ([String:Any]) -> Void = { arguments in
        DispatchQueue.main.async {
            shared.messagePosted(arguments: arguments)
        }
    }

    @objc func messagePosted(arguments: [String:Any]) {
//        print("messagePosted", arguments)
        if let functionName = arguments["function"] as? String,
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
}
