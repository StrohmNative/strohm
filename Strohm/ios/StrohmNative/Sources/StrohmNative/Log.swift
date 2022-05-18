import Foundation

class Log {
    static func debug(_ args: Any...) {
        Log.log(level: .debug, args)
    }

    static func info(_ args: Any...) {
        Log.log(level: .info, args)
    }

    static func warn(_ args: Any...) {
        Log.log(level: .warn, args)
    }

    static func error(_ args: Any...) {
        Log.log(level: .error, args)
    }

    static private func log(level: Level, _ args: [Any]) {
        print("[\(level)]", args)

        let argsWithLevel: [Any] = ["\(level)"] + args
        guard let encodedArgs = StrohmNative.default.comms.encode(object: argsWithLevel) else {
            print("[error] Failed to encode log args", argsWithLevel)
            return
        }

        let encoded = encodedArgs.replacingOccurrences(of: "\"", with: "\\\"")
        StrohmNative.default.call(method: "globalThis.strohm_native.log.log_from_native(\"\(encoded)\")") { cljsError in
            print("[fatal] Call to log error in cljs failed with error \(cljsError)")
        }
    }

    enum Level: String {
        case debug
        case info
        case warn
        case error
    }
}
