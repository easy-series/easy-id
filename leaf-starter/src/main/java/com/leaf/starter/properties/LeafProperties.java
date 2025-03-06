package com.leaf.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Leaf ID生成器配置属性
 */
@Data
@ConfigurationProperties(prefix = "leaf")
public class LeafProperties {
    /**
     * 服务名称
     */
    private String name = "leaf-service";

    /**
     * 号段模式配置
     */
    private SegmentProperties segment = new SegmentProperties();

    /**
     * 雪花算法模式配置
     */
    private SnowflakeProperties snowflake = new SnowflakeProperties();

    /**
     * Redis号段模式配置
     */
    private SegmentRedisProperties segmentRedis = new SegmentRedisProperties();

    /**
     * 号段模式配置属性
     */
    @Data
    public static class SegmentProperties {
        /**
         * 是否启用号段模式
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
    }

    /**
     * 雪花算法模式配置属性
     */
    @Data
    public static class SnowflakeProperties {
        /**
         * 是否启用雪花算法模式
         */
        private boolean enable = false;

        /**
         * ZooKeeper地址
         */
        private String zkAddress;

        /**
         * 服务端口
         */
        private int port = 2181;

        /**
         * 起始时间戳，默认为2010-11-04 09:42:54
         */
        private long twepoch = 1288834974657L;
    }

    /**
     * Redis号段模式配置属性
     */
    @Data
    public static class SegmentRedisProperties {
        /**
         * 是否启用Redis号段模式
         */
        private boolean enable = false;

        /**
         * Redis URI
         */
        private String uri;
        
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
         * Redis数据库索引
         */
        private int database = 0;

        /**
         * 号段步长
         */
        private int step = 1000;

        /**
         * 更新阈值百分比
         */
        private double updatePercent = 0.9;

        /**
         * 键前缀
         */
        private String keyPrefix = "leaf:segment:";

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
        
        /**
         * 获取Redis URI
         */
        public String getRedisUri() {
            if (uri != null && !uri.isEmpty()) {
                return uri;
            }
            
            if (password == null || password.isEmpty()) {
                return String.format("redis://%s:%d/%d", host, port, database);
            } else {
                return String.format("redis://%s@%s:%d/%d", password, host, port, database);
            }
        }
    }
} 