package com.xw.gateway.context;

import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import com.xw.gateway.constants.BasicConstant;
import com.xw.gateway.util.TimeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 下游请求
 */
@Slf4j
public class GatewayRequest implements GatewayRequestMutable {

    /**
     * 	FullHttpRequest: 在header里面必须要有该属性：uniqueId
     * 	表示服务的唯一性ID: serviceId:version
     */
    @Getter
    private final String uniqueId;

    /**
     * 	一次请求进入网关的开始时间戳
     */
    @Getter
    private final long beginTime;

    /**
     * 字符集
     */
    @Getter
    private final Charset charset;

    /**
     * 	客户端的ip地址：流控、黑白名单
     */
    @Getter
    private final String clientIp;

    /**
     * 	请求的地址：ip:port
     */
    @Getter
    private final String host;

    /**
     * 	请求的路径：/xxx/xx/xxx
     */
    @Getter
    private final String path;

    /**
     * uri: /xxx/xx/xxx?attr1=value1&attr2=value2
     */
    @Getter
    private final String uri;

    /**
     * 	请求的方式：get/post/put...
     */
    @Getter
    private final HttpMethod method;

    /**
     * 	请求的格式
     */
    @Getter
    private final String contentType;

    /**
     * 	请求头信息
     */
    @Getter
    private final HttpHeaders headers;

    /**
     * 	参数解析器
     */
    @Getter
    private final QueryStringDecoder queryDecoder;

    /**
     *	FullHttpRequest
     */
    @Getter
    private final FullHttpRequest fullHttpRequest;

    /**
     * 	请求体
     */
    private String body;

    /**
     * 	请求的时候定义的post参数集合
     */
    private Map<String, List<String>> postParameters;

    /***************** GatewayRequestMutable（对应下游请求） **********************/
    /**
     * 	可修改的scheme：默认为 http://
     */
    private String modifyScheme;

    /**
     * 	可修改的host
     */
    private String modifyHost;

    /**
     * 	可修改的path
     */
    private String modifyPath;

    /**
     * 	构建下游请求时的Http请构建器
     */
    private final RequestBuilder requestBuilder;

    public GatewayRequest(String uniqueId, Charset charset, String clientIp, String host, String uri,
                          HttpMethod method, String contentType, HttpHeaders headers, FullHttpRequest fullHttpRequest) {
        this.uniqueId = uniqueId;
        this.beginTime = TimeUtil.currentTimeMillis();
        this.charset = charset;
        this.clientIp = clientIp;
        this.host = host;
        this.method = method;
        this.contentType = contentType;
        this.headers = headers;
        this.uri = uri;
        this.queryDecoder = new QueryStringDecoder(uri, charset);
        this.path = queryDecoder.path();
        this.fullHttpRequest = fullHttpRequest;

        this.modifyHost = host;
        this.modifyPath = path;
        this.modifyScheme = BasicConstant.HTTP_PREFIX_SEPARATOR;

        this.requestBuilder = new RequestBuilder();
        this.requestBuilder.setMethod(getMethod().name());
        this.requestBuilder.setHeaders(getHeaders());
        this.requestBuilder.setQueryParams(queryDecoder.parameters());
        ByteBuf contentBuffer = fullHttpRequest.content();
        if(Objects.nonNull(contentBuffer)) {
            this.requestBuilder.setBody(contentBuffer.nioBuffer());
        }
    }

    /**
     * 获取body信息
     * @return
     */
    public String getBody() {
        if(StringUtils.isEmpty(body)) {
            body = fullHttpRequest.content().toString(charset);
        }
        return body;
    }

    /**
     * 获取请求参数中指定名称的参数值
     * @param name
     * @return
     */
    public List<String> getQueryParameter(String name){
        return queryDecoder.parameters().get(name);
    }

    /**
     * 获取POST提交内容中指定名称的参数值
     * @param name
     * @return
     */
    public List<String> getPostParameter(String name){
        String body = getBody();
        if (isFormPost()) {
            if (postParameters == null) {
                QueryStringDecoder paramDecoder = new QueryStringDecoder(body, false);
                postParameters = paramDecoder.parameters();
            }

            if (postParameters == null || postParameters.isEmpty()) {
                return null;
            } else {
                return postParameters.get(name);
            }
        } else if (isJsonPost()) {
            try {
                return Lists.newArrayList(JsonPath.read(body, name).toString());
            } catch (Exception e) {
                //	ignore
                log.error("#RapidRequest# getPostParametersMultiple JsonPath解析失败，jsonPath: {}, body: {}", name, body, e);
            }
        }
        return null;
    }

    @Override
    public Request build() {
        requestBuilder.setUrl(getFinalUrl());
        return requestBuilder.build();
    }

    @Override
    public String getFinalUrl() {
        return modifyScheme + modifyHost + modifyPath;
    }

    @Override
    public void setModifyHost(String modifyHost) {
        this.modifyHost = modifyHost;
    }

    @Override
    public String getModifyHost() {
        return modifyHost;
    }

    @Override
    public void setModifyPath(String modifyPath) {
        this.modifyPath = modifyPath;
    }

    @Override
    public String getModifyPath() {
        return modifyPath;
    }

    @Override
    public void addHeader(CharSequence name, String value) {
        requestBuilder.addHeader(name, value);
    }

    @Override
    public void setHeader(CharSequence name, String value) {
        requestBuilder.setHeader(name, value);
    }

    @Override
    public void addQueryParam(String name, String value) {
        requestBuilder.addQueryParam(name, value);
    }

    @Override
    public void addOrReplaceCookie(org.asynchttpclient.cookie.Cookie cookie) {
        requestBuilder.addOrReplaceCookie(cookie);
    }

    @Override
    public void addFormParam(String name, String value) {
        if(isFormPost()) {
            requestBuilder.addFormParam(name, value);
        }
    }

    @Override
    public void setRequestTimeout(int requestTimeout) {
        requestBuilder.setRequestTimeout(requestTimeout);
    }

    public boolean isFormPost() {
        return HttpMethod.POST.equals(method) &&
                (contentType.startsWith(HttpHeaderValues.FORM_DATA.toString()) ||
                        contentType.startsWith(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString()));
    }

    public boolean isJsonPost() {
        return HttpMethod.POST.equals(method) &&
                contentType.startsWith(HttpHeaderValues.APPLICATION_JSON.toString());
    }

}
