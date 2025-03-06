package com.leaf.starter.segment;

import com.leaf.starter.common.IDGen;
import com.leaf.starter.common.Result;
import com.leaf.starter.common.Status;
import com.leaf.starter.segment.dao.IDAllocDao;
import com.leaf.starter.segment.model.LeafAlloc;
import com.leaf.starter.segment.model.Segment;
import com.leaf.starter.segment.model.SegmentBuffer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 号段模式ID生成器实现
 */
@Slf4j
public class SegmentIDGenImpl implements IDGen {
    /**
     * IDCache未初始化成功时的异常码
     */
    private static final long EXCEPTION_ID_IDCACHE_INIT_FALSE = -1;
    
    /**
     * key不存在时的异常码
     */
    private static final long EXCEPTION_ID_KEY_NOT_EXISTS = -2;
    
    /**
     * SegmentBuffer中的两个Segment均未从DB中装载时的异常码
     */
    private static final long EXCEPTION_ID_TWO_SEGMENTS_ARE_NULL = -3;
    
    /**
     * 最大步长不超过100,0000
     */
    private static final int MAX_STEP = 1000000;
    
    /**
     * 一个Segment维持时间为15分钟
     */
    private static final long SEGMENT_DURATION = 15 * 60 * 1000L;
    
    /**
     * 线程池
     */
    private ExecutorService service = new ThreadPoolExecutor(
            5, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new UpdateThreadFactory());
    
    /**
     * 是否初始化完成
     */
    private volatile boolean initOK = false;
    
    /**
     * 缓存
     */
    private Map<String, SegmentBuffer> cache = new ConcurrentHashMap<>();
    
    /**
     * DAO
     */
    private IDAllocDao dao;

    public void setDao(IDAllocDao dao) {
        this.dao = dao;
    }

    public IDAllocDao getDao() {
        return dao;
    }

    @Override
    public Result get(String key) {
        if (!initOK) {
            return new Result(EXCEPTION_ID_IDCACHE_INIT_FALSE, Status.EXCEPTION);
        }
        
        if (cache.containsKey(key)) {
            SegmentBuffer buffer = cache.get(key);
            if (!buffer.isInitOk()) {
                synchronized (buffer) {
                    if (!buffer.isInitOk()) {
                        try {
                            updateSegmentFromDb(key, buffer.getCurrent());
                            buffer.setInitOk(true);
                        } catch (Exception e) {
                            log.warn("Init buffer {} failed", buffer.getKey(), e);
                        }
                    }
                }
            }
            return getIdFromSegmentBuffer(cache.get(key));
        }
        
        synchronized (this) {
            if (cache.containsKey(key)) {
                return getIdFromSegmentBuffer(cache.get(key));
            }
            
            SegmentBuffer buffer = new SegmentBuffer();
            buffer.setKey(key);
            Segment segment = buffer.getCurrent();
            segment.setValue(new AtomicLong(0));
            segment.setMax(0);
            segment.setStep(0);
            cache.put(key, buffer);
            return getIdFromSegmentBuffer(buffer);
        }
    }

    @Override
    public boolean init() {
        log.info("初始化号段模式ID生成器");
        
        // 确保DAO已设置
        if (dao == null) {
            log.error("未设置DAO");
            return false;
        }
        
        try {
            // 加载所有业务标识
            List<String> tags = dao.getAllTags();
            if (tags == null || tags.isEmpty()) {
                log.warn("数据库中没有业务标识");
                return false;
            }
            
            // 初始化缓存
            for (String tag : tags) {
                SegmentBuffer buffer = new SegmentBuffer();
                buffer.setKey(tag);
                Segment segment = buffer.getCurrent();
                segment.setValue(new AtomicLong(0));
                segment.setMax(0);
                segment.setStep(0);
                cache.put(tag, buffer);
                log.info("初始化业务标识: {}", tag);
            }
            
            initOK = true;
            log.info("号段模式ID生成器初始化成功");
            return true;
        } catch (Exception e) {
            log.error("初始化号段模式ID生成器失败", e);
            return false;
        }
    }

    /**
     * 从SegmentBuffer获取ID
     */
    private Result getIdFromSegmentBuffer(SegmentBuffer buffer) {
        while (true) {
            try {
                buffer.rLock().lock();
                
                final Segment segment = buffer.getCurrent();
                if (!buffer.isNextReady() && (segment.getIdle() < 0.9 * segment.getStep()) && buffer.getThreadRunning().compareAndSet(false, true)) {
                    service.execute(() -> {
                        Segment next = buffer.getNext();
                        boolean updateOk = false;
                        try {
                            updateSegmentFromDb(buffer.getKey(), next);
                            updateOk = true;
                            log.info("更新下一个号段成功: {}", buffer.getKey());
                        } catch (Exception e) {
                            log.warn("更新下一个号段失败: {}", buffer.getKey(), e);
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
            
            try {
                buffer.wLock().lock();
                final Segment segment = buffer.getCurrent();
                long value = segment.getValue().getAndIncrement();
                if (value < segment.getMax()) {
                    return new Result(value, Status.SUCCESS);
                }
                
                if (buffer.isNextReady()) {
                    buffer.switchPos();
                    buffer.setNextReady(false);
                } else {
                    log.error("两个号段均已用完，key: {}", buffer.getKey());
                    return new Result(EXCEPTION_ID_TWO_SEGMENTS_ARE_NULL, Status.EXCEPTION);
                }
            } finally {
                buffer.wLock().unlock();
            }
        }
    }

    /**
     * 从数据库更新号段
     */
    private void updateSegmentFromDb(String key, Segment segment) {
        SegmentBuffer buffer = segment.getBuffer();
        LeafAlloc leafAlloc;
        
        if (!buffer.isInitOk()) {
            leafAlloc = dao.getLeafAlloc(key);
            if (leafAlloc == null) {
                throw new RuntimeException("找不到业务标识: " + key);
            }
            
            int step = leafAlloc.getStep();
            segment.setStep(step);
            segment.setMax(leafAlloc.getMaxId());
            segment.setValue(new AtomicLong(leafAlloc.getMaxId() - step));
            
            // 更新buffer
            buffer.setStep(step);
            buffer.setMinStep(step);
            buffer.setUpdateTimestamp(System.currentTimeMillis());
        } else if (buffer.getUpdateTimestamp() == 0) {
            leafAlloc = dao.getLeafAlloc(key);
            if (leafAlloc == null) {
                throw new RuntimeException("找不到业务标识: " + key);
            }
            
            int step = leafAlloc.getStep();
            segment.setStep(step);
            segment.setMax(leafAlloc.getMaxId());
            segment.setValue(new AtomicLong(leafAlloc.getMaxId() - step));
            
            // 更新buffer
            buffer.setStep(step);
            buffer.setMinStep(step);
            buffer.setUpdateTimestamp(System.currentTimeMillis());
        } else {
            // 动态调整步长
            int nextStep = buffer.getStep();
            long duration = System.currentTimeMillis() - buffer.getUpdateTimestamp();
            if (duration < SEGMENT_DURATION) {
                if (nextStep * 2 <= MAX_STEP) {
                    nextStep = nextStep * 2;
                }
            } else if (duration >= SEGMENT_DURATION * 2) {
                nextStep = nextStep / 2 >= buffer.getMinStep() ? nextStep / 2 : buffer.getMinStep();
            }
            
            log.info("调整步长: {}, key: {}, step: {}, duration: {}", buffer.getKey(), nextStep, duration);
            
            // 更新最大ID
            LeafAlloc temp = new LeafAlloc();
            temp.setBizTag(key);
            temp.setStep(nextStep);
            dao.updateMaxIdByCustomStepAndConcurrent(temp);
            
            // 重新获取更新后的号段
            leafAlloc = dao.getLeafAlloc(key);
            if (leafAlloc == null) {
                throw new RuntimeException("找不到业务标识: " + key);
            }
            
            segment.setStep(nextStep);
            segment.setMax(leafAlloc.getMaxId());
            segment.setValue(new AtomicLong(leafAlloc.getMaxId() - nextStep));
            
            // 更新buffer
            buffer.setStep(nextStep);
            buffer.setUpdateTimestamp(System.currentTimeMillis());
        }
    }

    /**
     * 等待并睡眠
     */
    private void waitAndSleep(SegmentBuffer buffer) {
        int roll = 0;
        while (buffer.getThreadRunning().get()) {
            roll += 1;
            if (roll > 10000) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                    break;
                } catch (InterruptedException e) {
                    log.warn("等待更新号段时被中断", e);
                    break;
                }
            }
        }
    }

    /**
     * 更新线程工厂
     */
    public static class UpdateThreadFactory implements ThreadFactory {
        private static int threadInitNumber = 0;
        
        private static synchronized int nextThreadNum() {
            return threadInitNumber++;
        }
        
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Leaf-Segment-Update-" + nextThreadNum());
        }
    }
} 