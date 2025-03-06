package com.leaf.starter.service;

import com.leaf.starter.common.Result;

/**
 * Leaf ID生成服务接口
 */
public interface LeafService {
    /**
     * 获取ID
     *
     * @param key 业务标识
     * @return ID生成结果
     */
    Result getId(String key);
    
    /**
     * 获取ID（直接返回ID值）
     *
     * @param key 业务标识
     * @return ID值
     */
    long getIdAsLong(String key);
    
    /**
     * 获取ID（直接返回ID值的字符串形式）
     *
     * @param key 业务标识
     * @return ID值的字符串形式
     */
    String getIdAsString(String key);
} 