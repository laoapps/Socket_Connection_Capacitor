package com.laoapps.socketconnectioncapacitor;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.JSObject;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;

@CapacitorPlugin(name = "SocketConnectionCapacitor")
public class SocketConnectionCapacitorPlugin extends Plugin {

    private SocketConnectionCapacitor implementation = new SocketConnectionCapacitor();

    @PluginMethod
    public void connect(PluginCall call) {
        String host = call.getString("host", "localhost");
        int port = call.getInt("port", 8080);
        boolean useSsl = call.getBoolean("ssl", false);

        new Thread(() -> {
            try {
                EventLoopGroup group = new NioEventLoopGroup();
                Bootstrap bootstrap = new Bootstrap();
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
                                        // Handle incoming messages
                                        JSObject ret = new JSObject();
                                        ret.put("message", msg);
                                        notifyListeners("messageReceived", ret);
                                    }
                                });
                            }
                        });

                ChannelFuture future = bootstrap.connect(host, port).sync();
                call.resolve(new JSObject().put("success", true));
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                call.reject("Connection failed: " + e.getMessage());
            }
        }).start();
    }

    @PluginMethod
    public void sendMessage(PluginCall call) {
        String message = call.getString("message", "");
        // Implement message sending logic here
        call.resolve(new JSObject().put("success", true));
    }
}
