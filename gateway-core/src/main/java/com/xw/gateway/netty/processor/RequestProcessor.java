package com.xw.gateway.netty.processor;

import com.xw.gateway.netty.context.HttpRequestWrapper;

public interface RequestProcessor {

    void process(HttpRequestWrapper request);

}
