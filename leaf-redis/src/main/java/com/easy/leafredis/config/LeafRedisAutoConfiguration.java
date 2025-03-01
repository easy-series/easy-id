package com.easy.leafredis.config;

import com.easy.leafredis.segment.RedisSegmentIDGenImpl;
import com.sankuai.inf.leaf.IDGen;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@Configuration
@EnableConfigurationProperties(LeafRedisProperties.class)
public class LeafRedisAutoConfiguration {

    @Bean
    public IDGen idGen(RedisTemplate<String, Object> redisTemplate, LeafRedisProperties properties) {
        RedisSegmentIDGenImpl idGen = new RedisSegmentIDGenImpl(redisTemplate, properties);
        idGen.init();
        return idGen;
    }
} 