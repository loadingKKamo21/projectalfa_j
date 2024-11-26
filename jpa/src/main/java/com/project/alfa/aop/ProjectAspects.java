package com.project.alfa.aop;

import com.project.alfa.aop.lock.LockManager;
import com.project.alfa.aop.trace.TraceStatus;
import com.project.alfa.aop.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ProjectAspects {
    
    private static final int MAX_RETIRES = 5;
    
    @Aspect
    @Order(2)
    @RequiredArgsConstructor
    public static class LogTraceAspect {
        
        private final LogTrace logTrace;
        
        @Around("com.project.alfa.aop.Pointcuts.allMvc() || com.project.alfa.aop.Pointcuts.allUtils()")
        public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
            TraceStatus status = null;
            try {
                String message = joinPoint.getSignature().toShortString();
                status = logTrace.begin(message);
                
                Object result = joinPoint.proceed();
                
                logTrace.end(status);
                return result;
            } catch (Exception e) {
                logTrace.exception(status, e);
                throw e;
            }
        }
        
    }
    
    @Aspect
    @Order(1)
    @RequiredArgsConstructor
    public static class LockAspect {
        
        private final LockManager lockManager;
        
        @Around("@annotation(com.project.alfa.aop.annotation.LockAop)")
        public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
            String key = extractKeyFromArguments(joinPoint.getArgs());

            if (key == null)
                return joinPoint.proceed();

            ReentrantLock lock = lockManager.getLock(key);
            int     attempts = 0;
            boolean isLocked = false;
            
            try {
                while (attempts < MAX_RETIRES) {
                    attempts++;
                    try {
                        if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                            isLocked = true;
                            break;
                        } else
                            Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                if (!isLocked)
                    throw new RuntimeException("Failed to acquire lock after " + MAX_RETIRES + " attempts");
                
                return joinPoint.proceed();
            } finally {
                if (isLocked) {
                    lock.unlock();
                    lockManager.releaseLock(key, lock);
                }
            }
        }

        private String extractKeyFromArguments(final Object[] args) {
            return args.length > 0 ? String.valueOf(args[0]) : null;
        }

    }
    
}
