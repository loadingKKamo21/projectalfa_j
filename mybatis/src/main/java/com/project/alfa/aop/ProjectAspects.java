package com.project.alfa.aop;

import com.project.alfa.aop.trace.TraceStatus;
import com.project.alfa.aop.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ProjectAspects {
    
    private static final int MAX_RETRIES = 5;
    
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
    public static class LockAspect {
        
        private final ReentrantLock lock = new ReentrantLock();
        
        @Around("@annotation(com.project.alfa.aop.annotation.LockAop)")
        public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
            int     attempts = 0;
            boolean isLocked = false;
            
            try {
                while (attempts < MAX_RETRIES) {
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
                    throw new RuntimeException("Failed to acquire lock after " + MAX_RETRIES + " attempts");
                
                return joinPoint.proceed();
            } finally {
                if (isLocked)
                    lock.unlock();
            }
        }
        
    }
    
}
