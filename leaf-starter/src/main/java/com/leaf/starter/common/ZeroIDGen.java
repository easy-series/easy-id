package com.leaf.starter.common;

import lombok.extern.slf4j.Slf4j;

/**
 * 零ID生成器实现（始终返回0）
 */
@Slf4j
public class ZeroIDGen implements IDGen {
    @Override
    public Result get(String key) {
        return new Result(0, Status.SUCCESS);
    }

    @Override
    public boolean init() {
        log.warn("零ID生成器已初始化，将始终返回0");
        return true;
    }
} 