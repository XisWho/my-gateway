package com.xw.gateway.netty;

import com.xw.gateway.config.GatewayConfig;
import com.xw.gateway.container.LifeCycle;
import com.xw.gateway.netty.handler.NettyHttpServerHandler;
import com.xw.gateway.netty.handler.NettyServerConnectManagerHandler;
import com.xw.gateway.netty.processor.RequestProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NettyHttpServer implements LifeCycle {

    private final GatewayConfig gatewayConfig;

    private int port = 8888;

    private ServerBootstrap serverBootstrap;

    private EventLoopGroup bossEventLoopGroup;

    private EventLoopGroup workerEventLoopGroup;

    private RequestProcessor requestProcessor;

    public NettyHttpServer(GatewayConfig gatewayConfig, RequestProcessor requestProcessor) {
        this.gatewayConfig = gatewayConfig;
        this.requestProcessor = requestProcessor;

        // 0-1023：系统端口，也叫公认端口，这些端口只有系统特许的进程才能使用；
        // 1024~65535为用户端口：
        //    1024-5000： 临时端口，一般的应用程序使用1024到4999来进行通讯；
        //    5001-65535：服务器(非特权)端口，用来给用户自定义端口。
        if (gatewayConfig.getPort()>=1024 && gatewayConfig.getPort()<=65535) {
            this.port = gatewayConfig.getPort();
        }

        init();
    }

    @Override
    public void init() {
        this.serverBootstrap = new ServerBootstrap();
        this.bossEventLoopGroup = new NioEventLoopGroup(gatewayConfig.getBossEventLoopGroupThreadNum(), new DefaultThreadFactory("NettyBossGroup"));
        this.workerEventLoopGroup = new NioEventLoopGroup(gatewayConfig.getWorkerEventLoopGroupThreadNum(), new DefaultThreadFactory("NettyWorkerGroup"));
    }

    @Override
    public void start() {
        this.serverBootstrap
                .group(bossEventLoopGroup, workerEventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(this.port))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(
                                new HttpServerCodec(),
                                // 连接管理
                                new NettyServerConnectManagerHandler(),
                                // 请求处理
                                new NettyHttpServerHandler(requestProcessor)
                        );
                    }
                });
        try {
            this.serverBootstrap.bind().sync();
            log.info("< ============= Gateway NettyHttpServer starts on port: " + this.port + "================ >");
        } catch (Exception e) {
            throw new RuntimeException("Gateway NettyHttpServer starts fail!", e);
        }
    }

    @Override
    public void shutdown() {
        if (bossEventLoopGroup != null) {
            bossEventLoopGroup.shutdownGracefully();
        }
        if (workerEventLoopGroup != null) {
            workerEventLoopGroup.shutdownGracefully();
        }
    }

}
