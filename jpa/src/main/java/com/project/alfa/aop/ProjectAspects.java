package com.project.alfa.aop;

import com.project.alfa.aop.trace.TraceStatus;
import com.project.alfa.aop.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

@Slf4j
public class ProjectAspects {
    
    
    @Aspect
    @Order(1)
    @RequiredArgsConstructor
    public static class LogTraceAspect {
        
        private final LogTrace logTrace;
        
        @Around("com.project.alfa.aop.Pointcuts.allMvc()" +
                "|| com.project.alfa.aop.Pointcuts.allUtils()" +
                "|| com.project.alfa.aop.Pointcuts.authentication()")
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
    
}
