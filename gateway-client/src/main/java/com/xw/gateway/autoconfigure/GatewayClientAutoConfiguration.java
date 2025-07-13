package com.xw.gateway.autoconfigure;

import javax.servlet.Servlet;

import com.xw.gateway.registry.dubbo.Dubbo27ClientRegistryManager;
import com.xw.gateway.registry.springmvc.SpringMVCClientRegistryManager;
import org.apache.dubbo.config.spring.ServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
@ConditionalOnProperty(prefix = GatewayProperties.PREFIX, name = {"registryAddress", "namespace"})
public class GatewayClientAutoConfiguration {

    @Autowired
    private GatewayProperties gatewayProperties;

    @Bean
    @ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
    @ConditionalOnMissingBean(SpringMVCClientRegistryManager.class)
    public SpringMVCClientRegistryManager springMVCClientRegisteryManager() throws Exception {
        return new SpringMVCClientRegistryManager(gatewayProperties);
    }

    @Bean
    @ConditionalOnClass({ServiceBean.class})
    @ConditionalOnMissingBean(Dubbo27ClientRegistryManager.class)
    public Dubbo27ClientRegistryManager dubbo27ClientRegisteryManager() throws Exception {
        return new Dubbo27ClientRegistryManager(gatewayProperties);
    }

}
