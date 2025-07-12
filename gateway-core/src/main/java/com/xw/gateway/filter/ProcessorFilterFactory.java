package com.xw.gateway.filter;

import com.xw.gateway.context.Context;

import java.util.List;

/**
 * 过滤器工厂接口
 */
public interface ProcessorFilterFactory {

    /**
     * 根据过滤器类型，添加一组过滤器，用于构建过滤器链
     * @param filterType
     * @param filters
     * @throws Exception
     */
    void buildFilterChain(ProcessorFilterType filterType, List<ProcessorFilter<Context>> filters);

    /**
     * 正常情况下执行过滤器链
     * @param ctx
     * @throws Exception
     */
    void doFilterChain(Context ctx);

    /**
     * 异常情况下执行过滤器链
     * @param ctx
     * @throws Exception
     */
    void doErrorFilterChain(Context ctx);

    /**
     * 获取指定类类型的过滤器
     * @param t
     * @return
     * @param <T>
     * @throws Exception
     */
    <T> T getFilter(Class<T> t);

    /**
     * 获取指定ID的过滤器
     * @param filterId
     * @return
     * @param <T>
     * @throws Exception
     */
    <T> T getFilter(String filterId);

}
