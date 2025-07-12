package com.xw.exception;

import com.xw.constants.ResponseCode;

/**
 * 服务信息未找到异常定义：比如服务定义、实例等信息未找到均会抛出此异常
 */
public class NotFoundException extends BaseException {

	public NotFoundException(ResponseCode code) {
		super(code.getMessage(), code);
	}

	public NotFoundException(Throwable cause, ResponseCode code) {
		super(code.getMessage(), cause, code);
	}
	
}
