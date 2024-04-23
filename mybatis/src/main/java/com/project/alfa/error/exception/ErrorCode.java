package com.project.alfa.error.exception;

import lombok.Getter;

/**
 * https://cheese10yun.github.io/spring-guide-exception/
 */
@Getter
public enum ErrorCode {
    
    //Common
    INVALID_INPUT_VALUE(400, "C001", "Invalid input value"),
    METHOD_NOT_ALLOWED(405, "C002", "Invalid input value"),
    ENTITY_NOT_FOUND(400, "C003", "Entity not found"),
    INTERNAL_SERVER_ERROR(500, "C004", "Server error"),
    INVALID_TYPE_VALUE(400, "C005", "Invalid type value"),
    HANDLE_ACCESS_DENIED(403, "C006", "Access denied"),
    
    //Member
    USERNAME_DUPLICATION(400, "M001", "Username duplicated"),
    NICKNAME_DUPLICATION(400, "M002", "Nickname duplicated"),
    AUTH_NOT_COMPLETED(400, "M003", "Authentication not completed"),
    PASSWORD_DO_NOT_MATCH(400, "M005", "Password do not match"),
    UNAUTHORIZED(400, "M006", "No permission"),
    
    //Post
    NOT_WRITER_OF_POST(400, "P001", "Not writer of post"),
    
    //Comment
    NOT_WRITER_OF_COMMENT(400, "R001", "Not writer of comment"),
    NOT_COMMENT_ON_POST(400, "R002", "Not the comment on this post");
    
    private final int    status;
    private final String code;
    private final String message;
    
    ErrorCode(final int status, final String code, final String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
    
}
