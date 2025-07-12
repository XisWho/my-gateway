package com.xw.gateway.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.xw.constants.BasicConstant;
import com.xw.rule.GatewayRule.FilterConfig;
import com.xw.gateway.cache.DefaultCacheManager;
import com.xw.gateway.context.Context;
import com.xw.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public abstract class AbstractEntryProcessorFilter<T> extends AbstractLinkedProcessorFilter<Context> {

    protected Filter filterAnnotation;

    protected Cache<String, T> cache;

    protected final Class<T> filterConfigClass;

    public AbstractEntryProcessorFilter(Class<T> filterConfigClass) {
        this.filterAnnotation = this.getClass().getAnnotation(Filter.class);
        this.filterConfigClass = filterConfigClass;
        this.cache = DefaultCacheManager.getInstance().create(DefaultCacheManager.FILTER_CONFIG_CACHE_ID);
    }

    @Override
    public boolean check(Context ctx) {
        return ctx.getRule().hashId(filterAnnotation.id());
    }

    @Override
    public void prepareForFilter(Context ctx, Object... args) {
        T filterConfigClass = dynamicLoadCache(ctx, args);
        super.prepareForFilter(ctx, filterConfigClass);
    }

    /**
     * 动态加载缓存：每一个过滤器的具体配置规则
     * @param ctx
     * @param args
     * @return
     */
    private T dynamicLoadCache(Context ctx, Object[] args) {
        //	通过上下文对象拿到规则，再通过规则获取到指定filterId的FilterConfig
        FilterConfig filterConfig = ctx.getRule().getFilterConfig(filterAnnotation.id());

        //	定义一个cacheKey：
        String ruleId = ctx.getRule().getId();
        String cacheKey = ruleId + BasicConstant.DOLLAR_SEPARATOR + filterAnnotation.id();

        T fcc = cache.getIfPresent(cacheKey);
        if (fcc == null) {
            if(filterConfig != null && StringUtils.isNotEmpty(filterConfig.getConfig())) {
                String configStr = filterConfig.getConfig();
                try {
                    fcc = JSONUtil.parse(configStr, filterConfigClass);
                    cache.put(cacheKey, fcc);
                } catch (Exception e) {
                    log.error("#AbstractEntryProcessorFilter# dynamicLoadCache filterId: {}, config parse error: {}",
                            filterAnnotation.id(),
                            configStr,
                            e);
                }
            }
        }
        return fcc;
    }


}
