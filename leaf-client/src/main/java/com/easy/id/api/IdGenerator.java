package com.easy.id.api;

/**
 * 通用ID生成器接口
 */
public interface IdGenerator {
    
    /**
     * 获取ID
     * 
     * @param key 业务标识
     * @return 生成的ID
     */
    long generateId(String key);
    
    /**
     * 初始化
     * 
     * @return 是否初始化成功
     */
    boolean init();
} 