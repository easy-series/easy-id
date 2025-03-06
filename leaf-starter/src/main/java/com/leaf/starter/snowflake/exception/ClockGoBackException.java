package com.leaf.starter.snowflake.exception;

/**
 * 时钟回拨异常
 */
public class ClockGoBackException extends RuntimeException {
    public ClockGoBackException(String message) {
        super(message);
    }
}