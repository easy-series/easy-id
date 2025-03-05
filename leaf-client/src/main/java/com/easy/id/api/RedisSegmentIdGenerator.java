package com.easy.id.api;

import com.easy.id.config.RedisSegmentIdGeneratorProperties;
import com.easy.id.exception.IdGenerationException;
import com.sankuai.inf.leaf.IDGen;
import com.sankuai.inf.leaf.common.Result;
import com.sankuai.inf.leaf.common.Status;
import com.sankuai.inf.leaf.segment.redis.RedisSegmentConfig;
import com.sankuai.inf.leaf.segment.redis.RedisSegmentIDGenImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于Redis号段模式的ID生成器
 */
public class RedisSegmentIdGenerator implements IdGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisSegmentIdGenerator.class);
    
    private final IDGen idGen;
    
    public RedisSegmentIdGenerator(RedisSegmentIdGeneratorProperties properties) {
        // 构建Redis URI
        String redisUri = properties.getPassword().isEmpty() ? 
                String.format("redis://%s:%s", properties.getHost(), properties.getPort()) : 
                String.format("redis://%s@%s:%s", properties.getPassword(), properties.getHost(), properties.getPort());
        
        // 配置Redis号段生成器
        RedisSegmentConfig config = new RedisSegmentConfig();
        config.setStep(properties.getStep());
        config.setUpdatePercent(properties.getUpdatePercent());
        config.setKeyPrefix(properties.getKeyPrefix());
        config.setEnableAdaptiveStep(properties.isEnableAdaptiveStep());
        config.setMinStep(properties.getMinStep());
        config.setMaxStep(properties.getMaxStep());
        
        this.idGen = new RedisSegmentIDGenImpl(redisUri, config);
    }
    
    @Override
    public long generateId(String key) {
        Result result = idGen.get(key);
        if (result.getStatus() == Status.SUCCESS) {
            return result.getId();
        }
        logger.error("Generate ID failed for key: {}, result: {}", key, result);
        throw new IdGenerationException("Failed to generate ID for key: " + key);
    }
    
    @Override
    public boolean init() {
        return idGen.init();
    }
    
    /**
     * 关闭资源
     */
    public void destroy() {
        if (idGen instanceof RedisSegmentIDGenImpl) {
            ((RedisSegmentIDGenImpl) idGen).destroy();
        }
    }
} 