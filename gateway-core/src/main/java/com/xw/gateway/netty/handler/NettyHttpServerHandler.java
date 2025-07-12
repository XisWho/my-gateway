package com.xw.gateway.netty.handler;

import com.xw.gateway.netty.context.HttpRequestWrapper;
import com.xw.gateway.netty.processor.RequestProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 请求处理器
 */
@Slf4j
public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {

    private RequestProcessor requestProcessor;

    public NettyHttpServerHandler(RequestProcessor requestProcessor) {
        this.requestProcessor = requestProcessor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            // HttpRequest和FullHttpRequest都是netty中的接口类
            FullHttpRequest request = (FullHttpRequest) msg;

            // 由于是异步处理请求，所以要把请求和上下文（可以找到响应地址）包装起来
            HttpRequestWrapper httpRequestWrapper = new HttpRequestWrapper();
            httpRequestWrapper.setHttpRequest(request);
            httpRequestWrapper.setContext(ctx);
            requestProcessor.process(httpRequestWrapper);
        } else {
            // 正常来说都不会走到这里，因为第一个handler是HttpServerCodec，它会转化请求为HttpRequest
            log.error("NettyHttpServerHandler.channelRead receive error msg {}", msg);
            boolean release = ReferenceCountUtil.release(msg);
            if(!release) {
                log.error("NettyHttpServerHandler.channelRead release msg fail");
            }
        }
    }

}
