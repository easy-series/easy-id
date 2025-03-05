package com.sankuai.inf.leaf.segment.redis;

public class RedisSegmentConfig {

    /**
     * 号段步长
     */
    private int step = 1000;

    /**
     * 更新下一个号段的触发百分比
     */
    private double updatePercent = 0.9;

    /**
     * Redis键前缀
     */
    private String keyPrefix = "leaf:segment:";

    /**
     * 最大步长
     */
    private int maxStep = 100000;

    /**
     * 最小步长
     */
    private int minStep = 1000;

    /**
     * 是否启用自适应步长
     */
    private boolean enableAdaptiveStep = false;

    /**
     * 自适应步长的时间窗口(毫秒)
     */
    private long stepAdjustWindow = 15 * 60 * 1000L;

    private long updateIntervalThreshold = 2000; // 2秒

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public double getUpdatePercent() {
        return updatePercent;
    }

    public void setUpdatePercent(double updatePercent) {
        this.updatePercent = updatePercent;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public int getMaxStep() {
        return maxStep;
    }

    public void setMaxStep(int maxStep) {
        this.maxStep = maxStep;
    }

    public int getMinStep() {
        return minStep;
    }

    public void setMinStep(int minStep) {
        this.minStep = minStep;
    }

    public boolean isEnableAdaptiveStep() {
        return enableAdaptiveStep;
    }

    public void setEnableAdaptiveStep(boolean enableAdaptiveStep) {
        this.enableAdaptiveStep = enableAdaptiveStep;
    }

    public long getStepAdjustWindow() {
        return stepAdjustWindow;
    }

    public void setStepAdjustWindow(long stepAdjustWindow) {
        this.stepAdjustWindow = stepAdjustWindow;
    }

    public long getUpdateIntervalThreshold() {
        return updateIntervalThreshold;
    }

    public void setUpdateIntervalThreshold(long updateIntervalThreshold) {
        this.updateIntervalThreshold = updateIntervalThreshold;
    }
}