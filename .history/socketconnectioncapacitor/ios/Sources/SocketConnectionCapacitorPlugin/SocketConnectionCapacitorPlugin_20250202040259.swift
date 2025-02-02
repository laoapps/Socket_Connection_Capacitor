import Foundation
import Capacitor
import SwiftSocket

@objc(SocketConnectionCapacitorPlugin)
public class SocketConnectionCapacitorPlugin: CAPPlugin {
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

    @objc func disconnect(_ call: CAPPluginCall) {
        client?.close()
        call.resolve(["success": true])
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

    @objc func receiveMessage(_ call: CAPPluginCall) {
        guard let response = client?.read(1024*10) else {
            call.reject("Failed to receive message")
            return
        }
        let message = String(bytes: response, encoding: .utf8) ?? ""
        call.resolve(["message": message])
    }
}
