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

        StrohmNative.default.call(method: "globalThis.strohm_native.log.log_from_native(\"\(encodedArgs)\")")
    }

    enum Level: String {
        case debug
        case info
        case warn
        case error
    }
}
