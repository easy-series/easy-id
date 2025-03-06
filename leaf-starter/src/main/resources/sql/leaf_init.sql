-- 创建数据库
CREATE DATABASE IF NOT EXISTS leaf;
USE leaf;

-- 创建号段表
CREATE TABLE IF NOT EXISTS leaf_alloc (
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
INSERT INTO leaf_alloc(biz_tag, max_id, step, description) VALUES('product', 1, 10000, '产品ID');
INSERT INTO leaf_alloc(biz_tag, max_id, step, description) VALUES('customer', 1, 50000, '客户ID');
INSERT INTO leaf_alloc(biz_tag, max_id, step, description) VALUES('trade', 1, 100000, '交易ID'); 