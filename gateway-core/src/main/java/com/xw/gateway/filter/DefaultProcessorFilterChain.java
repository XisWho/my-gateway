package com.xw.gateway.filter;

import com.xw.gateway.context.Context;

/**
 * 默认过滤器链
 */
public class DefaultProcessorFilterChain extends ProcessorFilterChain<Context> {

    private final String id;

    public DefaultProcessorFilterChain(String id) {
        this.id = id;
    }

    /**
     * 	虚拟头结点：dummyHead
     */
    private AbstractLinkedProcessorFilter<Context> first = new AbstractLinkedProcessorFilter<Context>() {
        @Override
        public boolean check(Context ctx) {
            return true;
        }

        @Override
        public void filter(Context ctx, Object... args) {
            fireNext(ctx, args);
        }
    };

    /**
     * 	尾节点
     */
    private AbstractLinkedProcessorFilter<Context> end = first;

    @Override
    public void addFirst(AbstractLinkedProcessorFilter<Context> filter) {
        filter.setNext(first.getNext());
        first.setNext(filter);
        if (end == first) {
            end = filter;
        }
    }

    @Override
    public void addLast(AbstractLinkedProcessorFilter<Context> filter) {
        end.setNext(filter);
        end = filter;
    }

    @Override
    public void setNext(AbstractLinkedProcessorFilter<Context> filter) {
        addLast(filter);
    }

    @Override
    public AbstractLinkedProcessorFilter<Context> getNext() {
        return first.getNext();
    }

    @Override
    public boolean check(Context ctx) {
        return true;
    }

    @Override
    public void filter(Context ctx, Object... args) {
        first.prepareForFilter(ctx, args);
    }

    public String getId() {
        return id;
    }

}