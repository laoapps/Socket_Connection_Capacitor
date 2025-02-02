import Foundation
import Capacitor
import SwiftSocket
/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(SocketConnectionCapacitorPlugin)
public class SocketConnectionCapacitorPlugin: CAPPlugin, CAPBridgedPlugin {
   private var client: TCPClient?

    @objc func connect(_ call: CAPPluginCall) {
        guard let host = call.getString("host") else {
            call.reject("Host is required")
            return
        }
        let port = Int32(call.getInt("port") ?? 8080)
        let useSsl = call.getBool("ssl") ?? false

        client = TCPClient(address: host, port: port)

        switch client?.connect(timeout: 10) {
        case .success:
            call.resolve(["success": true])
        case .failure(let error):
            call.reject("Connection failed: \(error)")
        case .none:
            call.reject("Client initialization failed")
        }
    }

    @objc func sendMessage(_ call: CAPPluginCall) {
        guard let message = call.getString("message") else {
            call.reject("Message is required")
            return
        }

        switch client?.send(string: message) {
        case .success:
            call.resolve(["success": true])
        case .failure(let error):
            call.reject("Failed to send message: \(error)")
        case .none:
            call.reject("Client is not connected")
        }
    }
}
