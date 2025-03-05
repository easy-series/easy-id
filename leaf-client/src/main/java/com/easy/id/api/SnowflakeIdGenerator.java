package com.easy.id.api;

import com.easy.id.config.SnowflakeIdGeneratorProperties;
import com.easy.id.exception.IdGenerationException;
import com.sankuai.inf.leaf.IDGen;
import com.sankuai.inf.leaf.common.Result;
import com.sankuai.inf.leaf.common.Status;
import com.sankuai.inf.leaf.snowflake.SnowflakeIDGenImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于雪花算法的ID生成器
 */
public class SnowflakeIdGenerator implements IdGenerator {

    private static final Logger logger = LoggerFactory.getLogger(SnowflakeIdGenerator.class);

    private final IDGen idGen;

    public SnowflakeIdGenerator(SnowflakeIdGeneratorProperties properties) {
        this.idGen = new SnowflakeIDGenImpl(
                properties.getZkAddress(),
                properties.getPort(),
                0L);
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
} 