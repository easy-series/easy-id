package com.easy.id.api;

import com.easy.id.config.RedisSegmentIdGeneratorProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class IdGeneratorTest {
    
    @Test
    public void testRedisSegmentIdGenerator() {
        // 配置Redis号段模式
        RedisSegmentIdGeneratorProperties properties = new RedisSegmentIdGeneratorProperties();
        properties.setHost("localhost");
        properties.setPort(6379);
        properties.setPassword("123456");
        
        // 创建ID生成器
        RedisSegmentIdGenerator idGenerator = new RedisSegmentIdGenerator(properties);
        assertTrue(idGenerator.init());
        
        // 生成ID
        long id = idGenerator.generateId("test");
        System.out.println("Generated ID: " + id);
        
        // 清理资源
        idGenerator.destroy();
    }
} 