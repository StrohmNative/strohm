import SwiftUI

struct ContentView: View {
    var body: some View {
        VStack {
            HStack {
                Text("Hello, world!")
                    .padding()
                Button(action: { Strohm.default.reload() },
                       label: { Text("Reload") })
            }
            WebViewWrapper()
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
