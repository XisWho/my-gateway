package com.xw.gateway.netty.processor;

import com.xw.gateway.analyzer.RequestAnalyzer;
import com.xw.gateway.context.GatewayContext;
import com.xw.gateway.netty.context.HttpRequestWrapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public class CoreRequestProcessor implements RequestProcessor {

    @Override
    public void process(HttpRequestWrapper request) {
        FullHttpRequest fullHttpRequest = request.getFullHttpRequest();
        ChannelHandlerContext ctx = request.getCtx();
        try {
            //	1. 解析FullHttpRequest, 把他转换为我们自己想要的内部对象：Context
            GatewayContext gatewayContext = RequestAnalyzer.buildContext(fullHttpRequest, ctx);
        } catch (Throwable t) {
            // 处理异常
            t.printStackTrace();
        }
    }

}
