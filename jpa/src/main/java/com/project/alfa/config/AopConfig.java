package com.project.alfa.config;

import com.project.alfa.aop.ProjectAspects.LockAspect;
import com.project.alfa.aop.ProjectAspects.LogTraceAspect;
import com.project.alfa.aop.lock.LockManager;
import com.project.alfa.aop.trace.logtrace.LogTrace;
import com.project.alfa.aop.trace.logtrace.ThreadLocalLogTrace;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AopConfig {

    private final LockManager lockManager;
    
    @Bean
    public LogTraceAspect logTraceAspect(LogTrace logTrace) {
        return new LogTraceAspect(logTrace);
    }
    
    @Bean
    public LockAspect lockAspect() {
        return new LockAspect(lockManager);
    }
    
    @Bean
    public LogTrace logTrace() {
        return new ThreadLocalLogTrace();
    }
    
}
