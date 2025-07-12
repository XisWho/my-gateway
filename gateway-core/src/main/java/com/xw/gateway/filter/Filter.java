package com.xw.gateway.filter;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Filter {

    /**
     * 过滤器的唯一ID, 必填
     * @return
     */
    String id();

    /**
     * 过滤器的名字
     * @return
     */
    String name() default "";

    /**
     * 过滤器的类型
     * @return
     */
    ProcessorFilterType value();

    /**
     * 过滤器的排序，按照此排序从小到大依次执行过滤器
     * @return
     */
    int order() default 0;

}
