package com.xw.gateway.api;

import com.xw.gateway.util.Pair;

import java.util.List;

/**
 * 注册接口
 */
public interface Registry {

	/**
	 * 	/services: 是要存储所有的服务定义信息的：ServiceDefinition (永久存储)
	 */
	String SERVICE_PREFIX = "/services";
	
	/**
	 * 	/instances: 是要存储所有的服务实例信息的： ServiceInstance (加载时存储)
	 */
	String INSTANCE_PREFIX = "/instances";
	
	/**
	 * 	/rules: 是要存储所有的规则信息的：Rule (永久存储)
	 */
	String RULE_PREFIX = "/rules";
	
	/**
	 * 	/gateway: 这个是要存储所有的网关本身自注册信息的： rapid-core(网关服务本身，加载时存储)
	 */
	String GATEWAY_PREFIX = "/gateway";
	
	String PATH = "/";

	/**
	 * 注册一个不存在的路径
	 * @param path
	 * @param value
	 * @param isPersistent
	 * @throws Exception
	 */
	void registerPathIfNotExists(String path, String value, boolean isPersistent) throws Exception;

	/**
	 * 注册一个临时节点
	 * @param key
	 * @param value
	 * @return
	 * @throws Exception
	 */
	long registerEphemeralNode(String key, String value) throws Exception;

	/**
	 * 注册一个永久节点
	 * @param key
	 * @param value
	 * @throws Exception
	 */
	void registerPersistentNode(String key, String value) throws Exception;

	/**
	 * 通过一个前缀路径，获取一堆对应的数据信息
	 * @param prefix
	 * @return
	 * @throws Exception
	 */
	List<Pair<String, String>> getListByPrefixKey(String prefix) throws Exception;

	/**
	 * 通过一个key查询对应键值对对象
	 * @param key
	 * @return
	 * @throws Exception
	 */
	Pair<String, String> getByKey(String key) throws Exception;

	/**
	 * 根据一个key键判断是否存在
	 * @param key
	 * @return
	 * @throws Exception
	 */
	boolean isExistKey(String key) throws Exception;

	/**
	 * 根据key删除
	 * @param key
	 */
	void deleteByKey(String key) throws Exception;

	/**
	 * 关闭服务
	 */
	void close() throws Exception;
	
}
