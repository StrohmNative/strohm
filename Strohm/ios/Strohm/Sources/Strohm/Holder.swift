import SwiftUI
import WebKit

public struct StrohmHolder: UIViewRepresentable {
    public init() {}

    public func makeUIView(context: Context) -> WKWebView {
        Strohm.default.install(appJsPath: "main.js")
        return Strohm.default.webView!
    }

    public func updateUIView(_ uiView: WKWebView, context: Context) {}
}
