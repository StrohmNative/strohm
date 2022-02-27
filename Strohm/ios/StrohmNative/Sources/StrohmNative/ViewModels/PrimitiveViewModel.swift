import Foundation

open class PrimitiveViewModel<T>: ViewModelBase<T>, PropsHandler {
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

    override func propsToData(props: Props) -> T? {
        guard let rawData = props[self.propName] as? [String:Any],
              let value = rawData[self.propName] as? T else {
            return nil
        }

        print("Received value: ", value)
        return value
    }

    override func store(data: T) {
        self.data = data
    }

    public static func constant(_ value: T) -> Self {
        return .init(constantValue: value)
    }
}
