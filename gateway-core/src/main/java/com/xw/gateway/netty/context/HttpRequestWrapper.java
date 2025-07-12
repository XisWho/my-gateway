package com.xw.gateway.netty.context;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Getter;
import lombok.Setter;

/**
 * HttpRequest包装类
 */
@Getter
@Setter
public class HttpRequestWrapper {

    private FullHttpRequest httpRequest;

    private ChannelHandlerContext context;

}
