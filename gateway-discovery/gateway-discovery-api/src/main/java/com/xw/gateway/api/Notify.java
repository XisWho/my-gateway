package com.xw.gateway.api;

/**
 * 监听服务接口
 */
public interface Notify {

	/**
	 * 添加或者更新的方法
	 * @param key
	 * @param value
	 * @throws Exception
	 */
	void put(String key, String value) throws Exception;

	/**
	 * 删除方法
	 * @param key
	 * @throws Exception
	 */
	void delete(String key) throws Exception;

}
