package com.easy.id.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 雪花算法配置属性
 */
@ConfigurationProperties(prefix = "easy.id.snowflake")
public class SnowflakeIdGeneratorProperties {
    
    /**
     * 是否启用雪花算法
     */
    private boolean enable = false;
    
    /**
     * ZooKeeper地址
     */
    private String zkAddress;
    
    /**
     * 服务端口
     */
    private int port;
    
    /**
     * ZooKeeper路径
     */
    private String zkPath = "/easy/id/snowflake";

    // Getters and Setters
    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getZkAddress() {
        return zkAddress;
    }

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getZkPath() {
        return zkPath;
    }

    public void setZkPath(String zkPath) {
        this.zkPath = zkPath;
    }
} 