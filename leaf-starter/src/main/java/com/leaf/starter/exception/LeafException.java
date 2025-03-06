package com.leaf.starter.exception;

/**
 * Leaf ID生成器异常
 */
public class LeafException extends RuntimeException {
    public LeafException(String message) {
        super(message);
    }

    public LeafException(String message, Throwable cause) {
        super(message, cause);
    }
} 