package com.easy.leafredis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class RedisConnectionTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testRedisConnection() {
        Boolean result = redisTemplate.getConnectionFactory().getConnection().ping() != null;
        assertTrue(result, "Redis连接失败");
    }
} 