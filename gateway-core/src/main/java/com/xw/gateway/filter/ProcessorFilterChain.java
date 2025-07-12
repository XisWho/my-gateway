package com.xw.gateway.filter;

/**
 * 过滤器链表的抽象接口
 * @param <T>
 */
public abstract class ProcessorFilterChain<T> extends AbstractLinkedProcessorFilter<T> {

    /**
     * 在链表的头部添加元素
     * @param filter
     */
    public abstract void addFirst(AbstractLinkedProcessorFilter<T> filter);

    /**
     * 在链表的尾部添加元素
     * @param filter
     */
    public abstract void addLast(AbstractLinkedProcessorFilter<T> filter);

}

