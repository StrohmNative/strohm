import Foundation

class StatePersister {
    let strohm: Strohm

    init(strohm: Strohm) {
        self.strohm = strohm
        strohm.comms.registerHandlerFunction(
            name: "persistState",
            function: self.persistStateHandler)
    }

    func persistStateHandler(args: JsonComms.Arguments) {
        if let state = args["state"] as? String {
            print("got state")
            do {
                let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
                let data = state.data(using: .utf8)
                try data?.write(
                    to: dir.appendingPathComponent("state.enc"),
                    options: [.atomic, .completeFileProtectionUntilFirstUserAuthentication]
                )
            }
            catch {
                // TODO: handle errors
                print("Exception writing state: \(error)")
            }
        }
    }
}
