package com.xw.gateway.registry;

import com.xw.gateway.constants.BasicConstant;
import com.xw.gateway.annotation.GatewayService;
import com.xw.gateway.annotation.GatewayServiceInvoker;
import com.xw.gateway.annotation.Protocol;
import com.xw.gateway.registry.dubbo.DubboConstants;
import com.xw.gateway.service.DubboServiceInvoker;
import com.xw.gateway.service.HttpServiceInvoker;
import com.xw.gateway.service.ServiceDefinition;
import com.xw.gateway.service.ServiceInvoker;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.spring.ServiceBean;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 扫描注解类, 用于扫描所有的用户定义的 @GatewayService 和 @GatewayServiceInvoker
 */
public class GatewayServiceAnnotationScanner {

    private static final GatewayServiceAnnotationScanner INSTANCE = new GatewayServiceAnnotationScanner();

    private GatewayServiceAnnotationScanner() {
    }

    public static GatewayServiceAnnotationScanner getInstance() {
        return INSTANCE;
    }

    /**
     * 扫描传入的Bean对象，最终返回一个ServiceDefinition
     * @param bean
     * @param args
     * @return
     */
    public synchronized ServiceDefinition scanbuilder(Object bean, Object... args) {
        Class<?> clazz = bean.getClass();
        boolean isPresent = clazz.isAnnotationPresent(GatewayService.class);
        if (isPresent) {
            GatewayService gatewayService = clazz.getAnnotation(GatewayService.class);
            String serviceId = gatewayService.serviceId();
            Protocol protocol = gatewayService.protocol();
            String patternPath = gatewayService.patternPath();
            String version = gatewayService.version();

            ServiceDefinition serviceDefinition = new ServiceDefinition();
            Map<String /* invokerPath */, ServiceInvoker> invokerMap = new HashMap<String, ServiceInvoker>();

            Method[] methods = clazz.getMethods();
            if (methods != null && methods.length > 0) {
                for (Method method : methods) {
                    GatewayServiceInvoker serviceInvoker = method.getAnnotation(GatewayServiceInvoker.class);
                    if (serviceInvoker == null) {
                        continue;
                    }
                    String path = serviceInvoker.path();

                    switch (protocol) {
                        case HTTP:
                            HttpServiceInvoker httpServiceInvoker = createHttpServiceInvoker(path, bean, method);
                            invokerMap.put(path, httpServiceInvoker);
                            break;
                        case DUBBO:
                            ServiceBean<?> serviceBean = (ServiceBean<?>)args[0];
                            DubboServiceInvoker dubboServiceInvoker = createDubboServiceInvoker(path, serviceBean, method);
                            //	serviceDefinition version 重置为 dubbo version
                            String dubboVersion = dubboServiceInvoker.getVersion();
                            if(!StringUtils.isBlank(dubboVersion)) {
                                version = dubboVersion;
                            }
                            invokerMap.put(path, dubboServiceInvoker);
                            break;
                        default:
                            break;
                    }
                }
            }
            //	设置属性
            serviceDefinition.setUniqueId(serviceId + BasicConstant.COLON_SEPARATOR + version);
            serviceDefinition.setServiceId(serviceId);
            serviceDefinition.setVersion(version);
            serviceDefinition.setProtocol(protocol.getCode());
            serviceDefinition.setPatternPath(patternPath);
            serviceDefinition.setEnable(true);
            serviceDefinition.setInvokerMap(invokerMap);
            return serviceDefinition;
        }

        return null;
    }

    /**
     * 构建HttpServiceInvoker对象
     * @param path
     * @param bean
     * @param method
     * @return
     */
    private HttpServiceInvoker createHttpServiceInvoker(String path, Object bean, Method method) {
        HttpServiceInvoker httpServiceInvoker = new HttpServiceInvoker();
        httpServiceInvoker.setInvokerPath(path);
        return httpServiceInvoker;
    }

    /**
     * 构建DubboServiceInvoker对象
     * @param path
     * @param serviceBean
     * @param method
     * @return
     */
    private DubboServiceInvoker createDubboServiceInvoker(String path, ServiceBean<?> serviceBean, Method method) {
        DubboServiceInvoker dubboServiceInvoker = new DubboServiceInvoker();
        dubboServiceInvoker.setInvokerPath(path);

        String methodName = method.getName();
        String registerAddress = serviceBean.getRegistry().getAddress();
        String interfaceClass = serviceBean.getInterface();

        dubboServiceInvoker.setRegisterAddress(registerAddress);
        dubboServiceInvoker.setMethodName(methodName);
        dubboServiceInvoker.setInterfaceClass(interfaceClass);

        String[] parameterTypes = new String[method.getParameterCount()];
        Class<?>[] classes = method.getParameterTypes();
        for(int i = 0; i < classes.length; i ++) {
            parameterTypes[i] = classes[i].getName();
        }
        dubboServiceInvoker.setParameterTypes(parameterTypes);

        Integer seriveTimeout = serviceBean.getTimeout();
        if(seriveTimeout == null || seriveTimeout.intValue() == 0) {
            ProviderConfig providerConfig = serviceBean.getProvider();
            if(providerConfig != null) {
                Integer providerTimeout = providerConfig.getTimeout();
                if(providerTimeout == null || providerTimeout.intValue() == 0) {
                    seriveTimeout = DubboConstants.DUBBO_TIMEOUT;
                } else {
                    seriveTimeout = providerTimeout;
                }
            }
        }
        dubboServiceInvoker.setTimeout(seriveTimeout);

        String dubboVersion = serviceBean.getVersion();
        dubboServiceInvoker.setVersion(dubboVersion);

        return dubboServiceInvoker;
    }

}
