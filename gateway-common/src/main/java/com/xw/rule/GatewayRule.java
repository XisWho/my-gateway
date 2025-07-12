package com.xw.rule;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 规则类
 */
@Getter
@Setter
public class GatewayRule implements Comparable<GatewayRule>, Serializable {

	//	规则ID 全局唯一
	private String id;
	
	//	规则名称
	private String name;
	
	//	规则对应的协议
	private String protocol;
	
	//	规则排序，用于以后万一有需求做一个路径绑定多种规则，但是只能最终执行一个规则（按照该属性做优先级判断）
	private Integer order;
	
	//	规则集合定义
	private Set<FilterConfig> filterConifgs = new HashSet<>();

	/**
	 * 向规则里面添加指定的过滤器
	 * @param filterConfig
	 * @return
	 */
	public boolean addFilterConfig(FilterConfig filterConfig) {
		return filterConifgs.add(filterConfig);
	}

	/**
	 * 通过一个指定的filterId获取getFilterConfig
	 * @param id
	 * @return
	 */
	public FilterConfig getFilterConfig(String id){
		for(FilterConfig filterConfig : filterConifgs) {
			if(filterConfig.getId().equalsIgnoreCase(id)) {
				return filterConfig;
			}
		}
		return null;
	}

	/**
	 * 根据传入的filterId判断当前Rule中是否存在
	 * @param id
	 * @return
	 */
	public boolean hashId(String id) {
		for(FilterConfig filterConfig : filterConifgs) {
			if(filterConfig.getId().equalsIgnoreCase(id)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int compareTo(GatewayRule o) {
		int orderCompare = Integer.compare(getOrder(), o.getOrder());
		if(orderCompare == 0) {
			return getId().compareTo(o.getId());
		}
		return orderCompare;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if((o == null) || getClass() != o.getClass()) return false;
		GatewayRule that = (GatewayRule)o;
		return id.equals(that.id);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	/**
	 * 过滤器的配置类
	 */
	@Getter
	@Setter
	public static class FilterConfig {
		
		//	过滤器的唯一ID
		private String id;
		
		//	过滤器的配置信息描述：json string 
		private String config;

		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if((o == null) || getClass() != o.getClass()) return false;
			FilterConfig that = (FilterConfig)o;
			return id.equals(that.id);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(id);
		}
	}
	
}
