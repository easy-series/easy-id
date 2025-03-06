package com.leaf.starter.segment.model;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 号段模型
 */
@Getter
@Setter
public class Segment {
    /**
     * 当前值
     */
    private AtomicLong value = new AtomicLong(0);
    
    /**
     * 最大值
     */
    private volatile long max;
    
    /**
     * 步长
     */
    private volatile int step;
    
    /**
     * 所属缓冲区
     */
    private SegmentBuffer buffer;
    
    public Segment(SegmentBuffer buffer) {
        this.buffer = buffer;
    }
    
    /**
     * 获取剩余可用ID数量
     *
     * @return 剩余可用ID数量
     */
    public long getIdle() {
        return this.max - value.get();
    }
    
    @Override
    public String toString() {
        return "Segment(value=" + value + ", max=" + max + ", step=" + step + ")";
    }
} 