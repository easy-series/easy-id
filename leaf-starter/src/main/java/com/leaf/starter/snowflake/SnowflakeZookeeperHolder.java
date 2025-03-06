package com.leaf.starter.snowflake;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leaf.starter.common.Utils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Snowflake算法的ZooKeeper工作节点管理
 */
@Slf4j
public class SnowflakeZookeeperHolder {
    /**
     * 保存自身的IP地址
     */
    private String ip;
    
    /**
     * 保存自身的端口
     */
    private String port;
    
    /**
     * 保存自身的工作节点ID
     */
    private long workerId;
    
    /**
     * ZooKeeper地址
     */
    private String zkAddress;
    
    /**
     * ZooKeeper客户端
     */
    private CuratorFramework curator;
    
    /**
     * 工作节点路径
     */
    private static final String ZK_PATH = "/leaf/snowflake/node";
    
    /**
     * 临时节点路径
     */
    private static final String EPHEMERAL_PATH = ZK_PATH + "/";
    
    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_TIMES = 3;
    
    /**
     * 最大工作节点ID
     */
    private static final long MAX_WORKER_ID = 1023;
    
    /**
     * JSON序列化工具
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SnowflakeZookeeperHolder(String ip, String port, String zkAddress) {
        this.ip = ip;
        this.port = port;
        this.zkAddress = zkAddress;
    }

    public boolean init() {
        try {
            // 创建ZooKeeper客户端
            curator = createWithOptions(zkAddress, new RetryUntilElapsed(1000, 4), 10000, 6000);
            curator.start();
            
            // 确保根路径存在
            Stat stat = curator.checkExists().forPath(ZK_PATH);
            if (stat == null) {
                curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(ZK_PATH);
            }
            
            // 注册工作节点
            workerId = registerWorkerId();
            if (workerId >= 0) {
                // 定时上报本节点状态
                scheduledUploadData();
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("初始化SnowflakeZookeeperHolder失败", e);
            return false;
        }
    }

    /**
     * 创建ZooKeeper客户端
     */
    private CuratorFramework createWithOptions(String zkAddress, RetryUntilElapsed retryPolicy, int connectionTimeoutMs, int sessionTimeoutMs) {
        return CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .build();
    }

    /**
     * 注册工作节点ID
     */
    private long registerWorkerId() throws Exception {
        // 尝试使用IP+PORT作为节点标识
        String listenAddress = ip + ":" + port;
        
        // 先检查是否已有该节点
        String path = EPHEMERAL_PATH + listenAddress;
        Stat stat = curator.checkExists().forPath(path);
        if (stat != null) {
            byte[] bytes = curator.getData().forPath(path);
            WorkerNodeInfo nodeInfo = objectMapper.readValue(new String(bytes), WorkerNodeInfo.class);
            workerId = nodeInfo.getWorkerId();
            log.info("找到已存在的工作节点: {}, workerId: {}", path, workerId);
            return workerId;
        }
        
        // 尝试查找可用的workerId
        List<String> children = curator.getChildren().forPath(ZK_PATH);
        for (int i = 0; i <= MAX_WORKER_ID; i++) {
            boolean conflict = false;
            for (String child : children) {
                String childPath = EPHEMERAL_PATH + child;
                byte[] bytes = curator.getData().forPath(childPath);
                WorkerNodeInfo nodeInfo = objectMapper.readValue(new String(bytes), WorkerNodeInfo.class);
                if (nodeInfo.getWorkerId() == i) {
                    conflict = true;
                    break;
                }
            }
            
            if (!conflict) {
                // 找到可用的workerId
                WorkerNodeInfo nodeInfo = new WorkerNodeInfo();
                nodeInfo.setIp(ip);
                nodeInfo.setPort(port);
                nodeInfo.setWorkerId(i);
                nodeInfo.setTimestamp(System.currentTimeMillis());
                
                try {
                    curator.create().withMode(CreateMode.EPHEMERAL).forPath(path, objectMapper.writeValueAsBytes(nodeInfo));
                    workerId = i;
                    log.info("注册工作节点成功: {}, workerId: {}", path, workerId);
                    return workerId;
                } catch (Exception e) {
                    log.error("注册工作节点失败: {}", path, e);
                }
            }
        }
        
        log.error("无法找到可用的workerId");
        return -1;
    }

    /**
     * 定时上报本节点状态
     */
    private void scheduledUploadData() {
        Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "schedule-upload-time");
                thread.setDaemon(true);
                return thread;
            }
        }).scheduleWithFixedDelay(() -> {
            try {
                updateNodeInfo();
            } catch (Exception e) {
                log.error("上报工作节点状态失败", e);
            }
        }, 1L, 3L, TimeUnit.SECONDS);
    }

    /**
     * 更新节点信息
     */
    private void updateNodeInfo() throws Exception {
        String listenAddress = ip + ":" + port;
        String path = EPHEMERAL_PATH + listenAddress;
        
        WorkerNodeInfo nodeInfo = new WorkerNodeInfo();
        nodeInfo.setIp(ip);
        nodeInfo.setPort(port);
        nodeInfo.setWorkerId(workerId);
        nodeInfo.setTimestamp(System.currentTimeMillis());
        
        curator.setData().forPath(path, objectMapper.writeValueAsBytes(nodeInfo));
    }

    /**
     * 获取工作节点ID
     */
    public long getWorkerId() {
        return workerId;
    }

    /**
     * 工作节点信息
     */
    @Data
    static class WorkerNodeInfo {
        /**
         * IP地址
         */
        private String ip;
        
        /**
         * 端口
         */
        private String port;
        
        /**
         * 工作节点ID
         */
        private long workerId;
        
        /**
         * 时间戳
         */
        private long timestamp;
    }
} 