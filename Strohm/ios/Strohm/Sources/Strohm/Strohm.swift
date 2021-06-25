import Foundation
import WebKit

public class Strohm: NSObject, WKNavigationDelegate {
    public static var `default`: Strohm = {
        let strohm = Strohm()
        strohm.install(appJsPath: "main.js")
        return strohm
    }()

    var webView: StrohmWebView?
    var webConfiguration: WKWebViewConfiguration!
    var status: Status = .uninitialized
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
        #if DEBUG
        guard let appJsPath = self.appJsPath else { return }
        let devhost: String
        #if !targetEnvironment(simulator)
        if let devhostFile = Bundle.main.url(forResource: "devhost", withExtension: "txt"),
           let contents = try? String(contentsOf: devhostFile) {
            devhost = contents + ".local"
        } else {
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
            <script type="text/javascript">
                window.onload = function(e) {
                    document.getElementById('content').innerHTML += 'onload<br />'
                    globalThis.app.main.init()
                }
            </script>
            <script src="http://\(devhost):\(port)/\(appJsPath)"></script>
        </body>
        </html>
        """
        #else
        let mainJSURL = Bundle.main.url(forResource: "main", withExtension: "js")!
        let jsUrlString = mainJSURL.absoluteString
        let myHtml = """
        <html>
        <body style='background-color: #ddd;font-size: 200%'>
            <h1>Hi!</h1><div id='content'></div>
            <script type="text/javascript">
                window.onload = function(e) {
                    document.getElementById('content').innerHTML += 'onload<br />'
                    globalThis.app.main.init()
                }
            </script>
            <script src="\(jsUrlString)"></script>
        </body>
        </html>
        """
        #endif

        _ = webView?.loadHTMLString(myHtml, baseURL: Bundle.main.resourceURL)
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
        self.status = .ok
        self.subscriptions?.effectuatePendingSubscriptions()
    }

    func loadingFailed() {
        self.status = .serverNotRunning
        print("Please make sure dev server is running")
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
        print(method)
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

protocol StrohmWebView {
    var navigationDelegate: WKNavigationDelegate? { get set }
    func loadHTMLString(_ string: String, baseURL: URL?) -> WKNavigation?
    func evaluateJavaScript(_ javaScriptString: String, completionHandler: ((Any?, Error?) -> Void)?)
}

extension WKWebView: StrohmWebView {}
