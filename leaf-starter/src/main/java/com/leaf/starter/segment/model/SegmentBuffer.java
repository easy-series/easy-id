package com.leaf.starter.segment.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 号段缓冲区（双buffer）
 */
@Getter
@Setter
public class SegmentBuffer {
    /**
     * 业务标识
     */
    private String key;
    
    /**
     * 双buffer
     */
    private Segment[] segments;
    
    /**
     * 当前使用的segment索引
     */
    private volatile int currentPos;
    
    /**
     * 下一个segment是否准备好
     */
    private volatile boolean nextReady;
    
    /**
     * 是否初始化完成
     */
    private volatile boolean initOk;
    
    /**
     * 是否有线程在运行中
     */
    private final AtomicBoolean threadRunning;
    
    /**
     * 读写锁
     */
    private final ReadWriteLock lock;
    
    /**
     * 步长
     */
    private volatile int step;
    
    /**
     * 最小步长
     */
    private volatile int minStep;
    
    /**
     * 更新时间戳
     */
    private volatile long updateTimestamp;
    
    public SegmentBuffer() {
        segments = new Segment[]{new Segment(this), new Segment(this)};
        currentPos = 0;
        nextReady = false;
        initOk = false;
        threadRunning = new AtomicBoolean(false);
        lock = new ReentrantReadWriteLock();
    }
    
    /**
     * 获取当前segment
     *
     * @return 当前segment
     */
    public Segment getCurrent() {
        return segments[currentPos];
    }
    
    /**
     * 获取下一个segment
     *
     * @return 下一个segment
     */
    public Segment getNext() {
        return segments[1 - currentPos];
    }
    
    /**
     * 切换到下一个segment
     */
    public void switchPos() {
        currentPos = 1 - currentPos;
    }
    
    /**
     * 获取读锁
     *
     * @return 读锁
     */
    public java.util.concurrent.locks.Lock rLock() {
        return lock.readLock();
    }
    
    /**
     * 获取写锁
     *
     * @return 写锁
     */
    public java.util.concurrent.locks.Lock wLock() {
        return lock.writeLock();
    }
    
    @Override
    public String toString() {
        return "SegmentBuffer{" +
                "key='" + key + '\'' +
                ", segments=" + Arrays.toString(segments) +
                ", currentPos=" + currentPos +
                ", nextReady=" + nextReady +
                ", initOk=" + initOk +
                ", step=" + step +
                ", minStep=" + minStep +
                ", updateTimestamp=" + updateTimestamp +
                '}';
    }
} 