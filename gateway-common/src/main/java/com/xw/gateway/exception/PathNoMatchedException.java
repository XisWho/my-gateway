package com.xw.gateway.exception;


import com.xw.gateway.constants.ResponseCode;

/**
 * 请求路径不匹配的异常
 */
public class PathNoMatchedException extends BaseException {

	public PathNoMatchedException() {
		this(ResponseCode.PATH_NO_MATCHED);
	}

	public PathNoMatchedException(ResponseCode code) {
		super(code.getMessage(), code);
	}

	public PathNoMatchedException(Throwable cause, ResponseCode code) {
		super(code.getMessage(), cause, code);
	}

}
