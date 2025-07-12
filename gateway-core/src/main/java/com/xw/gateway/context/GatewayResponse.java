package com.xw.gateway.context;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xw.constants.ResponseCode;
import com.xw.util.JSONUtil;
import io.netty.handler.codec.http.*;
import lombok.Data;
import org.asynchttpclient.Response;

/**
 * 上游请求响应
 */
@Data
public class GatewayResponse {

    //	响应头
    private HttpHeaders responseHeaders = new DefaultHttpHeaders();

    //	额外的响应头信息
    private final HttpHeaders extraResponseHeaders = new DefaultHttpHeaders();

    //	返回的响应内容
    private String content;

    //	返回响应状态码
    private HttpResponseStatus httpResponseStatus;

    //	响应对象
    private Response futureResponse;

    private GatewayResponse() {
    }

    /**
     * 设置响应头信息
     * @param key
     * @param val
     */
    public void putHeader(CharSequence key, CharSequence val) {
        responseHeaders.add(key, val);
    }

    /**
     * 构建网关响应对象
     * @param futureResponse
     * @return
     */
    public static GatewayResponse buildGatewayResponse(Response futureResponse) {
        GatewayResponse gatewayResponse = new GatewayResponse();
        gatewayResponse.setFutureResponse(futureResponse);
        gatewayResponse.setHttpResponseStatus(HttpResponseStatus.valueOf(futureResponse.getStatusCode()));
        return gatewayResponse;
    }

    /**
     * 构建一个json类型的网关响应对象，失败时候使用
     * @param code
     * @param args
     * @return
     */
    public static GatewayResponse buildGatewayResponse(ResponseCode code, Object... args) {
        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS, code.getStatus().code());
        objectNode.put(JSONUtil.CODE, code.getCode());
        objectNode.put(JSONUtil.MESSAGE, code.getMessage());
        GatewayResponse GatewayResponse = new GatewayResponse();
        GatewayResponse.setHttpResponseStatus(code.getStatus());
        GatewayResponse.putHeader(HttpHeaderNames.CONTENT_TYPE,
                HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        GatewayResponse.setContent(JSONUtil.toJSONString(objectNode));
        return GatewayResponse;
    }

    /**
     * 构建一个json类型的网关响应对象，成功时候使用
     * @param data
     * @return
     */
    public static GatewayResponse buildGatewayResponseObj(Object data) {
        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS, ResponseCode.SUCCESS.getStatus().code());
        objectNode.put(JSONUtil.CODE, ResponseCode.SUCCESS.getCode());
        objectNode.putPOJO(JSONUtil.DATA, data);
        GatewayResponse GatewayResponse = new GatewayResponse();
        GatewayResponse.setHttpResponseStatus(ResponseCode.SUCCESS.getStatus());
        GatewayResponse.putHeader(HttpHeaderNames.CONTENT_TYPE,
                HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        GatewayResponse.setContent(JSONUtil.toJSONString(objectNode));
        return GatewayResponse;
    }

}
