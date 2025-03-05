package com.easy.id.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Leaf 配置属性
 * 兼容原始 Leaf 项目的配置格式
 */
@ConfigurationProperties(prefix = "leaf")
public class LeafProperties {
    
    /**
     * 服务名称
     */
    private String name = "default";
    
    /**
     * ZooKeeper 配置
     */
    private ZkConfig zk = new ZkConfig();
    
    /**
     * 数据库配置
     */
    private JdbcConfig jdbc = new JdbcConfig();
    
    /**
     * Redis 配置
     */
    private RedisConfig redis = new RedisConfig();
    
    public static class ZkConfig {
        /**
         * ZooKeeper 地址列表，多个地址用逗号分隔
         */
        private String list = "localhost:2181";

        public String getList() {
            return list;
        }

        public void setList(String list) {
            this.list = list;
        }
    }
    
    public static class JdbcConfig {
        /**
         * 数据库 URL
         */
        private String url;
        
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
        private String driver = "com.mysql.cj.jdbc.Driver";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
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

        public String getDriver() {
            return driver;
        }

        public void setDriver(String driver) {
            this.driver = driver;
        }
    }
    
    public static class RedisConfig {
        /**
         * Redis 号段配置
         */
        private SegmentConfig segment = new SegmentConfig();
        
        public static class SegmentConfig {
            /**
             * 号段步长
             */
            private int step = 1000;
            
            /**
             * 更新下一个号段的触发百分比
             */
            private double updatePercent = 0.9;
            
            /**
             * Redis 键前缀
             */
            private String keyPrefix = "leaf:segment:";
            
            /**
             * 最大步长
             */
            private int maxStep = 1000000;
            
            /**
             * 最小步长
             */
            private int minStep = 1000;
            
            /**
             * 是否启用自适应步长
             */
            private boolean enableAdaptiveStep = true;
            
            /**
             * 步长调整窗口（毫秒）
             */
            private long stepAdjustWindow = 900000;

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
        }

        public SegmentConfig getSegment() {
            return segment;
        }

        public void setSegment(SegmentConfig segment) {
            this.segment = segment;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ZkConfig getZk() {
        return zk;
    }

    public void setZk(ZkConfig zk) {
        this.zk = zk;
    }

    public JdbcConfig getJdbc() {
        return jdbc;
    }

    public void setJdbc(JdbcConfig jdbc) {
        this.jdbc = jdbc;
    }

    public RedisConfig getRedis() {
        return redis;
    }

    public void setRedis(RedisConfig redis) {
        this.redis = redis;
    }
} 