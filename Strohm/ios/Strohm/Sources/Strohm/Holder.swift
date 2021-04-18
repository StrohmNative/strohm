import SwiftUI
import WebKit

public struct StrohmHolder: UIViewRepresentable {
    public init() {}

    public func makeUIView(context: Context) -> WKWebView {
        return Strohm.default.webView! as! WKWebView
    }

    public func updateUIView(_ uiView: WKWebView, context: Context) {}
}
