package com.xw.gateway.api;

/**
 * 注册服务接口
 */
public interface RegistryService extends Registry {

	/**
	 * 初始化注册服务
	 * @param registryAddress
	 */
	void initialized(String registryAddress);

	/**
	 * 添加一堆的监听事件
	 * @param superPath
	 * @param notify
	 */
	void addWatcherListeners(String superPath, Notify notify);

}
