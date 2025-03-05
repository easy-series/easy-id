package com.easy.id.exception;

/**
 * ID生成异常
 */
public class IdGenerationException extends RuntimeException {
    
    public IdGenerationException(String message) {
        super(message);
    }
    
    public IdGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
} 