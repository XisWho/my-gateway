package com.xw.gateway.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GatewayService {

    /**
     * 服务的唯一ID
     * @return
     */
    String serviceId();

    /**
     * 服务的版本号
     * @return
     */
    String version() default "1.0.0";

    /**
     * 协议类型
     * @return
     */
    Protocol protocol();

    /**
     * ANT路径匹配表达式配置
     * @return
     */
    String patternPath();

}
