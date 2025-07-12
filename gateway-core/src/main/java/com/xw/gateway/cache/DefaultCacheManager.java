package com.xw.gateway.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存管理类
 */
public class DefaultCacheManager {

	private static final DefaultCacheManager INSTANCE = new DefaultCacheManager();

	private DefaultCacheManager() {
	}
	
	public static final String FILTER_CONFIG_CACHE_ID = "filterConfigCache";
	
	//	这个是全局的缓存：双层缓存
	private final ConcurrentHashMap<String, Cache<String, ?>> cacheMap = new ConcurrentHashMap<>();
	
	public static DefaultCacheManager getInstance() {
		return INSTANCE;
	}

	/**
	 * 根据一个全局的缓存ID 创建一个Caffeine缓存对象
	 * @param cacheId
	 * @return
	 * @param <V>
	 */
	@SuppressWarnings("unchecked")
	public <V> Cache<String, V> create(String cacheId) {
		Cache<String, V> cache = Caffeine.newBuilder().build();
		cacheMap.put(cacheId, cache);
		return (Cache<String, V>) cacheMap.get(cacheId);
	}

	/**
	 * 根据cacheId和对应的真正Caffeine缓存key，删除一个Caffeine缓存对象
	 * @param cacheId
	 * @param key
	 * @param <V>
	 */
	public <V> void remove(String cacheId, String key) {
		Cache<String, V> cache = (Cache<String, V>) cacheMap.get(cacheId);
		if(cache != null) {
			cache.invalidate(key);
		}
	}

	/**
	 * 根据全局的缓存id 删除这个Caffeine缓存对象
	 * @param cacheId
	 * @param <V>
	 */
	public <V> void remove(String cacheId) {
		@SuppressWarnings("unchecked")
		Cache<String, V> cache = (Cache<String, V>) cacheMap.get(cacheId);
		if(cache != null) {
			cache.invalidateAll();
		}
	}

	/**
	 * 清空所有的缓存
	 */
	public void cleanAll() {
		cacheMap.values().forEach(cache -> cache.invalidateAll());
	}
	
}
