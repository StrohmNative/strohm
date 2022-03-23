import Foundation
import Quick
import Nimble
import WebKit
@testable import StrohmNative

let userNamepropSpec: (name: String, path: [String]) = ("name", ["user","name"])

class SubscriptionsSpec: QuickSpec {
    override func spec() {
        var strohmNative: StrohmNative!
        var webViewMock: WebViewMock!

        beforeEach {
            strohmNative = StrohmNative()
            strohmNative.install(appJsPath: "")
            webViewMock = WebViewMock()
            strohmNative.webView = webViewMock
        }

        context("when subscribing before load finishes") {
            var subscriptionComplete: Bool = false
            let handlerFn: HandlerFunction = { _ in }

            beforeEach {
                subscriptionComplete = false
                strohmNative.subscribe(propSpec: userNamepropSpec, handler: handlerFn) { _ in
                    subscriptionComplete = true
                }
            }

            it("remembers pending subscrtiption details") {
                expect(strohmNative.subscriptions?.pendingSubscriptions).to(haveCount(1))
            }

            it("is not yet complete") {
                expect(subscriptionComplete).to(beFalse())
            }

            it("has nothing pending after load finishes") {
                strohmNative.whenLoadingFinished()
                expect(strohmNative.subscriptions?.pendingSubscriptions).to(beNil())
            }

            it("calls completion handler after load finishes") {
                strohmNative.whenLoadingFinished()
                expect(subscriptionComplete).to(beTrue())
            }
        }

        context("when subscribing after load finishes") {
            var subscriptionComplete: Bool = false
            let handlerFn: HandlerFunction = { _ in }

            beforeEach {
                strohmNative.whenLoadingFinished()
                subscriptionComplete = false
                strohmNative.subscribe(
                    propSpec: userNamepropSpec,
                    handler: handlerFn
                ) { _ in
                    subscriptionComplete = true
                }
            }

            it("is complete") {
                expect(subscriptionComplete).to(beTrue())
            }

            it("has called subscribe on the web view") {
                expect(webViewMock.evaluatedJavaScript).to(haveCount(1))
                let actual = webViewMock.evaluatedJavaScript.first!
                let pattern = #"strohm_native\.flow\.subscribe_from_native\(".*", ?"(.*)"\)"#
                expect(actual).to(match(pattern))
                let regex = try! NSRegularExpression(pattern: pattern, options: [])
                let range = NSRange(actual.startIndex..<actual.endIndex, in: actual)
                let matches = regex.matches(in: actual, options: [], range: range)
                let capturedRange = Range(matches[0].range(at: 1), in: actual)!
                let matchedSerializedPropSpec = actual[capturedRange]
                let unescaped = matchedSerializedPropSpec.replacingOccurrences(of: "\\\"", with: "\"")
                let parsed = try! JSONSerialization.jsonObject(
                    with: unescaped.data(using: .utf8)!,
                    options: []) as! [Any]
                expect(parsed[0] as? String) == userNamepropSpec.name
                expect(parsed[1] as? [String]) == userNamepropSpec.path
//                expect(parsed["city"]) == propSpec["city"]
            }
        }

        context("when subscribed") {
            var receivedProp: Prop?
            var subscriptionId: UUID?
            let handlerFn: HandlerFunction = { prop in
                receivedProp = prop
            }

            beforeEach {
                receivedProp = nil
                subscriptionId = nil
                strohmNative.whenLoadingFinished()
                subscriptionId = strohmNative.whenSubscriptionCompletes(userNamepropSpec, handlerFn)
            }

            it("receives prop updates") {
                let prop = ("name", "foo")
                strohmNative.whenIncoming(prop: prop, for: subscriptionId!)
                expect(receivedProp as? (String, String)) == prop
            }

            it("does not receive prop for someone else") {
                var otherProp: Prop?
                let otherId = strohmNative.whenSubscriptionCompletes(userNamepropSpec) { prop in
                    otherProp = prop
                }
                strohmNative.whenIncoming(prop: ("name", "foo"), for: otherId)
                expect(receivedProp).to(beNil())
                expect(otherProp).toNot(beNil())
            }

            it("does not receive prop after unsubscribe") {
                strohmNative.unsubscribe(subscriptionId: subscriptionId!)
                strohmNative.whenIncoming(prop: ("name", "foo"), for: subscriptionId!)
                expect(receivedProp).to(beNil())
            }

            it("calls unsubscribe on the web view when unsubscribing") {
                strohmNative.unsubscribe(subscriptionId: subscriptionId!)
                expect(webViewMock.evaluatedJavaScript).to(haveCount(2))
                let actual = webViewMock.evaluatedJavaScript.last!

                let pattern = #"strohm_native\.flow\.unsubscribe_from_native\("(.*)"\)"#
                expect(actual).to(match(pattern))
                let regex = try! NSRegularExpression(pattern: pattern, options: [])
                let range = NSRange(actual.startIndex..<actual.endIndex, in: actual)
                let matches = regex.matches(in: actual, options: [], range: range)
                let capturedRange = Range(matches[0].range(at: 1), in: actual)!
                let matchedSubscriptionId = String(actual[capturedRange])
                expect(matchedSubscriptionId) == subscriptionId!.uuidString
            }
        }
    }
}

extension StrohmNative {
    func whenLoadingFinished() {
        self.loadingFinished()
    }

    func whenSubscriptionCompletes(_ propSpec: PropSpec,
                                   _ handlerFn: @escaping HandlerFunction) -> UUID {
        var subscriptionId: UUID! = nil
        waitUntil { done in
            self.subscribe(
                propSpec: propSpec,
                handler: handlerFn) { id in
                    subscriptionId = id
                    done()
            }

        }
        return subscriptionId
    }

    func whenIncoming(prop: Prop, for subscriptionId: UUID) {
        let newData = try! JSONSerialization.data(withJSONObject: [prop.name, prop.value])
        let args: [String: Any] = [
            "subscriptionId": subscriptionId.uuidString,
            "new": String(data: newData, encoding: .utf8)!
        ]
        self.subscriptions?.subscriptionUpdateHandler(args: args)
    }
}

class WebViewMock: StrohmNativeWebView {
    var navigationDelegate: WKNavigationDelegate?
    var evaluatedJavaScript: [String] = []

    func loadHTMLString(_ string: String, baseURL: URL?) -> WKNavigation? {
        return nil
    }

    func evaluateJavaScript(_ javaScriptString: String, completionHandler: ((Any?, Error?) -> Void)?) {
        evaluatedJavaScript.append(javaScriptString)
    }


}
