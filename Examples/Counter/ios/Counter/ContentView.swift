import SwiftUI
import Strohm

struct ContentView: View {
    @State private var count: Int = 0
    let countFormatter = NumberFormatter()
    @State private var subscription: UUID?

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
                    Strohm.default.reload()
                }, label: { Text("Reload") })
                Button(action: self.subscribe, label: { Text("Subscribe") })
                Button(action: self.unsubscribe, label: { Text("Unsubscribe") })
            }.padding()
            StrohmHolder()
        }
    }

    func decrement() {
        Strohm.default.dispatch(type: "decrement")
    }

    func increment() {
        Strohm.default.dispatch(type: "increment")
    }

    func setCounter(count: Int) {
        Strohm.default.dispatch(type: "setCounter", payload: ["count": count])
    }

    func subscribe() {
        Strohm.default.subscribe(propsSpec: ["count": ""]) { props in
            print("Received props: ", props)
            if let count = props["count"] as? Int {
                self.count = count
            }
        } completion: { subscription in
            self.subscription = subscription
        }
    }

    func unsubscribe() {
        if let subscription = self.subscription {
            Strohm.default.unsubscribe(subscriptionId: subscription)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
