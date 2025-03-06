package com.leaf.starter.snowflake;

import com.leaf.starter.common.IDGen;
import com.leaf.starter.common.Result;
import com.leaf.starter.common.Status;
import com.leaf.starter.common.Utils;
import com.leaf.starter.snowflake.exception.ClockGoBackException;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * 雪花算法ID生成器实现
 */
@Slf4j
public class SnowflakeIDGenImpl implements IDGen {
    /**
     * 起始时间戳，默认为2010-11-04 09:42:54
     */
    private final long twepoch;
    
    /**
     * 工作节点ID位数
     */
    private final long workerIdBits = 10L;
    
    /**
     * 最大工作节点ID
     */
    private final long maxWorkerId = ~(-1L << workerIdBits);
    
    /**
     * 序列号位数
     */
    private final long sequenceBits = 12L;
    
    /**
     * 工作节点ID左移位数
     */
    private final long workerIdShift = sequenceBits;
    
    /**
     * 时间戳左移位数
     */
    private final long timestampLeftShift = sequenceBits + workerIdBits;
    
    /**
     * 序列号掩码
     */
    private final long sequenceMask = ~(-1L << sequenceBits);
    
    /**
     * 工作节点ID
     */
    private long workerId;
    
    /**
     * 序列号
     */
    private long sequence = 0L;
    
    /**
     * 上次生成ID的时间戳
     */
    private long lastTimestamp = -1L;
    
    /**
     * 随机数生成器
     */
    private static final Random RANDOM = new Random();
    
    /**
     * ZooKeeper工作节点管理
     */
    private SnowflakeZookeeperHolder snowflakeZookeeperHolder;

    public SnowflakeIDGenImpl(String zkAddress, int port) {
        this(zkAddress, port, 1288834974657L);
    }

    public SnowflakeIDGenImpl(String zkAddress, int port, long twepoch) {
        this.twepoch = twepoch;
        if (System.currentTimeMillis() <= twepoch) {
            throw new IllegalArgumentException("当前时间小于起始时间");
        }
        
        final String ip = Utils.getIp();
        snowflakeZookeeperHolder = new SnowflakeZookeeperHolder(ip, String.valueOf(port), zkAddress);
        log.info("雪花算法参数: twepoch={}, ip={}, zkAddress={}, port={}", twepoch, ip, zkAddress, port);
    }

    @Override
    public boolean init() {
        boolean initFlag = snowflakeZookeeperHolder.init();
        if (initFlag) {
            workerId = snowflakeZookeeperHolder.getWorkerId();
            log.info("雪花算法ID生成器初始化成功, workerId: {}", workerId);
            return true;
        }
        
        log.error("雪花算法ID生成器初始化失败");
        return false;
    }

    @Override
    public Result get(String key) {
        long id;
        try {
            id = nextId();
        } catch (ClockGoBackException e) {
            log.error("生成雪花算法ID失败", e);
            return new Result(-1, Status.EXCEPTION);
        }
        return new Result(id, Status.SUCCESS);
    }

    /**
     * 生成下一个ID
     */
    private synchronized long nextId() {
        long timestamp = timeGen();
        
        // 时钟回拨检查
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                try {
                    // 等待时钟追上
                    Thread.sleep(offset << 1);
                    timestamp = timeGen();
                    if (timestamp < lastTimestamp) {
                        throw new ClockGoBackException("时钟回拨，无法生成ID");
                    }
                } catch (InterruptedException e) {
                    log.error("等待时钟追上时被中断", e);
                    throw new ClockGoBackException("时钟回拨，等待被中断");
                }
            } else {
                throw new ClockGoBackException("时钟回拨，无法生成ID");
            }
        }
        
        // 同一毫秒内序列号递增
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                // 同一毫秒内序列号用完，等待下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒内，序列号重置
            sequence = RANDOM.nextInt(10);
        }
        
        lastTimestamp = timestamp;
        
        // 组装ID
        return ((timestamp - twepoch) << timestampLeftShift) | (workerId << workerIdShift) | sequence;
    }

    /**
     * 等待下一毫秒
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }
} 