import SwiftUI
import StrohmNative

struct ContentView: View {
    @State private var count: Int = 0
    let countFormatter = NumberFormatter()
    @State private var subscription: UUID?
    @State var strohmNativeStatus: StrohmNative.Status = .uninitialized

    var body: some View {
        VStack {
            Text("Hello, world!")
                .padding()
            HStack {
                Spacer()
                Button(action: self.decrement, label: { Text("–") })
                    .padding()

                TextField("", value: $count, formatter: countFormatter)
                    { _ in }
                    onCommit: { self.setCounter(count: count) }
                    .scaledToFit()
                    .frame(minWidth: 70)
                    .padding()
                Button(action: self.increment, label: { Text("+") })
                    .padding()
                Spacer()
            }.padding()
            HStack {
                Button(action: {
                    self.count = 0
                    self.subscription = nil
                    StrohmNative.default.reload()
                }, label: { Text("Reload") })
                Button(action: self.subscribe, label: { Text("Subscribe") })
                    .disabled(subscription != nil)
                Button(action: self.unsubscribe, label: { Text("Unsubscribe") })
                    .disabled(subscription == nil)
            }.padding()
            Text("StrohmNative status: \(strohmNativeStatus.rawValue)")
                .onReceive(StrohmNative.default.status, perform: { strohmNativeStatus = $0 })
            Text("Subscribed: \(subscription != nil ? "true" : "false")")
        }
    }

    func decrement() {
        StrohmNative.default.dispatch(type: "decrement")
    }

    func increment() {
        StrohmNative.default.dispatch(type: "increment")
    }

    func setCounter(count: Int) {
        StrohmNative.default.dispatch(type: "setCounter", payload: ["count": count])
    }

    func subscribe() {
        StrohmNative.default.subscribe(propsSpec: ("count", [])) { prop in
            print("Received prop: ", prop)
            if let count = prop.value as? Int {
                self.count = count
            }
        } completion: { subscription in
            self.subscription = subscription
        }
    }

    func unsubscribe() {
        if let subscription = self.subscription {
            StrohmNative.default.unsubscribe(subscriptionId: subscription)
            self.subscription = nil
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
