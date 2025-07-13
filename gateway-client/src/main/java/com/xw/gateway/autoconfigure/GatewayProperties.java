package com.xw.gateway.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = GatewayProperties.PREFIX)
public class GatewayProperties {

    public static final String PREFIX = "gateway";

    public static final String DEFAULT_ENV = "dev";

    /**
     * 	etcd注册中心地址
     */
    private String registryAddress;

    /**
     * 	etcd注册命名空间
     */
    private String namespace = PREFIX;

    /**
     * 	环境属性
     */
    private String env = DEFAULT_ENV;

}
