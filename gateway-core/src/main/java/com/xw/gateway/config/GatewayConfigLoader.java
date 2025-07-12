package com.xw.gateway.config;

import com.xw.util.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * 配置读取类
 */
@Slf4j
public class GatewayConfigLoader {

    private final static String CONFIG_FILE = "gateway.properties";

    private final static GatewayConfigLoader INSTANCE = new GatewayConfigLoader();

    private GatewayConfig gatewayConfig = new GatewayConfig();

    private GatewayConfigLoader() {
    }

    public static GatewayConfigLoader getInstance() {
        return INSTANCE;
    }

    public static GatewayConfig getGatewayConfig() {
        return INSTANCE.gatewayConfig;
    }

    /**
     * 配置加载规则：运行参数（优先级最高） > jvm参数 > 环境变量 > 配置文件 > GatewayConfig的默认属性值（优先级最低）
     * @param args
     * @return
     */
    public GatewayConfig load(String[] args) {
        // 1. 配置文件
        InputStream resource = GatewayConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
        if (resource != null) {
            Properties properties = new Properties();
            try {
                properties.load(resource);
                PropertiesUtils.properties2Object(properties, gatewayConfig);
            } catch (IOException e) {
                log.warn("GatewayConfigLoader.load config file({}) error.", CONFIG_FILE, e);
            } finally {
                if (resource != null) {
                    try {
                        resource.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
        
        // 2. 环境变量
        Map<String, String> envConfig = System.getenv();
        Properties properties = new Properties();
        properties.putAll(envConfig);
        PropertiesUtils.properties2Object(properties, gatewayConfig);
        
        // 3. jvm参数
        Properties jvmProperties = System.getProperties();
        PropertiesUtils.properties2Object(jvmProperties, gatewayConfig);

        // 4. 运行参数： --xxx=xxx
        if (args!=null && args.length>0) {
            Properties argProperties = new Properties();
            for (String arg : args) {
                if (arg.startsWith("--") && arg.contains("=") && arg.substring(arg.indexOf("=") + 1)!="") {
                    argProperties.put(arg.substring(2, arg.indexOf("=")), arg.substring(arg.indexOf("=") + 1));
                }
            }
            PropertiesUtils.properties2Object(argProperties, gatewayConfig);
        }

        return gatewayConfig;
    }

}
