package com.easy.id.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.easy.id.api.IdGenerator;
import com.easy.id.api.RedisSegmentIdGenerator;
import com.easy.id.api.SegmentIdGenerator;
import com.easy.id.api.SnowflakeIdGenerator;
import com.sankuai.inf.leaf.segment.dao.IDAllocDao;
import com.sankuai.inf.leaf.segment.dao.impl.IDAllocDaoImpl;
import com.sankuai.inf.leaf.segment.redis.RedisSegmentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * ID生成器自动配置类
 */
@Configuration
@EnableConfigurationProperties({
        SegmentIdGeneratorProperties.class,
        RedisSegmentIdGeneratorProperties.class,
        SnowflakeIdGeneratorProperties.class,
        LeafProperties.class,
        RedisProperties.class
})
public class IdGeneratorAutoConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(IdGeneratorAutoConfiguration.class);
    
    /**
     * 配置数据库号段模式的ID生成器（基于 easy.id.segment 配置）
     */
    @Bean
    @ConditionalOnProperty(prefix = "easy.id.segment", name = "enable", havingValue = "true")
    public IdGenerator segmentIdGenerator(SegmentIdGeneratorProperties properties) {
        logger.info("Initializing Segment ID Generator with easy.id.segment properties");
        
        // 配置数据源
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(properties.getJdbcUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        dataSource.setDriverClassName(properties.getDriverClassName());
        
        // 配置DAO
        IDAllocDao idAllocDao = new IDAllocDaoImpl(dataSource);
        
        // 创建ID生成器
        SegmentIdGenerator idGenerator = new SegmentIdGenerator(idAllocDao);
        if (!idGenerator.init()) {
            throw new RuntimeException("Failed to initialize Segment ID Generator");
        }
        
        return idGenerator;
    }
    
    /**
     * 配置数据库号段模式的ID生成器（基于 leaf.jdbc 配置）
     */
    @Bean
    @ConditionalOnProperty(prefix = "leaf.jdbc", name = "url")
    public IdGenerator leafSegmentIdGenerator(LeafProperties leafProperties) {
        logger.info("Initializing Segment ID Generator with leaf.jdbc properties");
        
        // 配置数据源
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(leafProperties.getJdbc().getUrl());
        dataSource.setUsername(leafProperties.getJdbc().getUsername());
        dataSource.setPassword(leafProperties.getJdbc().getPassword());
        dataSource.setDriverClassName(leafProperties.getJdbc().getDriver());
        
        // 配置DAO
        IDAllocDao idAllocDao = new IDAllocDaoImpl(dataSource);
        
        // 创建ID生成器
        SegmentIdGenerator idGenerator = new SegmentIdGenerator(idAllocDao);
        if (!idGenerator.init()) {
            throw new RuntimeException("Failed to initialize Segment ID Generator");
        }
        
        return idGenerator;
    }
    
    /**
     * 配置Redis号段模式的ID生成器（基于 easy.id.redis 配置）
     */
    @Bean
    @ConditionalOnProperty(prefix = "easy.id.redis", name = "enable", havingValue = "true")
    public RedisSegmentIdGenerator redisSegmentIdGenerator(RedisSegmentIdGeneratorProperties properties) {
        logger.info("Initializing Redis Segment ID Generator with easy.id.redis properties");
        
        // 创建ID生成器
        RedisSegmentIdGenerator idGenerator = new RedisSegmentIdGenerator(properties);
        if (!idGenerator.init()) {
            throw new RuntimeException("Failed to initialize Redis Segment ID Generator");
        }
        
        return idGenerator;
    }
    
    /**
     * 配置Redis号段模式的ID生成器（基于 leaf.redis 和 redis 配置）
     */
    @Bean
    @ConditionalOnProperty(prefix = "leaf.redis.segment", name = "key-prefix")
    public RedisSegmentIdGenerator leafRedisSegmentIdGenerator(LeafProperties leafProperties, RedisProperties redisProperties) {
        logger.info("Initializing Redis Segment ID Generator with leaf.redis.segment properties");
        
        // 创建配置对象
        RedisSegmentIdGeneratorProperties properties = new RedisSegmentIdGeneratorProperties();
        properties.setEnable(true);
        properties.setHost(redisProperties.getHost());
        properties.setPort(redisProperties.getPort());
        properties.setPassword(redisProperties.getPassword());
        properties.setStep(leafProperties.getRedis().getSegment().getStep());
        properties.setUpdatePercent(leafProperties.getRedis().getSegment().getUpdatePercent());
        properties.setKeyPrefix(leafProperties.getRedis().getSegment().getKeyPrefix());
        properties.setEnableAdaptiveStep(leafProperties.getRedis().getSegment().isEnableAdaptiveStep());
        properties.setMinStep(leafProperties.getRedis().getSegment().getMinStep());
        properties.setMaxStep(leafProperties.getRedis().getSegment().getMaxStep());
        
        // 创建ID生成器
        RedisSegmentIdGenerator idGenerator = new RedisSegmentIdGenerator(properties);
        if (!idGenerator.init()) {
            throw new RuntimeException("Failed to initialize Redis Segment ID Generator");
        }
        
        return idGenerator;
    }
    
    /**
     * 配置雪花算法的ID生成器（基于 easy.id.snowflake 配置）
     */
    @Bean
    @ConditionalOnProperty(prefix = "easy.id.snowflake", name = "enable", havingValue = "true")
    public IdGenerator snowflakeIdGenerator(SnowflakeIdGeneratorProperties properties) {
        logger.info("Initializing Snowflake ID Generator with easy.id.snowflake properties");
        
        // 创建ID生成器
        SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(properties);
        if (!idGenerator.init()) {
            throw new RuntimeException("Failed to initialize Snowflake ID Generator");
        }
        
        return idGenerator;
    }
    
    /**
     * 配置雪花算法的ID生成器（基于 leaf.zk 配置）
     */
    @Bean
    @ConditionalOnProperty(prefix = "leaf.zk", name = "list")
    public IdGenerator leafSnowflakeIdGenerator(LeafProperties leafProperties) {
        logger.info("Initializing Snowflake ID Generator with leaf.zk properties");
        
        // 创建配置对象
        SnowflakeIdGeneratorProperties properties = new SnowflakeIdGeneratorProperties();
        properties.setEnable(true);
        properties.setZkAddress(leafProperties.getZk().getList());
        properties.setPort(8080); // 默认端口
        properties.setZkPath("/leaf/snowflake/" + leafProperties.getName());
        
        // 创建ID生成器
        SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(properties);
        if (!idGenerator.init()) {
            throw new RuntimeException("Failed to initialize Snowflake ID Generator");
        }
        
        return idGenerator;
    }
    
    /**
     * 默认ID生成器（如果没有配置其他生成器）
     */
    @Bean
    @Primary
    @ConditionalOnProperty(
            prefix = "easy.id",
            name = {"segment.enable", "redis.enable", "snowflake.enable"},
            havingValue = "false",
            matchIfMissing = true
    )
    public IdGenerator defaultIdGenerator(RedisProperties redisProperties) {
        logger.warn("No ID generator is configured, using Redis Segment ID Generator as default");
        
        // 默认使用Redis号段模式
        RedisSegmentIdGeneratorProperties properties = new RedisSegmentIdGeneratorProperties();
        properties.setEnable(true);
        properties.setHost(redisProperties.getHost());
        properties.setPort(redisProperties.getPort());
        properties.setPassword(redisProperties.getPassword());
        
        // 创建ID生成器
        RedisSegmentIdGenerator idGenerator = new RedisSegmentIdGenerator(properties);
        if (!idGenerator.init()) {
            throw new RuntimeException("Failed to initialize default ID Generator");
        }
        
        return idGenerator;
    }
} 