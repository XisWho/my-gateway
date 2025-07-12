package com.xw.gateway.netty.processor;

import com.xw.gateway.config.GatewayConfig;
import com.xw.gateway.netty.context.HttpRequestWrapper;

public class MpmcRequestProcessor implements RequestProcessor {

    private GatewayConfig gatewayConfig;

    private CoreRequestProcessor coreRequestProcessor;

    public MpmcRequestProcessor(GatewayConfig gatewayConfig, CoreRequestProcessor coreRequestProcessor) {
        this.gatewayConfig = gatewayConfig;
        this.coreRequestProcessor = coreRequestProcessor;
    }

    @Override
    public void process(HttpRequestWrapper request) {

    }

}
