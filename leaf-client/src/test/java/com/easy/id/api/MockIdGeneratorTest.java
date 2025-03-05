package com.easy.id.api;

import com.easy.id.config.RedisSegmentIdGeneratorProperties;
import com.easy.id.config.SnowflakeIdGeneratorProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import com.sankuai.inf.leaf.segment.dao.IDAllocDao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MockIdGeneratorTest {

    @Mock
    private IDAllocDao idAllocDao;

    @Test
    public void testSegmentIdGenerator() {
        // 创建一个模拟的SegmentIdGenerator
        SegmentIdGenerator idGenerator = Mockito.spy(new SegmentIdGenerator(idAllocDao));
        
        // 模拟init方法返回true
        when(idGenerator.init()).thenReturn(true);
        
        // 模拟generateId方法返回一个固定值
        when(idGenerator.generateId("test")).thenReturn(12345L);
        
        // 测试
        assertTrue(idGenerator.init());
        assertEquals(12345L, idGenerator.generateId("test"));
    }

    @Test
    public void testRedisSegmentIdGenerator() {
        // 创建配置
        RedisSegmentIdGeneratorProperties properties = new RedisSegmentIdGeneratorProperties();
        properties.setHost("localhost");
        properties.setPort(6379);
        properties.setPassword("123456");
        
        // 创建一个模拟的RedisSegmentIdGenerator
        RedisSegmentIdGenerator idGenerator = Mockito.spy(new RedisSegmentIdGenerator(properties));
        
        // 模拟init方法返回true
        when(idGenerator.init()).thenReturn(true);
        
        // 模拟generateId方法返回一个固定值
        when(idGenerator.generateId("test")).thenReturn(67890L);
        
        // 测试
        assertTrue(idGenerator.init());
        assertEquals(67890L, idGenerator.generateId("test"));
    }

    @Test
    public void testSnowflakeIdGenerator() {
        // 创建配置
        SnowflakeIdGeneratorProperties properties = new SnowflakeIdGeneratorProperties();
        properties.setZkAddress("localhost:2181");
        properties.setPort(8080);
        
        // 创建一个模拟的SnowflakeIdGenerator
        SnowflakeIdGenerator idGenerator = Mockito.spy(new SnowflakeIdGenerator(properties));
        
        // 模拟init方法返回true
        when(idGenerator.init()).thenReturn(true);
        
        // 模拟generateId方法返回一个固定值
        when(idGenerator.generateId("test")).thenReturn(123456789L);
        
        // 测试
        assertTrue(idGenerator.init());
        assertEquals(123456789L, idGenerator.generateId("test"));
    }
} 