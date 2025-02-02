import Foundation
import Capacitor
import SwiftSocket

@objc(SocketConnectionCapacitorPlugin)
public class SocketConnectionCapacitorPlugin: CAPPlugin {
    private var tcpClient: TCPClient?
    private var udpClient: UDPClient?

    @objc func connect(_ call: CAPPluginCall) {
        guard let host = call.getString("host") else {
            call.reject("Host is required")
            return
        }
        let port = Int32(call.getInt("port") ?? 8080)
        let useSsl = call.getBool("ssl") ?? false
        let protocolType = call.getString("protocol", "tcp")

        if protocolType == "tcp" {
            tcpClient = TCPClient(address: host, port: port)
            switch tcpClient?.connect(timeout: 10) {
            case .success:
                call.resolve(["success": true])
            case .failure(let error):
                call.reject("Connection failed: \(error)")
            case .none:
                call.reject("Client initialization failed")
            }
        } else if protocolType == "udp" {
            udpClient = UDPClient(address: host, port: port)
            call.resolve(["success": true])
        } else {
            call.reject("Unsupported protocol")
        }
    }

    @objc func disconnect(_ call: CAPPluginCall) {
        tcpClient?.close()
        udpClient?.close()
        call.resolve(["success": true])
    }

    @objc func sendMessage(_ call: CAPPluginCall) {
        guard let message = call.getString("message") else {
            call.reject("Message is required")
            return
        }
        let messageWithNewline = message + "\n"
        let data = messageWithNewline.data(using: .utf8)!

        if let protocolType = call.getString("protocol"), protocolType == "udp", let udpClient = udpClient {
            switch udpClient.send(data: data) {
            case .success:
                call.resolve(["success": true])
            case .failure(let error):
                call.reject("Failed to send message: \(error)")
            }
        } else if let tcpClient = tcpClient {
            switch tcpClient.send(data: data) {
            case .success:
                call.resolve(["success": true])
            case .failure(let error):
                call.reject("Failed to send message: \(error)")
            }
        } else {
            call.reject("Client is not connected")
        }
    }

    @objc func listen(_ call: CAPPluginCall) {
        DispatchQueue.global(qos: .background).async {
            while let response = self.tcpClient?.read(1024*10) {
                let message = String(bytes: response, encoding: .utf8) ?? ""
                self.notifyListeners("messageReceived", data: ["message": message])
            }
        }
        call.resolve(["success": true])
    }

    override public func load() {
        super.load()
        NotificationCenter.default.addObserver(self, selector: #selector(self.handleAppTerminate), name: UIApplication.willTerminateNotification, object: nil)
    }

    @objc private func handleAppTerminate() {
        tcpClient?.close()
        udpClient?.close()
    }
}
