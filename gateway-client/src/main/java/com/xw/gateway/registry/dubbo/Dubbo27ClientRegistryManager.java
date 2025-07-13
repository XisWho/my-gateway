package com.xw.gateway.registry.dubbo;

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
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.spring.ServiceBean;
import org.apache.dubbo.config.spring.context.event.ServiceBeanExportedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

/**
 * dubbo 2.7.x 客户端注册管理类实现
 */
@Slf4j
public class Dubbo27ClientRegistryManager extends AbstractClientRegistryManager implements EnvironmentAware, ApplicationListener<ApplicationEvent> {

	public Dubbo27ClientRegistryManager(GatewayProperties gatewayProperties) throws Exception {
		super(gatewayProperties);
	}
	
	private Environment environment;
	
	private static final Set<Object> uniqueBeanSet = new HashSet<>();

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
	
	@PostConstruct
	private void init() {
		String port = environment.getProperty(DubboConstants.DUBBO_PROTOCOL_PORT);
		if (StringUtils.isEmpty(port)) {
			log.error("Dubbo服务未指定端口");
			return;
		}
		isStart = true;
	}
	
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (!isStart) {
			return;
		}
		// 服务发布事件
		if (event instanceof ServiceBeanExportedEvent) {
			ServiceBean<?> serviceBean = ((ServiceBeanExportedEvent)event).getServiceBean();
			try {
				registeryServiceBean(serviceBean);
			} catch (Exception e) {
				log.error("Rapid Dubbo 注册服务ServiceBean 失败，ServiceBean = {}", serviceBean, e);
			}
		} else if(event instanceof ApplicationStartedEvent){
			//	START:::
			System.err.println("******************************************");
			System.err.println("**        Rapid Dubbo Started           **");
			System.err.println("******************************************");
		}
	}

	/**
	 * 注册Dubbo服务：从ServiceBeanExportedEvent获取ServiceBean对象
	 * @param serviceBean
	 * @throws Exception
	 */
	private void registeryServiceBean(ServiceBean<?> serviceBean) throws Exception {
		
		Object bean = serviceBean.getRef();
		if (uniqueBeanSet.add(bean)) {
			ServiceDefinition serviceDefinition = GatewayServiceAnnotationScanner.getInstance().scanbuilder(bean, serviceBean);
			if (serviceDefinition != null) {
				//	设置环境
				serviceDefinition.setEnvType(getEnv());
				//	注册服务定义
				registerServiceDefinition(serviceDefinition);
				
				//	注册服务实例：
				ServiceInstance serviceInstance = new ServiceInstance();
				String localIp = NetUtils.getLocalIp();
				int port = serviceBean.getProtocol().getPort();
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
