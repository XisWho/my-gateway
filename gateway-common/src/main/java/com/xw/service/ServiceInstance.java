package com.xw.service;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public class ServiceInstance implements Serializable {

    private static final long serialVersionUID = -7559569289189228478L;

    /**
     * 	服务实例ID: ip:port
     */
    private String serviceInstanceId;

    /**
     * 	服务定义唯一id： uniqueId
     */
    private String uniqueId;

    /**
     * 	服务实例地址： ip:port
     */
    private String address;

    /**
     * 	标签信息
     */
    private String tags;

    /**
     * 	权重信息
     */
    private Integer weight;

    /**
     * 	服务注册的时间戳：后面我们做负载均衡，warmup预热
     */
    private long registerTime;

    /**
     * 	服务实例启用禁用
     */
    private boolean enable = true;

    /**
     * 	服务实例对应的版本号
     */
    private String version;

    public ServiceInstance() {
        super();
    }

    public ServiceInstance(String serviceInstanceId, String uniqueId, String address, String tags, Integer weight,
                           long registerTime, boolean enable, String version) {
        super();
        this.serviceInstanceId = serviceInstanceId;
        this.uniqueId = uniqueId;
        this.address = address;
        this.tags = tags;
        this.weight = weight;
        this.registerTime = registerTime;
        this.enable = enable;
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(this == null || getClass() != o.getClass()) return false;
        ServiceInstance serviceInstance = (ServiceInstance)o;
        return Objects.equals(serviceInstanceId, serviceInstance.serviceInstanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceInstanceId);
    }

}
