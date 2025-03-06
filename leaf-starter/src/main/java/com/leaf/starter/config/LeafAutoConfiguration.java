package com.leaf.starter.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.leaf.starter.common.IDGen;
import com.leaf.starter.common.ZeroIDGen;
import com.leaf.starter.exception.LeafException;
import com.leaf.starter.properties.LeafProperties;
import com.leaf.starter.segment.SegmentIDGenImpl;
import com.leaf.starter.segment.dao.IDAllocDao;
import com.leaf.starter.segment.dao.impl.IDAllocDaoImpl;
import com.leaf.starter.segment.redis.RedisSegmentConfig;
import com.leaf.starter.segment.redis.RedisSegmentIDGenImpl;
import com.leaf.starter.service.LeafService;
import com.leaf.starter.service.impl.LeafServiceImpl;
import com.leaf.starter.snowflake.SnowflakeIDGenImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Leaf ID生成器自动配置
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(LeafProperties.class)
public class LeafAutoConfiguration {

    /**
     * 配置号段模式ID生成器
     */
    @Bean
    @ConditionalOnProperty(prefix = "leaf.segment", name = "enable", havingValue = "true")
    public IDGen segmentIDGen(LeafProperties properties) {
        try {
            // 配置数据源
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setUrl(properties.getSegment().getJdbcUrl());
            dataSource.setUsername(properties.getSegment().getUsername());
            dataSource.setPassword(properties.getSegment().getPassword());
            dataSource.setDriverClassName(properties.getSegment().getDriverClassName());
            dataSource.init();

            // 配置DAO
            IDAllocDao dao = new IDAllocDaoImpl(dataSource);

            // 配置ID生成器
            SegmentIDGenImpl idGen = new SegmentIDGenImpl();
            idGen.setDao(dao);
            if (idGen.init()) {
                log.info("号段模式ID生成器初始化成功");
                return idGen;
            } else {
                throw new LeafException("号段模式ID生成器初始化失败");
            }
        } catch (Exception e) {
            log.error("初始化号段模式ID生成器失败", e);
            throw new LeafException("初始化号段模式ID生成器失败: " + e.getMessage(), e);
        }
    }

    /**
     * 配置雪花算法模式ID生成器
     */
    @Bean
    @ConditionalOnProperty(prefix = "leaf.snowflake", name = "enable", havingValue = "true")
    public IDGen snowflakeIDGen(LeafProperties properties) {
        try {
            // 配置雪花算法ID生成器
            SnowflakeIDGenImpl idGen = new SnowflakeIDGenImpl(
                    properties.getSnowflake().getZkAddress(),
                    properties.getSnowflake().getPort(),
                    properties.getSnowflake().getTwepoch()
            );
            
            if (idGen.init()) {
                log.info("雪花算法模式ID生成器初始化成功");
                return idGen;
            } else {
                throw new LeafException("雪花算法模式ID生成器初始化失败");
            }
        } catch (Exception e) {
            log.error("初始化雪花算法模式ID生成器失败", e);
            throw new LeafException("初始化雪花算法模式ID生成器失败: " + e.getMessage(), e);
        }
    }

    /**
     * 配置Redis号段模式ID生成器
     */
    @Bean
    @ConditionalOnProperty(prefix = "leaf.segment-redis", name = "enable", havingValue = "true")
    public IDGen redisSegmentIDGen(LeafProperties properties) {
        try {
            // 获取Redis URI
            String redisUri = properties.getSegmentRedis().getRedisUri();
            
            // 配置Redis号段生成器
            RedisSegmentConfig segmentConfig = new RedisSegmentConfig();
            segmentConfig.setStep(properties.getSegmentRedis().getStep());
            segmentConfig.setUpdatePercent(properties.getSegmentRedis().getUpdatePercent());
            segmentConfig.setKeyPrefix(properties.getSegmentRedis().getKeyPrefix());
            segmentConfig.setEnableAdaptiveStep(properties.getSegmentRedis().isEnableAdaptiveStep());
            segmentConfig.setMinStep(properties.getSegmentRedis().getMinStep());
            segmentConfig.setMaxStep(properties.getSegmentRedis().getMaxStep());

            // 创建Redis号段ID生成器
            RedisSegmentIDGenImpl idGen = new RedisSegmentIDGenImpl(redisUri, segmentConfig);
            
            if (idGen.init()) {
                log.info("Redis号段模式ID生成器初始化成功");
                return idGen;
            } else {
                throw new LeafException("Redis号段模式ID生成器初始化失败");
            }
        } catch (Exception e) {
            log.error("初始化Redis号段模式ID生成器失败", e);
            throw new LeafException("初始化Redis号段模式ID生成器失败: " + e.getMessage(), e);
        }
    }

    /**
     * 配置默认的零ID生成器（当没有启用任何ID生成模式时）
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(IDGen.class)
    public IDGen defaultIDGen() {
        log.warn("未启用任何ID生成模式，将使用零ID生成器（始终返回0）");
        return new ZeroIDGen();
    }

    /**
     * 配置Leaf服务
     */
    @Bean
    @ConditionalOnMissingBean(LeafService.class)
    public LeafService leafService(IDGen idGen) {
        return new LeafServiceImpl(idGen);
    }
} 