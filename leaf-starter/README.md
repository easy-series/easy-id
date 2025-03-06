# Leaf Spring Boot Starter

Leaf Spring Boot Starter 是一个基于美团 Leaf 分布式ID生成系统的 Spring Boot 启动器，提供了简单易用的分布式ID生成解决方案。

## 特性

- 支持号段模式（Segment）：基于数据库的号段模式
- 支持雪花算法（Snowflake）：基于ZooKeeper的雪花算法实现
- 支持Redis号段模式：基于Redis的号段模式实现
- 自动配置：与Spring Boot无缝集成
- 高性能：号段模式提供每秒生成数万ID的能力
- 高可用：多种ID生成策略保证系统可用性

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.leaf</groupId>
    <artifactId>leaf-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 配置属性

在`application.properties`或`application.yml`中添加相关配置：

```properties
# 服务名称
leaf.name=leaf-service

# 选择一种ID生成模式（以下三种模式选择一种即可）

# 1. 号段模式配置
leaf.segment.enable=true
leaf.segment.jdbc-url=jdbc:mysql://localhost:3306/leaf?useUnicode=true&characterEncoding=utf8&useSSL=false
leaf.segment.username=root
leaf.segment.password=123456
leaf.segment.driver-class-name=com.mysql.cj.jdbc.Driver

# 2. 雪花算法配置
# leaf.snowflake.enable=true
# leaf.snowflake.zk-address=localhost
# leaf.snowflake.port=2181
# leaf.snowflake.twepoch=1288834974657

# 3. Redis号段模式配置
# leaf.segment-redis.enable=true
# leaf.segment-redis.host=localhost
# leaf.segment-redis.port=6379
# leaf.segment-redis.password=
# leaf.segment-redis.database=0
# leaf.segment-redis.step=1000
# leaf.segment-redis.update-percent=0.9
# leaf.segment-redis.key-prefix=leaf:segment:
# leaf.segment-redis.enable-adaptive-step=true
# leaf.segment-redis.min-step=1000
# leaf.segment-redis.max-step=100000
```

### 3. 数据库初始化（号段模式）

如果使用号段模式，需要初始化数据库表：

```sql
CREATE DATABASE leaf;
USE leaf;

CREATE TABLE leaf_alloc (
  biz_tag VARCHAR(128) NOT NULL DEFAULT '',
  max_id BIGINT NOT NULL DEFAULT 1,
  step INT NOT NULL,
  description VARCHAR(256) DEFAULT NULL,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (biz_tag)
) ENGINE=InnoDB;

-- 初始化一些业务标签
INSERT INTO leaf_alloc(biz_tag, max_id, step, description) VALUES('leaf-segment-test', 1, 2000, '测试号段');
INSERT INTO leaf_alloc(biz_tag, max_id, step, description) VALUES('order', 1, 100000, '订单ID');
INSERT INTO leaf_alloc(biz_tag, max_id, step, description) VALUES('user', 1, 50000, '用户ID');
```

### 4. 使用示例

```java
import com.leaf.starter.common.Result;
import com.leaf.starter.service.LeafService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LeafController {

    @Autowired
    private LeafService leafService;

    @GetMapping("/api/id/{key}")
    public Result getId(@PathVariable String key) {
        return leafService.getId(key);
    }

    @GetMapping("/api/id/long/{key}")
    public long getIdAsLong(@PathVariable String key) {
        return leafService.getIdAsLong(key);
    }

    @GetMapping("/api/id/string/{key}")
    public String getIdAsString(@PathVariable String key) {
        return leafService.getIdAsString(key);
    }
}
```

## 配置说明

### 通用配置

| 配置项 | 说明 | 默认值 |
| --- | --- | --- |
| leaf.name | 服务名称 | leaf-service |

### 号段模式配置

| 配置项 | 说明 | 默认值 |
| --- | --- | --- |
| leaf.segment.enable | 是否启用号段模式 | false |
| leaf.segment.jdbc-url | 数据库连接URL | - |
| leaf.segment.username | 数据库用户名 | - |
| leaf.segment.password | 数据库密码 | - |
| leaf.segment.driver-class-name | 数据库驱动类名 | com.mysql.cj.jdbc.Driver |

### 雪花算法配置

| 配置项 | 说明 | 默认值 |
| --- | --- | --- |
| leaf.snowflake.enable | 是否启用雪花算法 | false |
| leaf.snowflake.zk-address | ZooKeeper地址 | - |
| leaf.snowflake.port | ZooKeeper端口 | 2181 |
| leaf.snowflake.twepoch | 起始时间戳 | 1288834974657 |

### Redis号段模式配置

| 配置项 | 说明 | 默认值 |
| --- | --- | --- |
| leaf.segment-redis.enable | 是否启用Redis号段模式 | false |
| leaf.segment-redis.uri | Redis URI（优先使用） | - |
| leaf.segment-redis.host | Redis主机地址 | localhost |
| leaf.segment-redis.port | Redis端口 | 6379 |
| leaf.segment-redis.password | Redis密码 | - |
| leaf.segment-redis.database | Redis数据库索引 | 0 |
| leaf.segment-redis.step | 号段步长 | 1000 |
| leaf.segment-redis.update-percent | 更新阈值百分比 | 0.9 |
| leaf.segment-redis.key-prefix | Redis键前缀 | leaf:segment: |
| leaf.segment-redis.enable-adaptive-step | 是否启用自适应步长 | true |
| leaf.segment-redis.min-step | 最小步长 | 1000 |
| leaf.segment-redis.max-step | 最大步长 | 100000 |

## 注意事项

1. 只能启用一种ID生成模式，如果同时启用多种模式，将按照以下优先级选择：号段模式 > 雪花算法 > Redis号段模式
2. 如果没有启用任何模式，将使用默认的ZeroIDGen（仅用于测试，返回固定值0）
3. 号段模式需要初始化数据库表，并提前插入业务标签
4. 雪花算法依赖ZooKeeper进行工作节点管理
5. Redis号段模式会自动在Redis中创建所需的键

## 许可证

Apache License 2.0 