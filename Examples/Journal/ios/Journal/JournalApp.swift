import SwiftUI
import Strohm

@main
struct JournalApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
            StrohmHolder()
                .frame(minWidth: 0, idealWidth: 0, maxWidth: 0,
                       minHeight: 0, idealHeight: 0, maxHeight: 0,
                       alignment: .center)
        }
    }
}
