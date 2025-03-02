package com.sankuai.inf.leaf.server.service;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sankuai.inf.leaf.IDGen;
import com.sankuai.inf.leaf.common.PropertyFactory;
import com.sankuai.inf.leaf.common.Result;
import com.sankuai.inf.leaf.common.ZeroIDGen;
import com.sankuai.inf.leaf.segment.redis.RedisSegmentConfig;
import com.sankuai.inf.leaf.segment.redis.RedisSegmentIDGenImpl;
import com.sankuai.inf.leaf.server.exception.InitException;

@Service("SegmentRedisService")
public class SegmentRedisService {
    private static final Logger logger = LoggerFactory.getLogger(SegmentRedisService.class);

    private IDGen idGen;
    private RedisSegmentIDGenImpl redisSegmentIDGen;

    public SegmentRedisService() throws InitException {
        Properties properties = PropertyFactory.getProperties();
        boolean flag = Boolean.parseBoolean(properties.getProperty("leaf.segment.redis.enable", "false"));

        if (flag) {
            try {
                // 配置Redis连接
                String host = properties.getProperty("leaf.redis.host", "localhost");
                String port = properties.getProperty("leaf.redis.port", "6379");
                String password = properties.getProperty("leaf.redis.password", "");

                // 构建Redis URI
                String redisUri = password.isEmpty() ? String.format("redis://%s:%s", host, port)
                        : String.format("redis://%s@%s:%s", password, host, port);

                // 配置Redis号段生成器
                RedisSegmentConfig segmentConfig = new RedisSegmentConfig();
                segmentConfig.setStep(Integer.parseInt(properties.getProperty("leaf.segment.redis.step", "1000")));
                segmentConfig.setUpdatePercent(
                        Double.parseDouble(properties.getProperty("leaf.segment.redis.update-percent", "0.9")));
                segmentConfig.setKeyPrefix(properties.getProperty("leaf.segment.redis.key-prefix", "leaf:segment:"));

                // 启用自适应步长
                boolean enableAdaptiveStep = Boolean
                        .parseBoolean(properties.getProperty("leaf.segment.redis.enable-adaptive-step", "true"));
                segmentConfig.setEnableAdaptiveStep(enableAdaptiveStep);

                if (enableAdaptiveStep) {
                    segmentConfig.setMinStep(
                            Integer.parseInt(properties.getProperty("leaf.segment.redis.min-step", "1000")));
                    segmentConfig.setMaxStep(
                            Integer.parseInt(properties.getProperty("leaf.segment.redis.max-step", "100000")));
                }

                // 初始化ID生成器
                redisSegmentIDGen = new RedisSegmentIDGenImpl(redisUri, segmentConfig);
                idGen = redisSegmentIDGen;

                if (idGen.init()) {
                    logger.info("Redis Segment Service Init Successfully");
                } else {
                    throw new InitException("Redis Segment Service Init Fail");
                }
            } catch (Exception e) {
                logger.error("Redis Segment Service Init Error", e);
                throw new InitException("Redis Segment Service Init Error: " + e.getMessage());
            }
        } else {
            idGen = new ZeroIDGen();
            logger.info("Zero ID Gen Service Init Successfully");
        }
    }

    public Result getId(String key) {
        return idGen.get(key);
    }

    public RedisSegmentIDGenImpl getIdGen() {
        return redisSegmentIDGen;
    }

    // 获取服务监控指标
    public Object getMetrics() {
        if (redisSegmentIDGen != null) {
            return redisSegmentIDGen.getMetrics();
        }
        return null;
    }

    // 关闭连接
    public void destroy() {
        if (redisSegmentIDGen != null) {
            redisSegmentIDGen.destroy();
        }
    }
}
