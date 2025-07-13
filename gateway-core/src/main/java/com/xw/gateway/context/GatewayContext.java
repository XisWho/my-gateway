package com.xw.gateway.context;

import com.xw.gateway.rule.GatewayRule;
import com.xw.gateway.util.AssertUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * 网关上下文
 */
public class GatewayContext extends BasicContext {

    private final GatewayRequest gatewayRequest;

    private GatewayResponse gatewayResponse;

    private final GatewayRule gatewayRule;

    private GatewayContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive,
                           GatewayRequest gatewayRequest, GatewayRule gatewayRule) {
        super(protocol, nettyCtx, keepAlive);
        this.gatewayRequest = gatewayRequest;
        this.gatewayRule = gatewayRule;
    }

    public static class Builder {

        private String protocol;

        private ChannelHandlerContext nettyCtx;

        private GatewayRequest gatewayRequest;

        private GatewayRule gatewayRule;

        private boolean keepAlive;

        public Builder() {
        }

        public Builder setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder setNettyCtx(ChannelHandlerContext nettyCtx) {
            this.nettyCtx = nettyCtx;
            return this;
        }

        public Builder setRapidRequest(GatewayRequest gatewayRequest) {
            this.gatewayRequest = gatewayRequest;
            return this;
        }

        public Builder setRule(GatewayRule gatewayRule) {
            this.gatewayRule = gatewayRule;
            return this;
        }

        public Builder setKeepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public GatewayContext build() {
            AssertUtil.notNull(protocol, "protocol不能为空");
            AssertUtil.notNull(nettyCtx, "nettyCtx不能为空");
            AssertUtil.notNull(gatewayRequest, "gatewayRequest不能为空");
            AssertUtil.notNull(gatewayRule, "gatewayRule不能为空");
            return new GatewayContext(protocol, nettyCtx, keepAlive, gatewayRequest, gatewayRule);
        }
    }

    /**
     * 获取必要的上下文参数，如果没有则抛出IllegalArgumentException
     * @param key
     * @return
     * @param <T>
     */
    public <T> T getRequiredAttribute(AttributeKey<T> key) {
        T value = getAttribute(key);
        AssertUtil.notNull(value, "required attribute '" + key + "' is missing !");
        return value;
    }

    /**
     * 获取指定key的上下文参数，如果没有则返回第二个参数的默认值
     * @param key
     * @param defaultValue
     * @return
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttributeOrDefault(AttributeKey<T> key, T defaultValue) {
        return (T) attributes.getOrDefault(key, defaultValue);
    }

    /**
     * 根据过滤器id获取对应的过滤器配置信息
     * @param filterId
     * @return
     */
    public GatewayRule.FilterConfig getFilterConfig(String filterId) {
        return gatewayRule.getFilterConfig(filterId);
    }

    /**
     * 获取上下文中唯一的UniqueId
     * @return
     */
    public String getUniqueId() {
        return gatewayRequest.getUniqueId();
    }

    /**
     * 重写覆盖父类：basicContext的该方法，主要用于真正的释放操作
     */
    @Override
    public void releaseRequest() {
        if(requestReleased.compareAndSet(false, true)) {
            ReferenceCountUtil.release(gatewayRequest.getFullHttpRequest());
        }
    }

    @Override
    public GatewayRule getRule() {
        return gatewayRule;
    }

    @Override
    public GatewayRequest getRequest() {
        return gatewayRequest;
    }

    /**
     * 调用该方法就是获取原始请求内容，不去做任何修改动作
     * @return
     */
    public GatewayRequest getOriginRequest() {
        return gatewayRequest;
    }

    /**
     * 调用该方法区分于原始的请求对象操作，主要就是做属性修改的
     * @return
     */
    public GatewayRequest getRequestMutale() {
        return gatewayRequest;
    }

    @Override
    public GatewayResponse getResponse() {
        return gatewayResponse;
    }

    @Override
    public void setResponse(Object response) {
        this.gatewayResponse = (GatewayResponse)response;
    }

}
