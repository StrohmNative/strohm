import Foundation

open class PrimitiveViewModel<Int>: ViewModelBase<Int>, PropsHandler {
    public var data: Int {
        willSet {
            // https://stackoverflow.com/questions/57615920/published-property-wrapper-not-working-on-subclass-of-observableobject
            self.objectWillChange.send()
        }
    }
    
    public init(initialValue: Int, propName: PropName, propPath: PropPath) {
        self.data = initialValue
        super.init(propName: propName, propPath: propPath)
    }

    init(constantValue: Int) {
        self.data = constantValue
        super.init()
    }

    override func propsToData(props: Props) -> Int? {
        guard let rawData = props[self.propName] as? [String:Any],
              let value = rawData[self.propName] as? Int else {
            return nil
        }

        print("Received value: ", value)
        return value
    }

    override func store(data: Int) {
        self.data = data
    }

    public static func constant(_ value: Int) -> PrimitiveViewModel<Int> {
        return .init(constantValue: value)
    }
}

