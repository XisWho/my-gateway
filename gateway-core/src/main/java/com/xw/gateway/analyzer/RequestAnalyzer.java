package com.xw.gateway.analyzer;

import com.xw.exception.PathNoMatchedException;
import com.xw.rule.GatewayRule;
import com.xw.service.ServiceDefinition;
import com.xw.constants.BasicConstant;
import com.xw.constants.Protocol;
import com.xw.constants.RequestConstant;
import com.xw.constants.ResponseCode;
import com.xw.exception.ResponseException;
import com.xw.exception.NotFoundException;
import com.xw.gateway.context.AttributeKey;
import com.xw.gateway.context.GatewayContext;
import com.xw.gateway.context.GatewayRequest;
import com.xw.gateway.service.DynamicServiceConfigManager;
import com.xw.service.ServiceInvoker;
import com.xw.util.AntPathMatcher;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RequestAnalyzer {

    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    /**
     * 解析FullHttpRequest 构建RapidContext
     * @param request
     * @param ctx
     * @return
     */
    public static GatewayContext buildContext(FullHttpRequest request, ChannelHandlerContext ctx) {

        //	1. 	构建请求对象RapidRequest
        GatewayRequest gatewayRequest = rebuildRequest(request, ctx);

        //	2.	根据请求对象里的uniqueId，获取资源服务信息(也就是服务定义信息)
        ServiceDefinition serviceDefinition = getServiceDefinition(gatewayRequest);

        //	3.	快速路径匹配失败的策略
        if (!ANT_PATH_MATCHER.match(serviceDefinition.getPatternPath(), gatewayRequest.getPath())) {
            throw new PathNoMatchedException();
        }

        //	4. 	根据请求对象获取服务定义对应的方法调用，然后获取对应的规则
        ServiceInvoker serviceInvoker = getServiceInvoker(gatewayRequest, serviceDefinition);
        String ruleId = serviceInvoker.getRuleId();
        GatewayRule rule = DynamicServiceConfigManager.getInstance().getRule(ruleId);

        //	5. 	构建GatewayContext对象
        GatewayContext gatewayContext = new GatewayContext.Builder()
                .setProtocol(serviceDefinition.getProtocol())
                .setRapidRequest(gatewayRequest)
                .setNettyCtx(ctx)
                .setKeepAlive(HttpUtil.isKeepAlive(request))
                .setRule(rule)
                .build();

        //	6. 	设置一些必要的上下文参数用于后面使用
        putContext(gatewayContext, serviceInvoker);

        return gatewayContext;
    }

    /**
     * 构建GatewayRequest请求对象
     * @param fullHttpRequest
     * @param ctx
     * @return
     */
    private static GatewayRequest rebuildRequest(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {

        HttpHeaders headers = fullHttpRequest.headers();
        //	从header头获取必须要传入的关键属性 uniqueId
        String uniqueId = headers.get(RequestConstant.UNIQUE_ID);
        if(StringUtils.isBlank(uniqueId)) {
            throw new ResponseException(ResponseCode.REQUEST_PARSE_ERROR_NO_UNIQUEID);
        }
        String host = headers.get(HttpHeaderNames.HOST);

        HttpMethod method = fullHttpRequest.method();
        String uri = fullHttpRequest.uri();
        String clientIp = getClientIp(ctx, fullHttpRequest);
        String contentType = HttpUtil.getMimeType(fullHttpRequest) == null ? null : HttpUtil.getMimeType(fullHttpRequest).toString();
        Charset charset = HttpUtil.getCharset(fullHttpRequest, StandardCharsets.UTF_8);

        return new GatewayRequest(
                uniqueId,
                charset,
                clientIp,
                host,
                uri,
                method,
                contentType,
                headers,
                fullHttpRequest
        );
    }

    /**
     * 获取客户端ip
     * @param ctx
     * @param request
     * @return
     */
    private static String getClientIp(ChannelHandlerContext ctx, FullHttpRequest request) {
        // 反向代理会在转发请求的 HTTP 头信息中，增加了 X-FORWARDED-FOR 信息，用以跟踪原有的客户端 IP 地址和原来客户端请求的服务器地址
        String xForwardedValue = request.headers().get(BasicConstant.HTTP_FORWARD_SEPARATOR);

        String clientIp = null;
        if (StringUtils.isNotEmpty(xForwardedValue)) {
            List<String> values = Arrays.asList(xForwardedValue.split(", "));
            if(values.size() >= 1 && StringUtils.isNotBlank(values.get(0))) {
                clientIp = values.get(0);
            }
        }
        if (clientIp == null) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
            clientIp = inetSocketAddress.getAddress().getHostAddress();
        }
        return clientIp;
    }

    /**
     * 通过请求对象获取服务资源信息
     * @param gatewayRequest
     * @return
     */
    private static ServiceDefinition getServiceDefinition(GatewayRequest gatewayRequest) {
        //	ServiceDefinition从哪里获取，就是在网关服务初始化的时候(加载的时候)？ 从缓存信息里获取
        ServiceDefinition serviceDefinition = DynamicServiceConfigManager.getInstance().getServiceDefinition(gatewayRequest.getUniqueId());
        //	做异常情况判断
        if(serviceDefinition == null) {
            throw new NotFoundException(ResponseCode.SERVICE_DEFINITION_NOT_FOUND);
        }
        return serviceDefinition;
    }

    /**
     * 根据请求对象和服务定义对象获取对应的ServiceInvoke
     * @param gatewayRequest
     * @param serviceDefinition
     * @return
     */
    private static ServiceInvoker getServiceInvoker(GatewayRequest gatewayRequest, ServiceDefinition serviceDefinition) {
        Map<String, ServiceInvoker> invokerMap = serviceDefinition.getInvokerMap();
        ServiceInvoker serviceInvoker = invokerMap.get(gatewayRequest.getPath());
        if(serviceInvoker == null) {
            throw new NotFoundException(ResponseCode.SERVICE_INVOKER_NOT_FOUND);
        }
        return serviceInvoker;
    }

    /**
     * 设置上下文
     * @param gatewayContext
     * @param serviceInvoker
     */
    private static void putContext(GatewayContext gatewayContext, ServiceInvoker serviceInvoker) {
        switch (gatewayContext.getProtocol()) {
            case Protocol.HTTP:
                gatewayContext.putAttribute(AttributeKey.HTTP_INVOKER, serviceInvoker);
                break;
            case Protocol.DUBBO:
                gatewayContext.putAttribute(AttributeKey.DUBBO_INVOKER, serviceInvoker);
                break;
            default:
                break;
        }
    }

}
