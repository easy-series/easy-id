package com.easy.id.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 数据库号段模式配置属性
 */
@ConfigurationProperties(prefix = "easy.id.segment")
public class SegmentIdGeneratorProperties {
    
    /**
     * 是否启用数据库号段模式
     */
    private boolean enable = false;
    
    /**
     * 数据库URL
     */
    private String jdbcUrl;
    
    /**
     * 数据库用户名
     */
    private String username;
    
    /**
     * 数据库密码
     */
    private String password;
    
    /**
     * 数据库驱动类名
     */
    private String driverClassName = "com.mysql.cj.jdbc.Driver";

    // Getters and Setters
    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }
} 