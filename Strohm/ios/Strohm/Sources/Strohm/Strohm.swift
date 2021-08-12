import Foundation
import JavaScriptCore

public class Strohm: NSObject {
    public static var `default`: Strohm = {
        let strohm = Strohm()
        strohm.install(appJsPath: "main.js")
        return strohm
    }()

    var context: StrohmJSContext?
    var status: Status = .uninitialized
    var appJsPath: String?
    var port: Int?
    var comms = JsonComms.shared
    var subscriptions: Subscriptions?
    var statePersister: StatePersister?

    static func determinePort(port: Int?, env: [String: String]) -> Int {
        if let portString = env["DEVSERVER_PORT"],
            let portInt = Int(portString),
            port == nil {
            return portInt
        } else {
            return port ?? 8080
        }
    }

    public func install(appJsPath: String, port: Int? = nil) {
        self.subscriptions = Subscriptions(strohm: self)
        self.statePersister = StatePersister(strohm: self)

        self.appJsPath = appJsPath
        self.port = port

        context = JSContext()
        context!.exceptionHandler = { (context, value) in
            print("Exception: ", value as Any)
            if let v = value {
                context?.exception = v
            }
        }

        let postMessageBlock = unsafeBitCast(JsonComms.postMessageBlock, to: AnyObject.self)
        context!.setObject(postMessageBlock, forKeyedSubscript: "postMessage" as NSCopying & NSObjectProtocol)

        self.reload()
    }

    public func reload() {
        if let initialState = statePersister?.loadState() {
            context!.setObject(initialState, forKeyedSubscript: "strohmPersistedState" as NSCopying & NSObjectProtocol)
        }
        #if DEBUG
            guard let appJsPath = self.appJsPath else { return }
            let devhost: String
            #if !targetEnvironment(simulator)
                if let devhostFile = Bundle.main.url(forResource: "devhost", withExtension: "txt"),
                   let contents = try? String(contentsOf: devhostFile) {
                    devhost = contents.trimmingCharacters(in: .whitespacesAndNewlines) + ".local"
                } else {
                    print("\nStrohm error: You don't seem to have configured the Stohm Dev Support shell script build phase. Please add it to make things work.\n") // TODO: ref to doc
                    devhost = "localhost"
                }
            #else
                devhost = "localhost"
            #endif
            let port = Strohm.determinePort(port: self.port,
                                            env: ProcessInfo().environment)
            let scriptUrl = URL(string: "http://\(devhost):\(port)/\(appJsPath)")!
            _ = context!.evaluateScript("globalThis.document = globalThis; globalThis.window = {location: {origin: \"\(scriptUrl)\"}};")
            let script = (try? String(contentsOf: scriptUrl))!
            _ = context!.evaluateScript(script, withSourceURL: scriptUrl)
            didLoad()

        #else

            guard let mainJSURL = Bundle.main.url(forResource: "main", withExtension: "js"),
                  let script = try? String(contentsOf: mainJSURL) else {
                print("\nStrohm error: JavaScript bundle main.js was not found; did you add it to the Xcode project?\n") // TODO: ref to doc
                return
            }
            _ = context!.evaluateScript("globalThis.document = globalThis; globalThis.window = {location: {origin: \"\(mainJSURL)\"}};")
            _ = context!.evaluateScript(script, withSourceURL: mainJSURL)
            didLoad()

        #endif
    }

    public func subscribe(propsSpec: PropsSpec,
                          handler: @escaping HandlerFunction,
                          completion: @escaping (UUID) -> Void) {
        self.subscriptions?.addSubscriber(propsSpec: propsSpec, handler: handler, completion: completion)
    }

    public func unsubscribe(subscriptionId: UUID) {
        self.subscriptions?.removeSubscriber(subscriptionId: subscriptionId)
    }

    func didLoad() {
//        print(context!.evaluateScript("Object.getOwnPropertyNames(globalThis.strohm.native$)") as Any)

        if let hasStrohmValue = context!.evaluateScript("globalThis.hasOwnProperty('strohm')"), hasStrohmValue.toBool() {
            self.loadingFinished()
        } else {
            self.loadingFailed()
        }
    }

    func loadingFinished() {
        self.status = .ok
        self.subscriptions?.effectuatePendingSubscriptions()
    }

    func loadingFailed() {
        self.status = .serverNotRunning
        print("\nStrohm error: Please make sure dev server is running\n") // TODO: ref to doc
    }

    func call(method: String) {
        let result = context!.evaluateScript(method) // TODO: every called function should have a return value?
        print("cljs call result: \(String(describing: result))")
    }

    public func dispatch(type: String, payload: ConvertableToDictionary) {
        dispatch(type: type, payload: payload.toDict())
    }

    public func dispatch(type: String, payload: [String: Any] = [:]) {
        print("dispatch", type, payload)
        let action: [String: Any] = ["type": type, "payload": payload]
        guard let serializedAction = comms.encode(object: action) else {
            return
        }
        let method = "globalThis.strohm.native$.dispatch_from_native(\"\(serializedAction)\")"
        call(method: method)
    }

    enum Status {
        case uninitialized
        case serverNotRunning
        case ok
    }
}

public typealias PropName = String
public typealias PropPath = [Any]
public typealias PropsSpec = [PropName: PropPath]
public typealias Props = [PropName: Any]
public typealias HandlerFunction = (Props) -> Void

protocol StrohmJSContext {
    var exceptionHandler: ((JSContext?, JSValue?) -> Void)! { get set }
    func evaluateScript(_ script: String!) -> JSValue!
    func evaluateScript(_ script: String!, withSourceURL sourceURL: URL!) -> JSValue!
    func setObject(_ object: Any!, forKeyedSubscript key: (NSCopying & NSObjectProtocol)!)
}

extension JSContext: StrohmJSContext {}
