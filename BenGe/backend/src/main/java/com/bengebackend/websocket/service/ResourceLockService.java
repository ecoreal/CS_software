package com.bengebackend.websocket.service;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class ResourceLockService {

    private final RedissonClient redissonClient;

    public <T> T executeWithLock(String lockKey, Supplier<T> task) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;

        try {
            // 如果锁空闲，则抢占，否则等待，最多等待0.3秒
            isLocked = lock.tryLock(300, 3000, TimeUnit.MILLISECONDS);
            if (isLocked) {
                return task.get();
            } else {
                throw new RuntimeException("无法获取锁");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取锁失败", e);
        } finally {
            if(isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
