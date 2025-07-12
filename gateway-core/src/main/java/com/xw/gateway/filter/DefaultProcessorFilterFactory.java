package com.xw.gateway.filter;

import com.xw.gateway.context.Context;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 默认过滤器工厂实现类
 */
@Slf4j
public class DefaultProcessorFilterFactory extends AbstractProcessorFilterFactory {

    private static final DefaultProcessorFilterFactory INSTANCE = new DefaultProcessorFilterFactory();

    public static DefaultProcessorFilterFactory getInstance() {
        return INSTANCE;
    }

    //	构造方法：加载所有的ProcessorFilter子类的实现
    private DefaultProcessorFilterFactory(){
        //	SPI方式加载filter的集合：
        Map<String , List<ProcessorFilter<Context>>> filterMap = new LinkedHashMap<String, List<ProcessorFilter<Context>>>();

        //	通过ServiceLoader加载
        ServiceLoader<ProcessorFilter> serviceLoader = ServiceLoader.load(ProcessorFilter.class);
        for (ProcessorFilter<Context> filter : serviceLoader) {
            Filter annotation = filter.getClass().getAnnotation(Filter.class);
            if (annotation != null) {
                String filterType = annotation.value().getCode();
                List<ProcessorFilter<Context>> filterList = filterMap.get(filterType);
                if (filterList == null) {
                    filterList = new ArrayList<ProcessorFilter<Context>>();
                }
                filterList.add(filter);
                filterMap.put(filterType, filterList);
            }
        }

        //	java基础：枚举类循环也是有顺序的
        for (ProcessorFilterType filterType : ProcessorFilterType.values()) {
            List<ProcessorFilter<Context>> filterList = filterMap.get(filterType.getCode());
            if(filterList == null || filterList.isEmpty()) {
                continue;
            }

            Collections.sort(filterList, new Comparator<ProcessorFilter<Context>>() {
                @Override
                public int compare(ProcessorFilter<Context> o1, ProcessorFilter<Context> o2) {
                    return o1.getClass().getAnnotation(Filter.class).order() -
                            o2.getClass().getAnnotation(Filter.class).order();
                }
            });

            buildFilterChain(filterType, filterList);
        }

    }

    /**
     * 正常过滤器链条执行：pre + route + post
     * @param ctx
     * @throws Exception
     */
    @Override
    public void doFilterChain(Context ctx) {
        try {
            defaultProcessorFilterChain.filter(ctx);
        } catch (Throwable e) {
            log.error("#DefaultProcessorFilterFactory.doFilterChain# ERROR MESSAGE: {}" , e.getMessage(), e);

            //	设置异常
            ctx.setThrowable(e);

            //	TODO: 执行doFilterChain抛出异常时，还有关键的一个步骤没有去做

            //	执行异常处理的过滤器链条
            doErrorFilterChain(ctx);
        }
    }

    /**
     * 异常过滤器链条执行：error + post
     * @param ctx
     */
    @Override
    public void doErrorFilterChain(Context ctx) {
        try {
            errorProcessorFilterChain.filter(ctx);
        } catch (Throwable e) {
            log.error("#DefaultProcessorFilterFactory.doErrorFilterChain# ERROR MESSAGE: {}" , e.getMessage(), e);
        }
    }

}
