package com.leaf.starter.segment.redis;

import com.leaf.starter.common.IDGen;
import com.leaf.starter.common.Result;
import com.leaf.starter.common.Status;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis号段模式ID生成器实现
 */
@Slf4j
public class RedisSegmentIDGenImpl implements IDGen {
    /**
     * Redis客户端
     */
    private RedisClient redisClient;
    
    /**
     * Redis连接
     */
    private StatefulRedisConnection<String, String> connection;
    
    /**
     * Redis命令
     */
    private RedisCommands<String, String> commands;
    
    /**
     * 配置
     */
    private RedisSegmentConfig config;
    
    /**
     * 缓存
     */
    private Map<String, Segment> cache = new ConcurrentHashMap<>();
    
    /**
     * 是否初始化完成
     */
    private volatile boolean initOK = false;

    public RedisSegmentIDGenImpl(String redisUri, RedisSegmentConfig config) {
        this.config = config;
        this.redisClient = RedisClient.create(redisUri);
    }

    @Override
    public boolean init() {
        try {
            // 创建Redis连接
            connection = redisClient.connect();
            commands = connection.sync();
            
            // 测试连接
            String pong = commands.ping();
            if (!"PONG".equalsIgnoreCase(pong)) {
                log.error("Redis连接测试失败");
                return false;
            }
            
            initOK = true;
            log.info("Redis号段模式ID生成器初始化成功");
            return true;
        } catch (Exception e) {
            log.error("Redis号段模式ID生成器初始化失败", e);
            return false;
        }
    }

    @Override
    public Result get(String key) {
        if (!initOK) {
            return new Result(-1, Status.EXCEPTION);
        }
        
        if (key == null || key.isEmpty()) {
            return new Result(-2, Status.EXCEPTION);
        }
        
        // 获取或创建号段
        Segment segment = getSegment(key);
        
        // 获取ID
        long id = segment.getId();
        if (id < 0) {
            return new Result(id, Status.EXCEPTION);
        }
        
        return new Result(id, Status.SUCCESS);
    }

    /**
     * 获取号段
     */
    private Segment getSegment(String key) {
        Segment segment = cache.get(key);
        if (segment == null) {
            synchronized (this) {
                segment = cache.get(key);
                if (segment == null) {
                    segment = new Segment(key);
                    cache.put(key, segment);
                }
            }
        }
        return segment;
    }

    /**
     * 号段类
     */
    private class Segment {
        /**
         * 业务标识
         */
        private String key;
        
        /**
         * 当前值
         */
        private AtomicLong value;
        
        /**
         * 最大值
         */
        private volatile long max;
        
        /**
         * 步长
         */
        private volatile int step;
        
        /**
         * 上次更新时间
         */
        private volatile long lastUpdateTime;
        
        /**
         * 是否初始化完成
         */
        private volatile boolean initOK;
        
        /**
         * 是否正在更新
         */
        private volatile boolean isUpdating;

        public Segment(String key) {
            this.key = key;
            this.value = new AtomicLong(0);
            this.max = 0;
            this.step = config.getStep();
            this.lastUpdateTime = 0;
            this.initOK = false;
            this.isUpdating = false;
        }

        /**
         * 获取ID
         */
        public synchronized long getId() {
            // 初始化
            if (!initOK) {
                updateSegmentFromRedis();
                initOK = true;
            }
            
            // 当前值
            long currentValue = value.get();
            
            // 如果当前值超过更新阈值，且没有其他线程正在更新，则异步更新号段
            if (currentValue >= max * config.getUpdatePercent() && !isUpdating) {
                isUpdating = true;
                Thread updateThread = new Thread(() -> {
                    try {
                        updateSegmentFromRedis();
                    } catch (Exception e) {
                        log.error("更新号段失败", e);
                    } finally {
                        isUpdating = false;
                    }
                });
                updateThread.setDaemon(true);
                updateThread.start();
            }
            
            // 获取ID
            long id = value.incrementAndGet();
            if (id <= max) {
                return id;
            }
            
            // 如果当前号段已用完，则同步更新号段
            for (int i = 0; i < 3; i++) {
                updateSegmentFromRedis();
                id = value.incrementAndGet();
                if (id <= max) {
                    return id;
                }
            }
            
            // 更新失败
            log.error("更新号段失败，key: {}", key);
            return -3;
        }

        /**
         * 从Redis更新号段
         */
        private void updateSegmentFromRedis() {
            try {
                // 计算步长
                int nextStep = calculateNextStep();
                
                // 生成Redis键
                String redisKey = config.getKeyPrefix() + key;
                
                // 使用Lua脚本原子性地获取并增加号段
                String script = "local current = redis.call('get', KEYS[1]) " +
                        "if not current then " +
                        "  redis.call('set', KEYS[1], ARGV[1]) " +
                        "  return ARGV[1] " +
                        "else " +
                        "  redis.call('incrby', KEYS[1], ARGV[2]) " +
                        "  return redis.call('get', KEYS[1]) " +
                        "end";
                
                // 执行Lua脚本
                String result = commands.eval(script, 
                        io.lettuce.core.ScriptOutputType.VALUE, 
                        new String[]{redisKey}, 
                        "1", String.valueOf(nextStep));
                
                // 解析结果
                long max = Long.parseLong(result);
                long min = max - nextStep;
                
                // 更新号段
                this.value = new AtomicLong(min);
                this.max = max;
                this.step = nextStep;
                this.lastUpdateTime = System.currentTimeMillis();
                
                log.info("更新号段成功，key: {}, min: {}, max: {}, step: {}", key, min, max, nextStep);
            } catch (Exception e) {
                log.error("更新号段失败，key: {}", key, e);
            }
        }

        /**
         * 计算下一个步长
         */
        private int calculateNextStep() {
            if (!config.isEnableAdaptiveStep() || lastUpdateTime == 0) {
                return config.getStep();
            }
            
            // 计算时间间隔
            long duration = System.currentTimeMillis() - lastUpdateTime;
            
            // 根据时间间隔调整步长
            int nextStep = step;
            if (duration < 15 * 60 * 1000) { // 小于15分钟
                nextStep = nextStep * 2;
                if (nextStep > config.getMaxStep()) {
                    nextStep = config.getMaxStep();
                }
            } else if (duration >= 30 * 60 * 1000) { // 大于30分钟
                nextStep = nextStep / 2;
                if (nextStep < config.getMinStep()) {
                    nextStep = config.getMinStep();
                }
            }
            
            return nextStep;
        }
    }
} 