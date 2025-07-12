package com.xw.gateway.container;

import com.xw.constants.BufferTypeConstant;
import com.xw.gateway.config.GatewayConfig;
import com.xw.gateway.netty.NettyHttpServer;
import com.xw.gateway.netty.processor.CoreRequestProcessor;
import com.xw.gateway.netty.processor.FlusherRequestProcessor;
import com.xw.gateway.netty.processor.MpmcRequestProcessor;
import com.xw.gateway.netty.processor.RequestProcessor;

public class GatewayContainer implements LifeCycle {

    private final GatewayConfig gatewayConfig;

    private NettyHttpServer nettyHttpServer;

    private RequestProcessor requestProcessor;

    public GatewayContainer(GatewayConfig gatewayConfig) {
        this.gatewayConfig = gatewayConfig;
        init();
    }

    @Override
    public void init() {
        CoreRequestProcessor coreRequestProcessor = new CoreRequestProcessor();

        String bufferType = gatewayConfig.getBufferType();
        if (BufferTypeConstant.FLUSHER.equals(bufferType)) {
            requestProcessor = new FlusherRequestProcessor(gatewayConfig, coreRequestProcessor);
        } else if (BufferTypeConstant.MPMC.equals(bufferType)) {
            requestProcessor = new MpmcRequestProcessor(gatewayConfig, coreRequestProcessor);
        } else {
            requestProcessor = coreRequestProcessor;
        }

        nettyHttpServer = new NettyHttpServer(gatewayConfig, requestProcessor);
    }

    @Override
    public void start() {
        nettyHttpServer.start();
    }

    @Override
    public void shutdown() {
        nettyHttpServer.shutdown();
    }

}
