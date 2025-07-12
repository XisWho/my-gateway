package com.xw.gateway;

import com.xw.gateway.config.GatewayConfig;
import com.xw.gateway.config.GatewayConfigLoader;
import com.xw.gateway.container.GatewayContainer;

public class Bootstrap {

    public static void main(String[] args) {
        // 1. 加载配置信息
        GatewayConfig gatewayConfig = GatewayConfigLoader.getInstance().load(args);

        // 2. 启动容器
        GatewayContainer gatewayContainer = new GatewayContainer(gatewayConfig);
        gatewayContainer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> gatewayContainer.shutdown()));
    }

}
