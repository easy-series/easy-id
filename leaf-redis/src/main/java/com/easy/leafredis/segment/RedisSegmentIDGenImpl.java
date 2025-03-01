package com.easy.leafredis.segment;

import com.sankuai.inf.leaf.IDGen;
import com.sankuai.inf.leaf.common.Result;
import com.sankuai.inf.leaf.common.Status;
import com.sankuai.inf.leaf.segment.model.Segment;
import com.sankuai.inf.leaf.segment.model.SegmentBuffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import com.easy.leafredis.config.LeafRedisProperties;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RedisSegmentIDGenImpl implements IDGen {
    
    /**
     * IDCache未初始化成功时的异常码
     */
    private static final long EXCEPTION_ID_CACHE_INIT_FALSE = -1;
    /**
     * key不存在时的异常码
     */
    private static final long EXCEPTION_ID_KEY_NOT_EXISTS = -2;
    /**
     * SegmentBuffer中的两个Segment均未从Redis中装载时的异常码
     */
    private static final long EXCEPTION_ID_TWO_SEGMENTS_ARE_NULL = -3;

    /**
     * 最大步长不超过100,0000
     */
    private static final int MAX_STEP = 1000000;
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final LeafRedisProperties properties;
    private final Map<String, SegmentBuffer> cache = new ConcurrentHashMap<>();
    private final ExecutorService service = new ThreadPoolExecutor(5, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, 
            new SynchronousQueue<>(), new ThreadFactory() {
        private int threadInitNumber = 0;
        
        private synchronized int nextThreadNum() {
            return threadInitNumber++;
        }
        
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Thread-Segment-Update-" + nextThreadNum());
        }
    });

    public RedisSegmentIDGenImpl(RedisTemplate<String, Object> redisTemplate, LeafRedisProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public boolean init() {
        log.info("Init ...");
        // 确保加载到Redis后才初始化成功
        updateCacheFromRedis();
        return true;
    }

    @Override
    public Result get(String key) {
        if (!cache.containsKey(key)) {
            synchronized (this) {
                if (!cache.containsKey(key)) {
                    SegmentBuffer buffer = new SegmentBuffer();
                    buffer.setKey(key);
                    Segment segment = buffer.getCurrent();
                    segment.setValue(new AtomicLong(0));
                    segment.setMax(0);
                    segment.setStep(properties.getStep());
                    cache.put(key, buffer);
                }
            }
        }
        
        SegmentBuffer buffer = cache.get(key);
        if (!buffer.isInitOk()) {
            synchronized (buffer) {
                if (!buffer.isInitOk()) {
                    try {
                        updateSegmentFromRedis(key, buffer.getCurrent());
                        log.info("Init buffer. Update key {} {} from Redis", key, buffer.getCurrent());
                        buffer.setInitOk(true);
                    } catch (Exception e) {
                        log.warn("Init buffer {} exception", buffer.getCurrent(), e);
                    }
                }
            }
        }
        return getIdFromSegmentBuffer(buffer);
    }

    private void updateCacheFromRedis() {
        // TODO: 实现从Redis加载所有业务标识
    }

    private void updateSegmentFromRedis(String key, Segment segment) {
        String luaScript = 
                "local step = tonumber(ARGV[1]) " +
                "local result = redis.call('INCRBY', KEYS[1], step) " +
                "return result";
        
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);
        
        Long maxId = redisTemplate.execute(redisScript, Collections.singletonList(key), segment.getStep());
        
        // 设置号段的最大值和当前值
        segment.setMax(maxId);
        segment.getValue().set(maxId - segment.getStep());
    }

    private Result getIdFromSegmentBuffer(SegmentBuffer buffer) {
        while (true) {
            buffer.rLock().lock();
            try {
                final Segment segment = buffer.getCurrent();
                if (!buffer.isNextReady() && 
                    (segment.getIdle() < properties.getUpdatePercent() * segment.getStep()) && 
                    buffer.getThreadRunning().compareAndSet(false, true)) {
                    service.execute(() -> {
                        Segment next = buffer.getSegments()[buffer.nextPos()];
                        boolean updateOk = false;
                        try {
                            updateSegmentFromRedis(buffer.getKey(), next);
                            updateOk = true;
                            log.info("update segment {} from redis {}", buffer.getKey(), next);
                        } catch (Exception e) {
                            log.warn(buffer.getKey() + " updateSegmentFromRedis exception", e);
                        } finally {
                            if (updateOk) {
                                buffer.wLock().lock();
                                buffer.setNextReady(true);
                                buffer.getThreadRunning().set(false);
                                buffer.wLock().unlock();
                            } else {
                                buffer.getThreadRunning().set(false);
                            }
                        }
                    });
                }
                long value = segment.getValue().getAndIncrement();
                if (value < segment.getMax()) {
                    return new Result(value, Status.SUCCESS);
                }
            } finally {
                buffer.rLock().unlock();
            }
            waitAndSleep(buffer);
            buffer.wLock().lock();
            try {
                final Segment segment = buffer.getCurrent();
                long value = segment.getValue().getAndIncrement();
                if (value < segment.getMax()) {
                    return new Result(value, Status.SUCCESS);
                }
                if (buffer.isNextReady()) {
                    buffer.switchPos();
                    buffer.setNextReady(false);
                } else {
                    log.error("Both two segments in {} are not ready!", buffer);
                    return new Result(EXCEPTION_ID_TWO_SEGMENTS_ARE_NULL, Status.EXCEPTION);
                }
            } finally {
                buffer.wLock().unlock();
            }
        }
    }

    private void waitAndSleep(SegmentBuffer buffer) {
        int roll = 0;
        while (buffer.getThreadRunning().get()) {
            roll += 1;
            if(roll > 10000) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                    break;
                } catch (InterruptedException e) {
                    log.warn("Thread {} Interrupted", Thread.currentThread().getName());
                    break;
                }
            }
        }
    }
} 