package com.easy.leafredis;

import com.sankuai.inf.leaf.IDGen;
import com.sankuai.inf.leaf.common.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LeafRedisApplicationTests {

    @Autowired
    private IDGen idGen;

    @Test
    void testGetId() {
        // 测试获取单个ID
        Result result = idGen.get("test");
        assertNotNull(result);
        assertEquals(0L, result.getId());
        
        // 测试连续获取多个ID
        for (int i = 1; i < 5; i++) {
            Result nextResult = idGen.get("test");
            assertEquals(i, nextResult.getId());
        }
    }

    @Test
    void testMultipleKeys() {
        // 测试不同的业务标识
        Result orderResult = idGen.get("order");
        Result userResult = idGen.get("user");
        
        assertEquals(0L, orderResult.getId());
        assertEquals(0L, userResult.getId());
    }
}
