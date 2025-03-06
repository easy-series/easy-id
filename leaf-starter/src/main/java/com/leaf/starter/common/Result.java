package com.leaf.starter.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ID生成结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    /**
     * 生成的ID
     */
    private long id;
    
    /**
     * 结果状态
     */
    private Status status;
} 