import Foundation

public protocol ConstructableFromDictionary {
    init?(from dict: [String:Any])
}

public protocol ConvertableToDictionary {
    func toDict() -> [String:Any]
}
