package com.sankuai.inf.leaf.segment.redis;

import com.sankuai.inf.leaf.IDGen;
import com.sankuai.inf.leaf.common.Result;
import org.junit.Before;
import org.junit.Test;

public class RedisSegmentIDGenImplTest {

    private IDGen idGen;

    @Before
    public void setUp() {
        // 配置Redis连接
//        String redisUri = "redis://localhost:6379";

        // 如果有密码，使用以下格式
        String redisUri = "redis://123456@localhost:6379";

        // 配置Redis号段生成器
        RedisSegmentConfig segmentConfig = new RedisSegmentConfig();
        segmentConfig.setStep(1000);
        segmentConfig.setUpdatePercent(0.9);
        segmentConfig.setEnableAdaptiveStep(true);
        segmentConfig.setMinStep(1000);
        segmentConfig.setMaxStep(100000);

        // 初始化ID生成器
        idGen = new RedisSegmentIDGenImpl(redisUri, segmentConfig);
        idGen.init();
    }

    @Test
    public void testGetId() {
        // 测试获取ID
//        for (int i = 0; i < 10; i++) {
//            Result result = idGen.get("test");
//            System.out.println(result);
//        }
        Result result = idGen.get("test");
        System.out.println(result);

    }

    @Test
    public void testMultipleKeys() {
        // 测试不同的业务标识
        Result orderResult = idGen.get("order");
        Result userResult = idGen.get("user");

        System.out.println("Order ID: " + orderResult.getId());
        System.out.println("User ID: " + userResult.getId());
    }
}