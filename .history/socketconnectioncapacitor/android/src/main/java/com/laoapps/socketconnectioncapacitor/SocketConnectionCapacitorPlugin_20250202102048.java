package com.laoapps.socketconnectioncapacitor;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;

@CapacitorPlugin(name = "SocketConnectionCapacitor")
public class SocketConnectionCapacitorPlugin extends Plugin {

    private Channel tcpChannel;
    private Channel udpChannel;
    private EventLoopGroup group;

    @PluginMethod
    public void connect(PluginCall call) {
        String host = call.getString("host", "localhost");
        int port = call.getInt("port", 8080);
        boolean useSsl = call.getBoolean("ssl", false);
        String protocolType = call.getString("protocol", "tcp");

        new Thread(() -> {
            try {
                group = new NioEventLoopGroup();
                Bootstrap bootstrap = new Bootstrap();
                if (protocolType.equals("tcp")) {
                    bootstrap.group(group)
                            .channel(NioSocketChannel.class)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    if (useSsl) {
                                        SslContext sslContext = SslContextBuilder.forClient()
                                                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                                .build();
                                        ch.pipeline().addLast(sslContext.newHandler(ch.alloc(), host, port));
                                    }
                                    ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                            JSObject ret = new JSObject();
                                            ret.put("message", msg);
                                            notifyListeners("messageReceived", ret);
                                        }
                                    });
                                }
                            });
                    ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port)).sync();
                    tcpChannel = future.channel();
                } else if (protocolType.equals("udp")) {
                    bootstrap.group(group)
                            .channel(NioDatagramChannel.class)
                            .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
                                    String msg = packet.content().toString(io.netty.util.CharsetUtil.UTF_8);
                                    JSObject ret = new JSObject();
                                    ret.put("message", msg);
                                    notifyListeners("messageReceived", ret);
                                }
                            });
                    ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port)).sync();
                    udpChannel = future.channel();
                } else {
                    call.reject("Unsupported protocol");
                    return;
                }
                call.resolve(new JSObject().put("success", true));
            } catch (Exception e) {
                call.reject("Connection failed: " + e.getMessage());
            }
        }).start();
    }

    @PluginMethod
    public void disconnect(PluginCall call) {
        try {
            if (tcpChannel != null) {
                tcpChannel.close().sync();
            }
            if (udpChannel != null) {
                udpChannel.close().sync();
            }
            if (group != null) {
                group.shutdownGracefully();
            }
            call.resolve(new JSObject().put("success", true));
        } catch (InterruptedException e) {
            call.reject("Disconnection failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void sendMessage(PluginCall call) {
        String message = call.getString("message", "") + "\n";
        String protocolType = call.getString("protocol", "tcp");
        if (protocolType.equals("udp") && udpChannel != null && udpChannel.isActive()) {
            udpChannel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(message, CharsetUtil.UTF_8), new InetSocketAddress(call.getString("host"), call.getInt("port"))));
            call.resolve(new JSObject().put("success", true));
        } else if (tcpChannel != null && tcpChannel.isActive()) {
            tcpChannel.writeAndFlush(message);
            call.resolve(new JSObject().put("success", true));
        } else {
            call.reject("Not connected");
        }
    }

    @PluginMethod
    public void listen(PluginCall call) {
        if ((tcpChannel != null && tcpChannel.isActive()) || (udpChannel != null && udpChannel.isActive())) {
            call.resolve(new JSObject().put("success", true));
        } else {
            call.reject("Not connected");
        }
    }

    @Override
    protected void handleOnDestroy() {
        try {
            if (tcpChannel != null) {
                tcpChannel.close();
            }
            if (udpChannel != null) {
                udpChannel.close();
            }
            if (group != null) {
                group.shutdownGracefully();
            }
        } catch (Exception e) {
            // Handle exception
        }
    }
}
