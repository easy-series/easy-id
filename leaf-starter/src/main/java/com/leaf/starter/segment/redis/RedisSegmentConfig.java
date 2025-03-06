package com.leaf.starter.segment.redis;

import lombok.Data;

/**
 * Redis号段模式配置
 */
@Data
public class RedisSegmentConfig {
    /**
     * 号段步长
     */
    private int step = 1000;
    
    /**
     * 更新阈值百分比
     */
    private double updatePercent = 0.9;
    
    /**
     * 键前缀
     */
    private String keyPrefix = "leaf:segment:";
    
    /**
     * 是否启用自适应步长
     */
    private boolean enableAdaptiveStep = true;
    
    /**
     * 最小步长
     */
    private int minStep = 1000;
    
    /**
     * 最大步长
     */
    private int maxStep = 100000;
} 