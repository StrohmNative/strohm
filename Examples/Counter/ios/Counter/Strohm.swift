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

class Strohm: NSObject, WKNavigationDelegate {
    static var `default` = Strohm()

    var webView: WKWebView?
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
        let port = Strohm.determinePort(port: port,
                                          env: ProcessInfo().environment)
        webView = WKWebView()
        webView?.navigationDelegate = self
        let myHtml = """
        <html>
        <body style='background-color: #ddd;font-size: 200%'>
        <script src="http://localhost:\(port)/\(appJsPath)"></script>
        </body>
        </html>
        """
        webView?.loadHTMLString(myHtml, baseURL: nil)
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

    enum Status {
        case uninitialized
        case serverNotRunning
        case ok
    }
}

public typealias PropsSpec = [String: String]
public typealias Props = [String: Any]
public typealias HandlerFunction = (Props) -> Void
