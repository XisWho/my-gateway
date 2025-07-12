package com.xw.constants;

public class Protocol {

    public static final String HTTP = "http";

    public static final String DUBBO = "dubbo";

    static boolean isHttp(String protocol) {
        return HTTP.equals(protocol);
    }

    static boolean isDubbo(String protocol) {
        return DUBBO.equals(protocol);
    }

}
