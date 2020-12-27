import SwiftUI

struct ContentView: View {
    @State private var count: Int = 0
    let countFormatter = NumberFormatter()

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
            Button(action: { Strohm.default.reload() }, label: { Text("Reload") })
                .padding()
            StrohmHolder()
        }
    }

    func decrement() {
        print("–")
    }

    func increment() {
        print("+")
    }

    func setCounter(count: Int) {
        print("setCounter", count)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
