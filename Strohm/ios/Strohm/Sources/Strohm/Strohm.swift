import Foundation
import WebKit
import Combine

public class Strohm: NSObject, WKNavigationDelegate {
    public static var `default`: Strohm = {
        let strohm = Strohm()
        strohm.install(appJsPath: "main.js")
        return strohm
    }()

    var webView: StrohmWebView?
    var webConfiguration: WKWebViewConfiguration!
    var _status = CurrentValueSubject<Status, Never>(.uninitialized)
    public lazy var status = _status.eraseToAnyPublisher()
    var appJsPath: String?
    var port: Int?
    var comms = JsonComms()
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

        webConfiguration = WKWebViewConfiguration()
        webConfiguration.userContentController.add(comms, name: "jsToSwift")

        webView = WKWebView(frame: .zero, configuration: webConfiguration)
        webView?.navigationDelegate = self

        self.reload()
    }

    public func reload() {
        var initialStateVar = ""
        if let initialState = statePersister?.loadState() {
            let escaped = initialState.replacingOccurrences(of: "\"", with: "\\\"")
            initialStateVar = "var strohmPersistedState=\"\(escaped)\";"
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
        let myHtml = """
        <html>
        <body style='background-color: #ddd;font-size: 200%'>
            <h1>Hi!</h1><div id='content'></div>
            <script>\(initialStateVar)</script>
            <script src="http://\(devhost):\(port)/\(appJsPath)"></script>
        </body>
        </html>
        """
        let baseUrl = URL(string: "http://\(devhost):\(port)/")!
        _ = webView?.loadHTMLString(myHtml, baseURL: baseUrl)
        #else
        guard let mainJSURL = Bundle.main.url(forResource: "main", withExtension: "js") else {
            print("\nStrohm error: JavaScript bundle main.js was not found; did you add it to the Xcode project?\n") // TODO: ref to doc
            return
        }
        let jsUrlString = mainJSURL.absoluteString
        let myHtml = """
        <html>
        <body style='background-color: #ddd;font-size: 200%'>
            <h1>Hi!</h1><div id='content'></div>
            <script>\(initialStateVar)</script>
            <script src="\(jsUrlString)"></script>
        </body>
        </html>
        """
        _ = webView?.loadHTMLString(myHtml, baseURL: Bundle.main.resourceURL)
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

    public func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        webView.evaluateJavaScript("Object.getOwnPropertyNames(strohm.native$)") { (result, error) in
            print("Strohm store properties:", result ?? "nil")
        }

        webView.evaluateJavaScript("this.hasOwnProperty('strohm')") { (result, error) in
            guard let returnValue = result as? Int,
                  returnValue == 1,
                  error == nil else {
                self.loadingFailed()
                return
            }
            self.loadingFinished()
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
        webView?.evaluateJavaScript(method) { (result, error) in
            print("cljs call result error: \(String(describing: error))")
        }
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

protocol StrohmWebView {
    var navigationDelegate: WKNavigationDelegate? { get set }
    func loadHTMLString(_ string: String, baseURL: URL?) -> WKNavigation?
    func evaluateJavaScript(_ javaScriptString: String, completionHandler: ((Any?, Error?) -> Void)?)
}

extension WKWebView: StrohmWebView {}
