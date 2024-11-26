package com.project.alfa.aop.lock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Component;

@Component
public class LockManager {

    private static final ConcurrentHashMap<String, ReentrantLock> LOCK_MAP = new ConcurrentHashMap<>();

    public ReentrantLock getLock(final String key) {
        return LOCK_MAP.computeIfAbsent(key, k -> new ReentrantLock());
    }

    public void releaseLock(final String key, final ReentrantLock lock) {
        if (!lock.hasQueuedThreads()) LOCK_MAP.remove(key);
    }

}
