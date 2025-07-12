package com.xw.gateway.filter;

import com.xw.gateway.context.Context;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽象过滤器工厂类
 */
public abstract class AbstractProcessorFilterFactory implements ProcessorFilterFactory {

    /*
     *	pre + route + post
     */
    public DefaultProcessorFilterChain defaultProcessorFilterChain = new DefaultProcessorFilterChain("defaultProcessorFilterChain");

    /*
     * 	error + post
     */
    public DefaultProcessorFilterChain errorProcessorFilterChain = new DefaultProcessorFilterChain("errorProcessorFilterChain");

    /*
     * 	根据过滤器类型获取filter集合
     */
    public Map<String /* processorFilterType */, Map<String /* filterId */, ProcessorFilter<Context>>> processorFilterTypeMap = new LinkedHashMap<>();

    /*
     * 	根据过滤器id获取对应的Filter
     */
    public Map<String /* filterId */, ProcessorFilter<Context>> processorFilterIdMap = new LinkedHashMap<>();

    /**
     * 构建过滤器链条
     * @param filterType
     * @param filters
     * @throws Exception
     */
    @Override
    public void buildFilterChain(ProcessorFilterType filterType, List<ProcessorFilter<Context>> filters) {
        switch (filterType) {
            case PRE:
            case ROUTE:
                addFilterForChain(defaultProcessorFilterChain, filters);
                break;
            case ERROR:
                addFilterForChain(errorProcessorFilterChain, filters);
                break;
            case POST:
                addFilterForChain(defaultProcessorFilterChain, filters);
                addFilterForChain(errorProcessorFilterChain, filters);
            default:
                throw new RuntimeException("ProcessorFilterType is not supported !");
        }
    }

    private void addFilterForChain(DefaultProcessorFilterChain processorFilterChain,
                                   List<ProcessorFilter<Context>> filters) {
        for (ProcessorFilter<Context> processorFilter : filters) {
            processorFilter.init();
            doAddFilter(processorFilterChain, processorFilter);
        }
    }

    /**
     * 添加过滤器到指定的filterChain
     * @param processorFilterChain
     * @param processorFilter
     */
    private void doAddFilter(DefaultProcessorFilterChain processorFilterChain,
                           ProcessorFilter<Context> processorFilter) {
        Filter annotation = processorFilter.getClass().getAnnotation(Filter.class);

        if (annotation != null) {
            //	构建过滤器链条，添加filter
            processorFilterChain.addLast((AbstractLinkedProcessorFilter<Context>)processorFilter);

            //	映射到过滤器集合
            String filterId = annotation.id();
            if(filterId == null || filterId.length() < 1) {
                filterId = processorFilter.getClass().getName();
            }
            String code = annotation.value().getCode();
            Map<String, ProcessorFilter<Context>> filterMap = processorFilterTypeMap.get(code);
            if (filterMap == null) {
                filterMap = new LinkedHashMap<String, ProcessorFilter<Context>>();
            }
            filterMap.put(filterId, processorFilter);

            //	type
            processorFilterTypeMap.put(code, filterMap);
            //	id
            processorFilterIdMap.put(filterId, processorFilter);
        }

    }

    public <T> T getFilter(Class<T> t) {
        Filter annotation = t.getAnnotation(Filter.class);
        if(annotation != null) {
            String filterId = annotation.id();
            if(filterId == null || filterId.length() < 1) {
                filterId = t.getName();
            }
            return this.getFilter(filterId);
        }
        return null;
    }

    public <T> T getFilter(String filterId) {
        ProcessorFilter<Context> filter = null;
        if(!processorFilterIdMap.isEmpty()) {
            filter = processorFilterIdMap.get(filterId);
        }
        return (T)filter;
    }

}
