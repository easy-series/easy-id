package com.leaf.starter.segment.dao;

import com.leaf.starter.segment.model.LeafAlloc;

import java.util.List;

/**
 * ID分配DAO接口
 */
public interface IDAllocDao {
    /**
     * 根据业务标识获取号段
     *
     * @param tag 业务标识
     * @return 号段分配信息
     */
    LeafAlloc getLeafAlloc(String tag);
    
    /**
     * 更新最大ID
     *
     * @param tag 业务标识
     * @param step 步长
     * @param maxId 新的最大ID
     * @return 更新结果
     */
    int updateMaxId(String tag, int step, long maxId);
    
    /**
     * 更新最大ID（带有乐观锁）
     *
     * @param leafAlloc 号段分配信息
     * @return 更新结果
     */
    int updateMaxIdByCustomStepAndConcurrent(LeafAlloc leafAlloc);
    
    /**
     * 获取所有业务标识
     *
     * @return 所有业务标识列表
     */
    List<String> getAllTags();
    
    /**
     * 获取所有号段分配信息
     *
     * @return 所有号段分配信息列表
     */
    List<LeafAlloc> getAllLeafAllocs();
} 