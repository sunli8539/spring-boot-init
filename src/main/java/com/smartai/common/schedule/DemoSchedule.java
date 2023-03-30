package com.smartai.common.schedule;

import lombok.extern.slf4j.Slf4j;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

@Slf4j
public class DemoSchedule {

    @Resource
    private RedissonClient redissonClient;

    // @Scheduled(cron = "*/10 * * * * *")
    public void doSomething() {
        RLock lock = redissonClient.getLock("lockKey");
        try {
            if (lock.tryLock(1, 60, TimeUnit.SECONDS)) {
                // do something
                System.out.println(123);
            }
        } catch (InterruptedException e) {
            log.error("error:{}", e);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }
}
