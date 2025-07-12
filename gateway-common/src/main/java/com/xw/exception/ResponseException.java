package com.xw.exception;

import com.xw.constants.ResponseCode;

public class ResponseException extends BaseException {

    public ResponseException() {
        this(ResponseCode.INTERNAL_ERROR);
    }

    public ResponseException(ResponseCode code) {
        super(code.getMessage(), code);
    }

    public ResponseException(Throwable cause, ResponseCode code) {
        super(code.getMessage(), cause, code);
        this.code = code;
    }

}
