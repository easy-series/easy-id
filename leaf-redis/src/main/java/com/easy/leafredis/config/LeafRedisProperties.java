package com.easy.leafredis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "leaf.redis.segment")
public class LeafRedisProperties {
    
    /**
     * 号段步长
     */
    private int step = 1000;
    
    /**
     * 更新下一个号段的触发百分比
     */
    private double updatePercent = 0.9;
} 