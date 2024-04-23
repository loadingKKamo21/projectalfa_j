package com.project.alfa.error.exception;

import lombok.Getter;

/**
 * https://cheese10yun.github.io/spring-guide-exception/
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private ErrorCode errorCode;
    
    public BusinessException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
}
