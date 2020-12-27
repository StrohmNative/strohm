import Foundation
import WebKit
import SwiftUI

struct WebViewWrapper: UIViewRepresentable {
    func makeUIView(context: Context) -> WKWebView {
        Strohm.default.install(appJsPath: "main.js")
        return Strohm.default.webView!
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {

    }

    typealias UIViewType = WKWebView
}

class Strohm: NSObject, WKNavigationDelegate, WKScriptMessageHandler {
    static var `default` = Strohm()

    var webView: WKWebView?
    var webConfiguration: WKWebViewConfiguration!
    var status: Status = .uninitialized

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
        webConfiguration = WKWebViewConfiguration()
        webConfiguration.userContentController.add(self, name: "jsToSwift")

        webView = WKWebView(frame: .zero, configuration: webConfiguration)
        webView?.navigationDelegate = self

        #if DEBUG
        let port = Strohm.determinePort(port: port,
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
            <script src="https://localhost:\(port)/\(appJsPath)"></script>
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

        webView?.loadHTMLString(myHtml, baseURL: Bundle.main.resourceURL)
    }

    public func subscribe(propsSpec: PropsSpec,
                          handler: @escaping HandlerFunction,
                          completion: @escaping (UUID) -> Void) {
        addSubscriber(propsSpec: propsSpec, handler: handler, completion: completion)
    }

    public func unsubscribe(subscriptionId: UUID) {
        removeSubscriber(subscriptionId: subscriptionId)
    }

    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        webView.evaluateJavaScript("Object.getOwnPropertyNames(strohm.store)") { (result, error) in
            print(result)
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
        effectuatePendingSubscriptions()
    }

    func loadingFailed() {
        self.status = .serverNotRunning
        print("Please make sure dev server is running")
    }

    func webView(_ webView: WKWebView,
                 didReceive challenge: URLAuthenticationChallenge,
                 completionHandler: @escaping (URLSession.AuthChallengeDisposition, URLCredential?) -> Void) {
        if challenge.protectionSpace.host == "localhost",
            challenge.protectionSpace.protocol == "https",
            let serverTrust = challenge.protectionSpace.serverTrust {
            let credential = URLCredential(trust: serverTrust)
            completionHandler(.useCredential, credential)
        } else {
            completionHandler(.performDefaultHandling, nil)
        }
    }

    func userContentController(_ userContentController: WKUserContentController,
                               didReceive message: WKScriptMessage) {
        print("jsToSwift", message.name, message.body)
    }

    enum Status {
        case uninitialized
        case serverNotRunning
        case ok
    }
}

public typealias PropsSpec = [String: String]
public typealias Props = [String: Any]
public typealias HandlerFunction = (Props) -> Void
