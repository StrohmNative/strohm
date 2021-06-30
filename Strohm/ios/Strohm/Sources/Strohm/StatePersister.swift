import Foundation

class StatePersister {
    let strohm: Strohm

    init(strohm: Strohm) {
        self.strohm = strohm
        strohm.comms.registerHandlerFunction(
            name: "persistState",
            function: self.persistStateHandler)
    }

    private func storedStateUrl() -> URL? {
        guard let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first else {
            return nil
        }
        return dir.appendingPathComponent("state.enc")
    }

    func persistStateHandler(args: JsonComms.Arguments) {
        if let state = args["state"] as? String {
            print("got state")
            do {
                let data = state.data(using: .utf8)
                try data?.write(
                    to: storedStateUrl()!,
                    options: [.atomic, .completeFileProtectionUntilFirstUserAuthentication]
                )
            }
            catch {
                // TODO: handle errors
                print("Exception writing state: \(error)")
            }
        }
    }

    func loadState() -> String? {
        guard let data = try? Data(contentsOf: storedStateUrl()!) else {
            return nil
        }
        return String(data:data, encoding: .utf8)
    }
}
