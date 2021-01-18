import XCTest
@testable import Strohm

final class StrohmTests: XCTestCase {
    func testExample() {
        // This is an example of a functional test case.
        // Use XCTAssert and related functions to verify your tests produce the correct
        // results.
        XCTAssertNotNil(Strohm.default)
    }

    static var allTests = [
        ("testExample", testExample),
    ]
}
