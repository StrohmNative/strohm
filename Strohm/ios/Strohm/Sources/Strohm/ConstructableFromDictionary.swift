import Foundation

public protocol ConstructableFromDictionary {
    init?(from dict: [String:Any])
}
