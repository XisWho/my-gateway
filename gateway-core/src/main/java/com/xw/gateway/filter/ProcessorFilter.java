package com.xw.gateway.filter;

public interface ProcessorFilter<T> {

    /**
     * 过滤器是否需要执行的校验方法
     * @param t
     * @return
     * @throws Throwable
     */
    boolean check(T t);

    /**
     * 在执行过滤方法之前做一些准备工作
     * @param t
     * @param args
     * @throws Throwable
     */
    void prepareForFilter(T t, Object... args);

    /**
     * 真正执行过滤器的方法
     * @param t
     * @param args
     * @throws Throwable
     */
    void filter(T t, Object... args);

    /**
     * 触发下一个过滤器执行
     * @param t
     * @param args
     * @throws Throwable
     */
    void fireNext(T t, Object... args);


   /**
     * 过滤器初始化的方法，如果子类有需求则进行覆盖
     * @throws Exception
     */
    default void init() {}

    /**
     * 过滤器销毁的方法，如果子类有需求则进行覆盖
     * @throws Exception
     */
    default void destroy() {}

    /**
     * 过滤器刷新的方法，如果子类有需求则进行覆盖
     * @throws Exception
     */
    default void refresh() {}

}
