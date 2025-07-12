package com.xw.gateway.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 连接管理器
 */
@Slf4j
public class NettyServerConnectManagerHandler extends ChannelDuplexHandler {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        String remoteAddr = parseChannelRemoteAddr(ctx.channel());
        log.debug("NettyServer channelRegistered {} at {}", remoteAddr, getCurrentTime());
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        String remoteAddr = parseChannelRemoteAddr(ctx.channel());
        log.debug("NettyServer channelUnregistered {} at {}", remoteAddr, getCurrentTime());
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String remoteAddr = parseChannelRemoteAddr(ctx.channel());
        log.debug("NettyServer channelActive {} at {}", remoteAddr, getCurrentTime());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String remoteAddr = parseChannelRemoteAddr(ctx.channel());
        log.debug("NettyServer channelInactive {} at {}", remoteAddr, getCurrentTime());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String remoteAddr = parseChannelRemoteAddr(ctx.channel());
        log.warn("NettyServer exceptionCaught {} at {}, error info: {}", remoteAddr, getCurrentTime(), cause);
        ctx.channel().close();
    }

    private String parseChannelRemoteAddr(final Channel channel) {
        if (null == channel) {
            return "";
        }
        SocketAddress remote = channel.remoteAddress();
        final String addr = remote != null ? remote.toString() : "";

        if (addr.length() > 0) {
            int index = addr.lastIndexOf("/");
            if (index >= 0) {
                return addr.substring(index + 1);
            }

            return addr;
        }

        return "";
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

}
