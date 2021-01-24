import Foundation
import Quick
import Nimble
import WebKit
@testable import Strohm

let propsSpec = [
    "name": "user.name",
    "city": "user.address.city"
]

class SubscriptionsSpec: QuickSpec {
    override func spec() {
        var strohm: Strohm!
        var webViewMock: WebViewMock!

        beforeEach {
            strohm = Strohm()
            strohm.install(appJsPath: "")
            webViewMock = WebViewMock()
            strohm.webView = webViewMock
        }

        context("when subscribing before load finishes") {
            var subscriptionComplete: Bool = false
            let handlerFn: HandlerFunction = { _ in }

            beforeEach {
                subscriptionComplete = false
                strohm.subscribe(propsSpec: propsSpec, handler: handlerFn) { _ in
                    subscriptionComplete = true
                }
            }

            it("remembers pending subscrtiption details") {
                expect(strohm.subscriptions?.pendingSubscriptions).to(haveCount(1))
            }

            it("is not yet complete") {
                expect(subscriptionComplete).to(beFalse())
            }

            it("has nothing pending after load finishes") {
                strohm.whenLoadingFinished()
                expect(strohm.subscriptions?.pendingSubscriptions).to(beNil())
            }

            it("calls completion handler after load finishes") {
                strohm.whenLoadingFinished()
                expect(subscriptionComplete).to(beTrue())
            }
        }

        context("when subscribing after load finishes") {
            var subscriptionComplete: Bool = false
            let handlerFn: HandlerFunction = { _ in }

            beforeEach {
                strohm.whenLoadingFinished()
                subscriptionComplete = false
                strohm.subscribe(
                    propsSpec: propsSpec,
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
                let pattern = #"strohm\.core\.subscribe_from_native\(".*", ?"(.*)"\)"#
                expect(actual).to(match(pattern))
                let regex = try! NSRegularExpression(pattern: pattern, options: [])
                let range = NSRange(actual.startIndex..<actual.endIndex, in: actual)
                let matches = regex.matches(in: actual, options: [], range: range)
                let capturedRange = Range(matches[0].range(at: 1), in: actual)!
                let matchedSerializedPropsSpec = actual[capturedRange]
                let unescaped = matchedSerializedPropsSpec.replacingOccurrences(of: "\\\"", with: "\"")
                let parsed = try! JSONSerialization.jsonObject(
                    with: unescaped.data(using: .utf8)!,
                    options: []) as! Dictionary<String, String>
                expect(parsed["name"]) == propsSpec["name"]
                expect(parsed["city"]) == propsSpec["city"]
            }
        }

        context("when subscribed") {
            var receivedProps: [String: Any]?
            var subscriptionId: UUID?
            let handlerFn: HandlerFunction = { props in
                receivedProps = props
            }

            beforeEach {
                receivedProps = nil
                subscriptionId = nil
                strohm.whenLoadingFinished()
                subscriptionId = strohm.whenSubscriptionCompletes(propsSpec, handlerFn)
            }

            it("receives prop updates") {
                let props = ["name": "foo"]
                strohm.whenIncoming(props: props, for: subscriptionId!)
                expect(receivedProps as? [String: String]) == props
            }

            it("does not receive props for someone else") {
                var otherProps: Props?
                let otherId = strohm.whenSubscriptionCompletes(propsSpec) { props in
                    otherProps = props
                }
                strohm.whenIncoming(props: ["name": "foo"], for: otherId)
                expect(receivedProps).to(beNil())
                expect(otherProps).toNot(beNil())
            }

            it("does not receive props after unsubscribe") {
                strohm.unsubscribe(subscriptionId: subscriptionId!)
                strohm.whenIncoming(props: ["name": "foo"], for: subscriptionId!)
                expect(receivedProps).to(beNil())
            }

            it("calls unsubscribe on the web view when unsubscribing") {
                strohm.unsubscribe(subscriptionId: subscriptionId!)
                expect(webViewMock.evaluatedJavaScript).to(haveCount(2))
                let actual = webViewMock.evaluatedJavaScript.last!

                let pattern = #"strohm\.core\.unsubscribe_from_native\("(.*)"\)"#
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

extension Strohm {
    func whenLoadingFinished() {
        self.loadingFinished()
    }

    func whenSubscriptionCompletes(_ propsSpec: PropsSpec,
                                   _ handlerFn: @escaping HandlerFunction) -> UUID {
        var subscriptionId: UUID! = nil
        waitUntil { done in
            self.subscribe(
                propsSpec: propsSpec,
                handler: handlerFn) { id in
                    subscriptionId = id
                    done()
            }

        }
        return subscriptionId
    }

    func whenIncoming(props: Props, for subscriptionId: UUID) {
        self.subscriptions?.handlePropsUpdate(props: props, subscriptionId: subscriptionId)
    }
}

class WebViewMock: StrohmWebView {
    var navigationDelegate: WKNavigationDelegate?
    var evaluatedJavaScript: [String] = []

    func loadHTMLString(_ string: String, baseURL: URL?) -> WKNavigation? {
        return nil
    }

    func evaluateJavaScript(_ javaScriptString: String, completionHandler: ((Any?, Error?) -> Void)?) {
        evaluatedJavaScript.append(javaScriptString)
    }


}
