package com.xw.gateway.netty.processor;

import com.xw.gateway.config.GatewayConfig;
import com.xw.gateway.netty.context.HttpRequestWrapper;

public class FlusherRequestProcessor implements RequestProcessor {

    private GatewayConfig gatewayConfig;

    private CoreRequestProcessor coreRequestProcessor;

    public FlusherRequestProcessor(GatewayConfig gatewayConfig, CoreRequestProcessor coreRequestProcessor) {
        this.gatewayConfig = gatewayConfig;
        this.coreRequestProcessor = coreRequestProcessor;
    }

    @Override
    public void process(HttpRequestWrapper request) {

    }

}
