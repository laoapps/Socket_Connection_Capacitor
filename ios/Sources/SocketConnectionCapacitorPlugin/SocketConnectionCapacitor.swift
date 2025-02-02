import Foundation

@objc public class SocketConnectionCapacitor: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
