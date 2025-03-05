package com.easy.id.api;

import com.easy.id.exception.IdGenerationException;
import com.sankuai.inf.leaf.IDGen;
import com.sankuai.inf.leaf.common.Result;
import com.sankuai.inf.leaf.common.Status;
import com.sankuai.inf.leaf.segment.SegmentIDGenImpl;
import com.sankuai.inf.leaf.segment.dao.IDAllocDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于数据库号段模式的ID生成器
 */
public class SegmentIdGenerator implements IdGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(SegmentIdGenerator.class);
    
    private final IDGen idGen;
    
    public SegmentIdGenerator(IDAllocDao idAllocDao) {
        SegmentIDGenImpl segmentIDGen = new SegmentIDGenImpl();
        segmentIDGen.setDao(idAllocDao);
        this.idGen = segmentIDGen;
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