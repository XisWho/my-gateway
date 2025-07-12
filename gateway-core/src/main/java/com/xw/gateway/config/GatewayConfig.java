package com.xw.gateway.config;

import com.xw.constants.BufferTypeConstant;
import lombok.Getter;
import lombok.Setter;

/**
 * 网关配置类
 */
@Getter
@Setter
public class GatewayConfig {

    // 默认端口
    private int port = 8888;
    // 网关唯一ID
    private String gatewayId = "127.0.0.1" + ":" + port;
    // 网关服务器的CPU核数映射的线程数
    private int processThread = Runtime.getRuntime().availableProcessors();
    // Netty的Boss线程数
    private int bossEventLoopGroupThreadNum = 1;
    // Netty的Work线程数
    private int workerEventLoopGroupThreadNum = processThread;
    //网关队列配置：缓冲模式；
    private String bufferType = BufferTypeConstant.MPMC; // RapidBufferHelper.FLUSHER;

}
