package com.xw.gateway.annotation;

public enum Protocol {

	HTTP("http", "http协议"),
	DUBBO("dubbo", "http协议");
	
	private String code;
	
	private String desc;
	
	Protocol(String code, String desc){
		this.code = code;
		this.desc = desc;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getDesc() {
		return desc;
	}
	
}
