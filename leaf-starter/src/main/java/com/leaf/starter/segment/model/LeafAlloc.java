package com.leaf.starter.segment.model;

import lombok.Data;

import java.util.Date;

/**
 * 号段分配模型
 */
@Data
public class LeafAlloc {
    /**
     * 业务标识
     */
    private String bizTag;
    
    /**
     * 当前最大ID
     */
    private long maxId;
    
    /**
     * 步长
     */
    private int step;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 