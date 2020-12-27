import SwiftUI
import WebKit

struct StrohmHolder: UIViewRepresentable {
    func makeUIView(context: Context) -> WKWebView {
        Strohm.default.install(appJsPath: "main.js")
        return Strohm.default.webView!
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {}
}
