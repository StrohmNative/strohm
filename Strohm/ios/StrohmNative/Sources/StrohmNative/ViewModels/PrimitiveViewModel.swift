import Foundation

open class PrimitiveViewModel<T: Decodable>: ViewModelBase<T>, PropsHandler {
    public var data: T {
        willSet {
            // https://stackoverflow.com/questions/57615920/published-property-wrapper-not-working-on-subclass-of-observableobject
            self.objectWillChange.send()
        }
    }
    
    public init(initialValue: T, propName: PropName, propPath: PropPath) {
        self.data = initialValue
        super.init(propName: propName, propPath: propPath)
    }

    public required init(constantValue: T) {
        self.data = constantValue
        super.init()
    }

    override func propToData2(serializedProp: String) -> T? {
        guard let rawData = serializedProp.data(using: .utf8) else {
            return nil
        }

        do {
            let data = try decoder().decode(PropEnvelope<T>.self, from: rawData)
            let value = data.propValue
            print("Received value: ", value)
            return value
        }
        catch let e {
            Log.error(String(describing: e))
            return nil
        }
    }

    override func store(data: T) {
        self.data = data
    }

    public static func constant(_ value: T) -> Self {
        return .init(constantValue: value)
    }
}
