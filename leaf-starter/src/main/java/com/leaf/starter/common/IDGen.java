package com.leaf.starter.common;

/**
 * ID生成器接口
 */
public interface IDGen {
    /**
     * 获取ID
     *
     * @param key 业务标识
     * @return ID生成结果
     */
    Result get(String key);
    
    /**
     * 初始化
     *
     * @return 是否初始化成功
     */
    boolean init();
} 