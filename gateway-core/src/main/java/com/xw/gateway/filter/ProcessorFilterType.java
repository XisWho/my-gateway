package com.xw.gateway.filter;

public enum ProcessorFilterType {

    PRE("PRE", "前置过滤器"),

    ROUTE("ROUTE", "中置过滤器"),

    ERROR("ERROR", "前置过滤器"),

    POST("POST", "前置过滤器");

    private final String code ;

    private final String message;

    ProcessorFilterType(String code, String message){
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
