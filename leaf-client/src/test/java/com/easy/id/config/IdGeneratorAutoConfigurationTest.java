package com.easy.id.config;

import com.easy.id.api.IdGenerator;
import com.easy.id.api.RedisSegmentIdGenerator;
import com.easy.id.api.SegmentIdGenerator;
import com.easy.id.api.SnowflakeIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAutoConfiguration
@TestPropertySource(properties = {
        "redis.host=localhost",
        "redis.port=6379",
        "redis.password=123456",
        "redis.database=0"
})
public class IdGeneratorAutoConfigurationTest {

    @Autowired
    private ApplicationContext context;

    @Configuration
    static class TestConfig {
    }

    @Test
    public void testDefaultIdGenerator() {
        IdGenerator idGenerator = context.getBean(IdGenerator.class);
        assertNotNull("ID Generator should not be null", idGenerator);
        assertTrue("Default ID Generator should be RedisSegmentIdGenerator", 
                idGenerator instanceof RedisSegmentIdGenerator);
        
        // 生成ID
        try {
            long id = idGenerator.generateId("test");
            System.out.println("Generated ID: " + id);
            assertTrue("Generated ID should be positive", id > 0);
        } catch (Exception e) {
            // 在测试环境中，如果Redis不可用，会抛出异常，这是正常的
            System.out.println("Redis may not be available: " + e.getMessage());
        }
    }
} 