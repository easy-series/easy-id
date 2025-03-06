package com.leaf.starter.example;

import com.leaf.starter.common.Result;
import com.leaf.starter.service.LeafService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Leaf ID生成器示例控制器
 * 注意：此控制器仅作为示例，实际使用时应该在自己的应用中创建类似的控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/leaf")
public class LeafController {

    @Autowired
    private LeafService leafService;

    /**
     * 获取ID（返回完整结果对象）
     *
     * @param key 业务标识
     * @return ID生成结果
     */
    @GetMapping("/id/{key}")
    public Result getId(@PathVariable String key) {
        log.info("生成ID，业务标识: {}", key);
        return leafService.getId(key);
    }

    /**
     * 获取ID（直接返回ID值）
     *
     * @param key 业务标识
     * @return ID值
     */
    @GetMapping("/id/long/{key}")
    public Map<String, Object> getIdAsLong(@PathVariable String key) {
        log.info("生成ID（Long类型），业务标识: {}", key);
        long id = leafService.getIdAsLong(key);
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("id", id);
        return result;
    }

    /**
     * 获取ID（直接返回ID值的字符串形式）
     *
     * @param key 业务标识
     * @return ID值的字符串形式
     */
    @GetMapping("/id/string/{key}")
    public Map<String, Object> getIdAsString(@PathVariable String key) {
        log.info("生成ID（String类型），业务标识: {}", key);
        String id = leafService.getIdAsString(key);
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("id", id);
        return result;
    }

    /**
     * 批量获取ID
     *
     * @param key 业务标识
     * @param count 数量
     * @return ID列表
     */
    @GetMapping("/ids/{key}/{count}")
    public Map<String, Object> getBatchIds(@PathVariable String key, @PathVariable int count) {
        log.info("批量生成ID，业务标识: {}, 数量: {}", key, count);
        if (count <= 0 || count > 10000) {
            count = 10;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("count", count);
        
        long[] ids = new long[count];
        for (int i = 0; i < count; i++) {
            ids[i] = leafService.getIdAsLong(key);
        }
        
        result.put("ids", ids);
        return result;
    }
} 