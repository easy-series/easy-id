package com.easy.id.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis 配置属性
 * 兼容原始 Leaf 项目的配置格式
 */
@ConfigurationProperties(prefix = "redis")
public class RedisProperties {
    
    /**
     * Redis 主机
     */
    private String host = "localhost";
    
    /**
     * Redis 端口
     */
    private int port = 6379;
    
    /**
     * Redis 密码
     */
    private String password = "";
    
    /**
     * Redis 数据库索引
     */
    private int database = 0;

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

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }
} 