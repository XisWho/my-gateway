package com.xw.gateway;

import com.xw.gateway.config.GatewayConfig;
import com.xw.gateway.config.GatewayConfigLoader;
import com.xw.gateway.container.GatewayContainer;
import com.xw.gateway.registry.RegistryManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Bootstrap {

    public static void main(String[] args) {
        // 1. 加载配置信息
        GatewayConfig gatewayConfig = GatewayConfigLoader.getInstance().load(args);


        //	3. 初始化服务注册管理中心（服务注册管理器）, 监听动态配置的新增、修改、删除
        try {
            RegistryManager.getInstance().initialized(gatewayConfig);
        } catch (Exception e) {
            log.error("RegistryManager is failed", e);
        }

        // 4. 启动容器
        GatewayContainer gatewayContainer = new GatewayContainer(gatewayConfig);
        gatewayContainer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> gatewayContainer.shutdown()));
    }

}
