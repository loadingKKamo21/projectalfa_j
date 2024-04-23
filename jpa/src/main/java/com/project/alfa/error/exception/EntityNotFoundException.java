package com.project.alfa.error.exception;

/**
 * https://cheese10yun.github.io/spring-guide-exception/
 */
public class EntityNotFoundException extends BusinessException {
    
    public EntityNotFoundException(String message) {
        super(message, ErrorCode.ENTITY_NOT_FOUND);
    }
    
}
