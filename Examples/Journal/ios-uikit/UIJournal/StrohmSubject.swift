import Foundation
import RxSwift
import StrohmNative

open class KeyedArraySubject<EntryType: Codable>: KeyedArrayViewModel<EntryType> {
//    public let publisher = PublishSubject<[EntryType]>()
    public let publisher: BehaviorSubject<[EntryType]>

    public override init(initialEntries: [EntryType], propName: PropName, propPath: PropPath, sorter: ((EntryType, EntryType) -> Bool)? = nil) {
        self.publisher = BehaviorSubject(value: initialEntries)
        super.init(initialEntries: initialEntries, propName: propName, propPath: propPath, sorter: sorter)
    }

    public required init(constantEntries: [EntryType]) {
        self.publisher = BehaviorSubject(value: constantEntries)
        super.init(constantEntries: constantEntries)
    }

    override open func store(data: [EntryType]) {
//        super.store(data: data) // TODO: keep also old behavior? Only inherit functions, not data?
        publisher.onNext(data)
    }
}

open class ArraySubject<EntryType: Codable>: ArrayViewModel<EntryType> {
    public let publisher: BehaviorSubject<[EntryType]>

    public override init(propName: PropName, propPath: PropPath) {
        self.publisher = BehaviorSubject(value: [])
        super.init(propName: propName, propPath: propPath)
    }

    override open func store(data: [EntryType]) {
        super.store(data: data)
        publisher.onNext(data)
    }
}

open class PrimitiveSubject<T: Decodable>: PrimitiveViewModel<T> {
    public let publisher: BehaviorSubject<T>

    public override init(initialValue: T, propName: PropName, propPath: PropPath) {
        self.publisher = BehaviorSubject(value: initialValue)
        super.init(initialValue: initialValue, propName: propName, propPath: propPath)
    }

    public required init(constantValue: T) {
        self.publisher = BehaviorSubject(value: constantValue)
        super.init(constantValue: constantValue)
    }

    override open func store(data: T) {
        super.store(data: data)
        publisher.onNext(data)
    }
}

open class SimpleSubject<EntryType: Decodable>: SimpleViewModel<EntryType> {
    public let publisher: BehaviorSubject<EntryType>

    public override init(initialData: EntryType, propName: PropName, propPath: PropPath) {
        self.publisher = BehaviorSubject(value: initialData)
        super.init(initialData: initialData, propName: propName, propPath: propPath)
    }

    public required init(constant: EntryType) {
        self.publisher = BehaviorSubject(value: constant)
        super.init(constant: constant)
    }

    override open func store(data: EntryType) {
        super.store(data: data)
        publisher.onNext(data)
    }
}
