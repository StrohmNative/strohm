import Foundation
import JavaScriptCore
import Combine

public class Strohm: NSObject {
    public static var `default`: Strohm = {
        let strohm = Strohm()
        strohm.install(appJsPath: "main.js")
        return strohm
    }()

    var context: StrohmJSContext?
    var _status = CurrentValueSubject<Status, Never>(.uninitialized)
    public lazy var status = _status.eraseToAnyPublisher()
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

    func defaultExceptionHandler(context: JSContext?, value: JSValue?) {
        print("\nStrohm error: JavaScript exception: ", value as Any)
        if let v = value {
            context?.exception = v
        }
    }

    public func install(appJsPath: String, port: Int? = nil) {
        self.subscriptions = Subscriptions(strohm: self)
        self.statePersister = StatePersister(strohm: self)

        self.appJsPath = appJsPath
        self.port = port

        var c: StrohmJSContext = JSContext()
        c.exceptionHandler = defaultExceptionHandler

        let postMessageBlock = unsafeBitCast(JsonComms.postMessageBlock, to: AnyObject.self)
        c.setObject(postMessageBlock, forKeyedSubscript: "postMessage" as NSCopying & NSObjectProtocol)

        self.reload(context: c)
    }

    func determineScriptUrlDebug() -> (String, URL)? {
        guard let appJsPath = self.appJsPath else { return nil }
        var devhost = "localhost"

        #if !targetEnvironment(simulator)
        if let devhostFile = Bundle.main.url(forResource: "devhost", withExtension: "txt"),
           let contents = try? String(contentsOf: devhostFile) {
            devhost = contents.trimmingCharacters(in: .whitespacesAndNewlines) + ".local"
        } else {
            print("\nStrohm error: You don't seem to have configured the Stohm Dev Support shell script build phase. Please add it to make things work.\n") // TODO: ref to doc
        }
        #endif

        let port = Strohm.determinePort(port: self.port,
                                        env: ProcessInfo().environment)
        let scriptUrl = URL(string: "http://\(devhost):\(port)/\(appJsPath)")!
        guard let script = (try? String(contentsOf: scriptUrl)) else {
            return nil
        }
        return (script, scriptUrl)
    }

    func determineScriptUrlRelease() -> (String, URL)? {
        guard let scriptUrl = Bundle.main.url(forResource: "main", withExtension: "js"),
              let script = try? String(contentsOf: scriptUrl) else {
            print("\nStrohm error: JavaScript bundle main.js was not found; did you add it to the Xcode project?\n") // TODO: ref to doc
            return nil
        }
        return (script, scriptUrl)
    }

    func determineScriptUrl() -> (String, URL)? {
        #if DEBUG
        return determineScriptUrlDebug()
        #else
        return determineScriptUrlRelease()
        #endif
    }

    public func reload() {
        guard let ctx = self.context else {
            print("\nStrohm error: reload requested before initialization completed")
            return
        }
        DispatchQueue.main.async { [weak self] in
            self?.reload(context: ctx)
        }
    }

    func reload(context ctx: StrohmJSContext) {
        _status.value = .loading
        if let initialState = statePersister?.loadState() {
            ctx.setObject(initialState, forKeyedSubscript: "strohmPersistedState" as NSCopying & NSObjectProtocol)
        }
        guard let (script, scriptUrl) = determineScriptUrl() else {
            print("\nStrohm: load failed; please fix any errors displayed above and restart app.")
            return
        }
        _ = ctx.evaluateScript("globalThis.document = globalThis; globalThis.window = {location: {origin: \"\(scriptUrl)\"}};")
        _ = ctx.evaluateScript(script, withSourceURL: scriptUrl)
        didLoad(context: ctx)
    }

    public func subscribe(propsSpec: PropsSpec,
                          handler: @escaping HandlerFunction,
                          completion: @escaping (UUID) -> Void) {
        self.subscriptions?.addSubscriber(propsSpec: propsSpec, handler: handler, completion: completion)
    }

    public func unsubscribe(subscriptionId: UUID) {
        self.subscriptions?.removeSubscriber(subscriptionId: subscriptionId)
    }

    func didLoad(context ctx: StrohmJSContext) {
//        print(ctx.evaluateScript("Object.getOwnPropertyNames(globalThis.strohm.native$)") as Any)

        self.context = ctx
        if let hasStrohmValue = ctx.evaluateScript("globalThis.hasOwnProperty('strohm')"), hasStrohmValue.toBool() {
            self.loadingFinished()
        } else {
            self.loadingFailed()
        }
    }

    func loadingFinished() {
        self._status.value = .ok
        self.subscriptions?.effectuatePendingSubscriptions()
    }

    func loadingFailed() {
        self._status.value = .serverNotRunning
        print("\nStrohm error: Please make sure dev server is running\n") // TODO: ref to doc
    }

    func call(method: String) {
        guard let ctx = context else {
            print("\nStrohm error: method call requested before initialization completed: \(method)")
            return
        }
        let result = ctx.evaluateScript(method) // TODO: every called function should have a return value?
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

    public enum Status: String {
        case uninitialized = "uninitialized"
        case loading = "loading"
        case serverNotRunning = "server not running"
        case ok = "ok"
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
