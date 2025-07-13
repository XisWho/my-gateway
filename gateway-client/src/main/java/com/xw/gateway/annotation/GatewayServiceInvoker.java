package com.xw.gateway.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GatewayServiceInvoker {

    /**
     * 访问路径
     * @return
     */
    String path();

}
