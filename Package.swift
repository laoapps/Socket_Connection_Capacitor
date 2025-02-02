// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "Socketconnectioncapacitor",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "Socketconnectioncapacitor",
            targets: ["SocketConnectionCapacitorPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "SocketConnectionCapacitorPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/SocketConnectionCapacitorPlugin"),
        .testTarget(
            name: "SocketConnectionCapacitorPluginTests",
            dependencies: ["SocketConnectionCapacitorPlugin"],
            path: "ios/Tests/SocketConnectionCapacitorPluginTests")
    ]
)