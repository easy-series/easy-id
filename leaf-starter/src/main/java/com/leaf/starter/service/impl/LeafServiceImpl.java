package com.leaf.starter.service.impl;

import com.leaf.starter.common.IDGen;
import com.leaf.starter.common.Result;
import com.leaf.starter.common.Status;
import com.leaf.starter.exception.LeafException;
import com.leaf.starter.service.LeafService;
import lombok.extern.slf4j.Slf4j;

/**
 * Leaf ID生成服务实现类
 */
@Slf4j
public class LeafServiceImpl implements LeafService {
    /**
     * ID生成器
     */
    private final IDGen idGen;

    public LeafServiceImpl(IDGen idGen) {
        this.idGen = idGen;
    }

    @Override
    public Result getId(String key) {
        if (key == null || key.isEmpty()) {
            throw new LeafException("业务标识key不能为空");
        }
        
        Result result = idGen.get(key);
        if (result.getStatus() == Status.EXCEPTION) {
            log.error("获取ID失败，key={}, result={}", key, result);
            throw new LeafException("获取ID失败: " + result);
        }
        
        return result;
    }

    @Override
    public long getIdAsLong(String key) {
        return getId(key).getId();
    }

    @Override
    public String getIdAsString(String key) {
        return String.valueOf(getIdAsLong(key));
    }
} 