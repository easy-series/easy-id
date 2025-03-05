package com.easy.id.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis号段模式配置属性
 */
@ConfigurationProperties(prefix = "easy.id.redis")
public class RedisSegmentIdGeneratorProperties {
    
    /**
     * 是否启用Redis号段模式
     */
    private boolean enable = false;
    
    /**
     * Redis主机
     */
    private String host = "localhost";
    
    /**
     * Redis端口
     */
    private int port = 6379;
    
    /**
     * Redis密码
     */
    private String password = "";
    
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
    private String keyPrefix = "easy:id:segment:";
    
    /**
     * 是否启用自适应步长
     */
    private boolean enableAdaptiveStep = true;
    
    /**
     * 最小步长
     */
    private int minStep = 1000;
    
    /**
     * 最大步长
     */
    private int maxStep = 100000;

    // Getters and Setters
    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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

    public boolean isEnableAdaptiveStep() {
        return enableAdaptiveStep;
    }

    public void setEnableAdaptiveStep(boolean enableAdaptiveStep) {
        this.enableAdaptiveStep = enableAdaptiveStep;
    }

    public int getMinStep() {
        return minStep;
    }

    public void setMinStep(int minStep) {
        this.minStep = minStep;
    }

    public int getMaxStep() {
        return maxStep;
    }

    public void setMaxStep(int maxStep) {
        this.maxStep = maxStep;
    }
} 