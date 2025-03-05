package com.sankuai.inf.leaf.segment.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sankuai.inf.leaf.IDGen;
import com.sankuai.inf.leaf.common.Result;
import com.sankuai.inf.leaf.common.Status;

public class RedisSegmentIDGenImpl implements IDGen {
    private static final Logger logger = LoggerFactory.getLogger(RedisSegmentIDGenImpl.class);

    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> syncCommands;
    private RedisSegmentConfig config;

    // 缓存每个业务标识的当前ID值
    private Map<String, AtomicLong> currentIds = new ConcurrentHashMap<>();
    // 缓存每个业务标识的最大ID值
    private Map<String, Long> maxIds = new ConcurrentHashMap<>();
    // 缓存每个业务标识的步长
    private Map<String, Integer> steps = new ConcurrentHashMap<>();
    // 缓存每个业务标识的上次更新时间
    private Map<String, Long> lastUpdateTimes = new ConcurrentHashMap<>();

    // 监控指标
    private Map<String, Long> totalCalls = new ConcurrentHashMap<>();
    private Map<String, Long> totalTime = new ConcurrentHashMap<>();

    public RedisSegmentIDGenImpl(String redisUri, RedisSegmentConfig config) {
        this.redisClient = RedisClient.create(redisUri);
        this.connection = redisClient.connect();
        this.syncCommands = connection.sync();
        this.config = config;
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public Result get(String key) {
        if (key == null || key.isEmpty()) {
            return new Result(-1, Status.EXCEPTION);
        }

        // 记录性能
        StopWatch stopWatch = new Slf4JStopWatch();

        try {
            // 获取或初始化当前ID
            AtomicLong currentId = currentIds.computeIfAbsent(key, k -> new AtomicLong(0));

            // 检查是否需要获取新的号段
            if (currentId.get() >= maxIds.getOrDefault(key, 0L)) {
                synchronized (this) {
                    // 双重检查，避免多线程问题
                    if (currentId.get() >= maxIds.getOrDefault(key, 0L)) {
                        // 获取新号段
                        long newMaxId = getNewSegmentFromRedis(key);
                        if (newMaxId < 0) {
                            return new Result(newMaxId, Status.EXCEPTION);
                        }

                        // 更新最大ID
                        maxIds.put(key, newMaxId);

                        // 重置当前ID
                        currentId.set(newMaxId - steps.getOrDefault(key, config.getStep()));
                    }
                }
            }

            // 获取并递增当前ID
            long id = currentId.incrementAndGet();

            // 记录性能指标
            updateMetrics(key, stopWatch.getElapsedTime());

            return new Result(id, Status.SUCCESS);
        } catch (Exception e) {
            logger.error("Generate ID error", e);
            return new Result(-1, Status.EXCEPTION);
        } finally {
            stopWatch.stop("RedisSegmentIDGen.get");
        }
    }

    private long getNewSegmentFromRedis(String key) {
        try {
            String fullKey = config.getKeyPrefix() + key;

            // 获取当前步长
            int step = steps.getOrDefault(key, config.getStep());

            // 使用Lua脚本原子性地获取并更新值
            String script = "local current = tonumber(redis.call('get', KEYS[1]) or '0') " +
                    "local step = tonumber(ARGV[1]) " +
                    "local newValue = current + step " +
                    "redis.call('set', KEYS[1], newValue) " +
                    "return newValue";

            Long result = syncCommands.eval(script,
                    io.lettuce.core.ScriptOutputType.INTEGER,
                    new String[] { fullKey },
                    String.valueOf(step));

            if (result == null) {
                return -1;
            }

            // 更新步长（自适应步长）
            if (config.isEnableAdaptiveStep()) {
                updateStep(key);
            }

            // 更新步长缓存
            steps.put(key, step);

            // 更新最后更新时间
            lastUpdateTimes.put(key, System.currentTimeMillis());

            return result;
        } catch (Exception e) {
            logger.error("Get new segment from Redis error", e);
            return -1;
        }
    }

    private void updateStep(String key) {
        // 获取当前步长
        int step = steps.getOrDefault(key, config.getStep());

        // 获取上次更新时间
        long lastUpdateTime = lastUpdateTimes.getOrDefault(key, 0L);
        long now = System.currentTimeMillis();

        // 如果上次更新时间太近，增加步长
        if (lastUpdateTime > 0 && now - lastUpdateTime < config.getUpdateIntervalThreshold()) {
            // 增加步长，但不超过最大步长
            int newStep = Math.min(step * 2, config.getMaxStep());
            if (newStep != step) {
                logger.info("Adaptive step for {}: {} -> {}", key, step, newStep);
                steps.put(key, newStep);
            }
        }
        // 如果上次更新时间太远，减少步长
        else if (lastUpdateTime > 0 && now - lastUpdateTime > config.getUpdateIntervalThreshold() * 10) {
            // 减少步长，但不低于最小步长
            int newStep = Math.max(step / 2, config.getMinStep());
            if (newStep != step) {
                logger.info("Adaptive step for {}: {} -> {}", key, step, newStep);
                steps.put(key, newStep);
            }
        }
    }

    private void updateMetrics(String key, long time) {
        // 更新调用次数
        totalCalls.compute(key, (k, v) -> (v == null) ? 1 : v + 1);

        // 更新总耗时
        totalTime.compute(key, (k, v) -> (v == null) ? time : v + time);
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // 基本指标
        metrics.put("totalKeys", currentIds.size());

        // 每个key的指标
        Map<String, Object> keysMetrics = new HashMap<>();
        for (String key : currentIds.keySet()) {
            Map<String, Object> keyMetric = new HashMap<>();
            keyMetric.put("currentId", currentIds.get(key).get());
            keyMetric.put("maxId", maxIds.getOrDefault(key, 0L));
            keyMetric.put("step", steps.getOrDefault(key, config.getStep()));
            keyMetric.put("totalCalls", totalCalls.getOrDefault(key, 0L));

            // 计算平均耗时
            long calls = totalCalls.getOrDefault(key, 0L);
            long time = totalTime.getOrDefault(key, 0L);
            double avgTime = calls > 0 ? (double) time / calls : 0;
            keyMetric.put("avgTime", avgTime);

            keysMetrics.put(key, keyMetric);
        }
        metrics.put("keys", keysMetrics);

        return metrics;
    }

    public void destroy() {
        if (connection != null) {
            connection.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }
    }
}