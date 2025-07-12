package com.xw.gateway.context;

import com.xw.rule.GatewayRule;
import io.netty.channel.ChannelHandlerContext;

import java.util.function.Consumer;

/**
 * 网关上下文接口定义
 */
public interface Context {

    // 一个请求正在执行过程中
    int RUNNING = -1;

    // 写回响应标记, 标记当前请求需要写回
    int WRITTEN = 0;

    // ctx.writeAndFlush(response)写回成功后, 设置该标记
    int COMPLETED = 1;

    // 表示整个网关请求完毕, 彻底结束
    int TERMINATED = 2;

    /**
     * 设置上下文状态为正常运行状态
     */
    void runned();

    /**
     * 设置上下文状态为标记写回
     */
    void writtened();

    /**
     * 设置上下文状态为写回结束
     */
    void completed();

    /**
     * 设置上下文状态为最终结束
     */
    void terminated();

    boolean isRunning();

    boolean isWrittened();

    boolean isCompleted();

    boolean isTerminated();

    /**
     * 获取请求转换协议
     * @return
     */
    String getProtocol();

    /**
     * 获取规则
     * @return
     */
    GatewayRule getRule();

    /**
     * 获取请求对象
     * @return
     */
    Object getRequest();

    /**
     * 获取响应对象
     * @return
     */
    Object getResponse();

    /**
     * 设置响应对象
     * @param response
     */
    void setResponse(Object response);

    /**
     * 设置异常信息
     * @param throwable
     */
    void setThrowable(Throwable throwable);

    /**
     * 获取异常
     * @return
     */
    Throwable getThrowable();

    /**
     * 获取上下文参数
     * @param key
     * @return
     * @param <T>
     */
    <T> T getAttribute(AttributeKey<T> key);

    /**
     * 保存上下文属性信息
     * @param key
     * @param value
     * @return
     * @param <T>
     */
    <T> T putAttribute(AttributeKey<T> key, T value);

    /**
     * 获取Netty的上下文对象
     * @return
     */
    ChannelHandlerContext getNettyCtx();

    /**
     * 是否保持连接
     * @return
     */
    boolean isKeepAlive();

    /**
     * 释放请求资源
     */
    void releaseRequest();

    /**
     * 设置写回接收回调函数
     * @param consumer
     */
    void completedCallback(Consumer<Context> consumer);

    /**
     * 回调函数执行
     */
    void invokeCompletedCallback();

}
