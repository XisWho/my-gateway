package com.xw.gateway.registry.springmvc;

import com.xw.gateway.constants.BasicConstant;
import com.xw.gateway.constants.GatewayConstant;
import com.xw.gateway.autoconfigure.GatewayProperties;
import com.xw.gateway.registry.AbstractClientRegistryManager;
import com.xw.gateway.registry.GatewayServiceAnnotationScanner;
import com.xw.gateway.service.ServiceDefinition;
import com.xw.gateway.service.ServiceInstance;
import com.xw.gateway.util.NetUtils;
import com.xw.gateway.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Http请求的客户端注册管理器
 */
@Slf4j
public class SpringMVCClientRegistryManager extends AbstractClientRegistryManager implements ApplicationListener<ApplicationEvent>, ApplicationContextAware  {

	ApplicationContext applicationContext;
	
	@Autowired
	private ServerProperties serverProperties;
	
	private static final Set<Object> uniqueBeanSet = new HashSet<>();
	
	public SpringMVCClientRegistryManager(GatewayProperties gatewayProperties) throws Exception {
		super(gatewayProperties);
	}
	
	@PostConstruct
	private void init() {
		// 如果当前验证属性都不为空，就进行初始化
		if (!ObjectUtils.allNotNull(serverProperties, serverProperties.getPort())) {
			log.error("服务配置缺失");
			return;
		}
		isStart = true;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (!isStart) {
			return;
		}

		// SpringMVC事件
		if (event instanceof WebServerInitializedEvent ||
				event instanceof ServletWebServerInitializedEvent) {
			try {
				registerySpringMVC();
			} catch (Exception e) {
				log.error("#SpringMVCClientRegisteryManager# registerySpringMVC error", e);
			}
		} else if (event instanceof ApplicationStartedEvent){
			//	START:::
			System.err.println("******************************************");
			System.err.println("**        Rapid SpringMVC Started       **");
			System.err.println("******************************************");
		}
	}

	/**
	 * 解析SpringMvc的事件，进行注册
	 * @throws Exception
	 */
	private void registerySpringMVC() throws Exception {
		// RequestMappingHandlerMapping是Spring MVC中的一个请求映射处理器，它负责将HTTP请求映射到特定的@RequestMapping注解的方法上。
		// 允许你使用简单的注解（如@GetMapping、@PostMapping、@RequestMapping等）来定义请求路径和HTTP方法。
		Map<String, RequestMappingHandlerMapping> allRequestMappings = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, 
				RequestMappingHandlerMapping.class, true, false);
		
		for (RequestMappingHandlerMapping handlerMapping : allRequestMappings.values()) {
			Map<RequestMappingInfo, HandlerMethod> map = handlerMapping.getHandlerMethods();
			for (Map.Entry<RequestMappingInfo, HandlerMethod> me : map.entrySet()) {
				HandlerMethod handlerMethod = me.getValue();
				Class<?> clazz = handlerMethod.getBeanType();
				Object bean = applicationContext.getBean(clazz);
				//	如果当前Bean对象已经加载则不需要做任何事
				if (uniqueBeanSet.add(bean)) {
					ServiceDefinition serviceDefinition = GatewayServiceAnnotationScanner.getInstance().scanbuilder(bean);
					if (serviceDefinition != null) {
						//	设置环境
						serviceDefinition.setEnvType(getEnv());
						//	注册服务定义
						registerServiceDefinition(serviceDefinition);
						
						//	注册服务实例
						ServiceInstance serviceInstance = new ServiceInstance();
						String localIp = NetUtils.getLocalIp();
						int port = serverProperties.getPort();
						String serviceInstanceId = localIp + BasicConstant.COLON_SEPARATOR + port;
						String address = serviceInstanceId;
						String uniqueId = serviceDefinition.getUniqueId();
						String version = serviceDefinition.getVersion();
						
						serviceInstance.setServiceInstanceId(serviceInstanceId);
						serviceInstance.setUniqueId(uniqueId);
						serviceInstance.setAddress(address);
						serviceInstance.setWeight(GatewayConstant.DEFAULT_WEIGHT);
						serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
						serviceInstance.setVersion(version);
						
						registerServiceInstance(serviceInstance);
					}
				}
			}
		}
	}
	
}
