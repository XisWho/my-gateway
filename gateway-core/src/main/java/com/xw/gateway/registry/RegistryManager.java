package com.xw.gateway.registry;

import com.alibaba.fastjson.JSONObject;
import com.xw.gateway.api.Notify;
import com.xw.gateway.api.Registry;
import com.xw.gateway.api.RegistryService;
import com.xw.gateway.config.GatewayConfig;
import com.xw.gateway.constants.BasicConstant;
import com.xw.gateway.constants.Protocol;
import com.xw.gateway.rule.GatewayRule;
import com.xw.gateway.service.*;
import com.xw.gateway.util.FastJsonConvertUtil;
import com.xw.gateway.util.Pair;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * 网关服务的注册中心管理类
 */
@Slf4j
public class RegistryManager {

	private RegistryManager() {
	}

	private static final RegistryManager INSTANCE = new RegistryManager();
	
	public static RegistryManager getInstance() {
		return INSTANCE;
	}
	
	private GatewayConfig gatewayConfig;
	
	private RegistryService registryService;
	
	private static String superPath;
	
	private static String servicesPath;

	private static String instancesPath;

	private static String rulesPath;
	
	private static String gatewaysPath;
	
	private CountDownLatch countDownLatch = new CountDownLatch(1);
	
	public void initialized(GatewayConfig gatewayConfig) throws Exception {
		this.gatewayConfig = gatewayConfig;

		//	1. 路径的设置
		superPath = Registry.PATH + gatewayConfig.getNamespace() + BasicConstant.BAR_SEPARATOR + gatewayConfig.getEnv();
		servicesPath = superPath + Registry.SERVICE_PREFIX;
		instancesPath = superPath + Registry.INSTANCE_PREFIX;
		rulesPath = superPath + Registry.RULE_PREFIX;
		gatewaysPath = superPath + Registry.GATEWAY_PREFIX;
		
		//	2. 初始化加载注册中心对象
		ServiceLoader<RegistryService> serviceLoader = ServiceLoader.load(RegistryService.class);
		for(RegistryService registryService : serviceLoader) {
			registryService.initialized(gatewayConfig.getRegistryAddress());
			this.registryService = registryService;
		}
		
		//	3. 注册监听
		this.registryService.addWatcherListeners(superPath, new ServiceListener());
		
		//	4.订阅服务
		subscribeService();
		
		//	5.注册自身服务
		RegistryServer registryServer = new RegistryServer(registryService);
		registryServer.registerSelf();
	}
	
	/**
	 * <B>方法名称：</B>subscribeService<BR>
	 * <B>概要说明：</B>订阅服务的方法：拉取Etcd注册中心的所有需要使用的元数据信息，解析并放置到缓存中<BR>
	 * 	
	 * 		/RuyuanRapid-dev
	 * 			/services
	 * 				/hello:1.0.0
	 * 				/say:1.0.0
	 * 			/instances
	 * 				/hello:1.0.0/192.168.11.100:1234
	 * 				/hello:1.0.0/192.168.11.101:4321
	 */
	private synchronized void subscribeService() {
		log.info("#RegistryManager#subscribeService  ------------ 	服务订阅开始 	---------------");
		
		try {
			//	1. 加载服务定义和服务实例的集合：获取  servicesPath = /RuyuanRapid-env/services 下面所有的列表
			List<Pair<String, String>> definitionList = this.registryService.getListByPrefixKey(servicesPath);
			
			for(Pair<String, String> definition : definitionList) {
				String definitionPath = definition.getObject1();
				String definitionJson = definition.getObject2();
				
				//	把当前获取的跟目录进行排除
				if(definitionPath.equals(servicesPath)) {
					continue;
				}
				
				//	1.1 加载服务定义集合：
				String uniqueId = definitionPath.substring(servicesPath.length() + 1);
				ServiceDefinition serviceDefinition = parseServiceDefinition(definitionJson);
				DynamicServiceConfigManager.getInstance().putServiceDefinition(uniqueId, serviceDefinition);
				log.info("#RegistryManager#subscribeService 1.1 加载服务定义信息 uniqueId : {}, serviceDefinition : {}", 
						uniqueId,
						FastJsonConvertUtil.convertObjectToJSON(serviceDefinition));
				
				//	1.2 加载服务实例集合：
				//	首先拼接当前服务定义的服务实例前缀路径
				String serviceInstancePrefix = instancesPath + Registry.PATH + uniqueId;
				List<Pair<String, String>> instanceList = this.registryService.getListByPrefixKey(serviceInstancePrefix);
				Set<ServiceInstance> serviceInstanceSet = new HashSet<>();
				for(Pair<String, String> instance : instanceList) {
					String instanceJson = instance.getObject2();
					ServiceInstance serviceInstance = FastJsonConvertUtil.convertJSONToObject(instanceJson, ServiceInstance.class);
					serviceInstanceSet.add(serviceInstance);
				}
				DynamicServiceConfigManager.getInstance().addServiceInstance(uniqueId, serviceInstanceSet);
				log.info("#RegistryManager#subscribeService 1.2 加载服务实例 uniqueId : {}, serviceDefinition : {}", 			
						uniqueId,
						FastJsonConvertUtil.convertObjectToJSON(serviceInstanceSet));

			}
			
			//	2. 加载规则集合
			List<Pair<String, String>> ruleList = this.registryService.getListByPrefixKey(rulesPath);
			for(Pair<String, String> r: ruleList) {
				String rulePath = r.getObject1();
				String ruleJson = r.getObject2();
				if(rulePath.endsWith(rulesPath)) {
					continue;
				}
				GatewayRule rule = FastJsonConvertUtil.convertJSONToObject(ruleJson, GatewayRule.class);
				DynamicServiceConfigManager.getInstance().putRule(rule.getId(), rule);
				log.info("#RegistryManager#subscribeService 2 加载规则信息 ruleId : {}, rule : {}", 			
						rule.getId(),
						FastJsonConvertUtil.convertObjectToJSON(rule));				
			}
		} catch (Exception e) {
			log.error("#RegistryManager#subscribeService 服务订阅失败 ", e);
		} finally {
			countDownLatch.countDown();
			log.info("#RegistryManager#subscribeService  ------------ 	服务订阅结束 	---------------");
		}
	}

	/**
	 * <B>方法名称：</B>parseServiceDefinition<BR>
	 * <B>概要说明：</B>把从注册中心拉取过来的json字符串 转换成指定的ServiceDefinition<BR>
	 * @param definitionJson
	 * @return ServiceDefinition
	 */
	@SuppressWarnings("unchecked")
	private ServiceDefinition parseServiceDefinition(String definitionJson) {
		Map<String, Object> jsonMap = FastJsonConvertUtil.convertJSONToObject(definitionJson, Map.class);
		ServiceDefinition serviceDefinition = new ServiceDefinition();

		//	填充serviceDefinition
		serviceDefinition.setUniqueId((String)jsonMap.get("uniqueId"));
		serviceDefinition.setServiceId((String)jsonMap.get("serviceId"));
		serviceDefinition.setProtocol((String)jsonMap.get("protocol"));
		serviceDefinition.setPatternPath((String)jsonMap.get("patternPath"));
		serviceDefinition.setVersion((String)jsonMap.get("version"));
		serviceDefinition.setEnable((boolean)jsonMap.get("enable"));
		serviceDefinition.setEnvType((String)jsonMap.get("envType"));
		
		Map<String, ServiceInvoker> invokerMap = new HashMap<String, ServiceInvoker>();
		JSONObject jsonInvokerMap = (JSONObject)jsonMap.get("invokerMap");
		
		switch (serviceDefinition.getProtocol()) {
			case Protocol.HTTP:
				Map<String, Object> httpInvokerMap = FastJsonConvertUtil.convertJSONToObject(jsonInvokerMap, Map.class);
				for(Map.Entry<String, Object> me : httpInvokerMap.entrySet()) {
					String path = me.getKey();
					JSONObject jsonInvoker = (JSONObject)me.getValue();
					HttpServiceInvoker httpServiceInvoker = FastJsonConvertUtil.convertJSONToObject(jsonInvoker, HttpServiceInvoker.class);
					invokerMap.put(path, httpServiceInvoker);
				}
				break;
			case Protocol.DUBBO:
				Map<String, Object> dubboInvokerMap = FastJsonConvertUtil.convertJSONToObject(jsonInvokerMap, Map.class);
				for(Map.Entry<String, Object> me : dubboInvokerMap.entrySet()) {
					String path = me.getKey();
					JSONObject jsonInvoker = (JSONObject)me.getValue();
					DubboServiceInvoker dubboServiceInvoker = FastJsonConvertUtil.convertJSONToObject(jsonInvoker, DubboServiceInvoker.class);
					invokerMap.put(path, dubboServiceInvoker);
				}
				break;
			default:
				break;
		}
		
		serviceDefinition.setInvokerMap(invokerMap);
		return serviceDefinition;
	}

	class ServiceListener implements Notify {
		@Override
		public void put(String key, String value) throws Exception {
			countDownLatch.await();
			if(servicesPath.equals(key) ||
					instancesPath.equals(key) ||
					rulesPath.equals(key)) {
				return;
			}
			
			//	如果是服务定义发生变更
			if(key.contains(servicesPath)) {
				String uniqueId = key.substring(servicesPath.length() + 1);
				//	ServiceDefinition
				ServiceDefinition serviceDefinition = parseServiceDefinition(value);
				DynamicServiceConfigManager.getInstance().putServiceDefinition(uniqueId, serviceDefinition);
				return;
			}

			//	如果是服务实例发生变更
			if(key.contains(instancesPath)) {
				//	ServiceInstance
				//			hello:1.0.0/192.168.11.100:1234
				String temp = key.substring(instancesPath.length() + 1);
				String[] tempArray = temp.split(Registry.PATH);
				if(tempArray.length == 2) {
					String uniqueId = tempArray[0];
					ServiceInstance serviceInstance = FastJsonConvertUtil.convertJSONToObject(value, ServiceInstance.class);
					DynamicServiceConfigManager.getInstance().updateServiceInstance(uniqueId, serviceInstance);
				}
				return;
			}

			//	如果是规则发生变更
			if(key.contains(rulesPath)) {
				//	Rule
				String ruleId = key.substring(rulesPath.length() + 1);
				GatewayRule rule = FastJsonConvertUtil.convertJSONToObject(value, GatewayRule.class);
				DynamicServiceConfigManager.getInstance().putRule(ruleId, rule);
			}
		}

		@Override
		public void delete(String key) throws Exception {
			countDownLatch.await();
			
			if(servicesPath.equals(key) ||
					instancesPath.equals(key) ||
					rulesPath.equals(key)) {
				return;
			}
			
			//	如果是服务定义发生变更
			if(key.contains(servicesPath)) {
				String uniqueId = key.substring(servicesPath.length() + 1);
				DynamicServiceConfigManager.getInstance().removeServiceDefinition(uniqueId);
				DynamicServiceConfigManager.getInstance().removeServiceInstancesByUniqueId(uniqueId);
				return;
			}

			//	如果是服务实例发生变更
			if(key.contains(instancesPath)) {
				//	hello:1.0.0/192.168.11.100:1234
				String temp = key.substring(instancesPath.length() + 1);
				String[] tempArray = temp.split(Registry.PATH);
				if(tempArray.length == 2) {
					String uniqueId = tempArray[0];
					String serviceInstanceId = tempArray[1];
					DynamicServiceConfigManager.getInstance().removeServiceInstance(uniqueId, serviceInstanceId);
				}				
				return;
			}

			//	如果是规则发生变更：
			if(key.contains(rulesPath)) {
				String ruleId = key.substring(rulesPath.length() + 1);
				DynamicServiceConfigManager.getInstance().removeRule(ruleId);
			}
		}
	} 
	
	/**
	 * <B>主类名称：</B>RegistryServer<BR>
	 * <B>概要说明：</B>网关自身注册服务<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 下午11:21:45
	 */
	class RegistryServer {
		
		private RegistryService registryService;
		
		private String selfPath;
		
		public RegistryServer(RegistryService registryService) throws Exception {
			this.registryService = registryService;
			this.registryService.registerPathIfNotExists(superPath, "", true);
			this.registryService.registerPathIfNotExists(gatewaysPath, "", true);
			this.selfPath = gatewaysPath + Registry.PATH + gatewayConfig.getRapidId();
		}
		
		public void registerSelf() throws Exception {
			String rapidConfigJson = FastJsonConvertUtil.convertObjectToJSON(gatewayConfig);
			this.registryService.registerPathIfNotExists(selfPath, rapidConfigJson, false);
		}
	}

	public static String getSuperPath() {
		return superPath;
	}

	public static String getServicesPath() {
		return servicesPath;
	}

	public static String getInstancesPath() {
		return instancesPath;
	}

	public static String getRulesPath() {
		return rulesPath;
	}

	public static String getGatewaysPath() {
		return gatewaysPath;
	}
	
}
