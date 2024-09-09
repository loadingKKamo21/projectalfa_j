package com.project.alfa.config;

import com.project.alfa.aop.ProjectAspects.LockAspect;
import com.project.alfa.aop.ProjectAspects.LogTraceAspect;
import com.project.alfa.aop.trace.logtrace.LogTrace;
import com.project.alfa.aop.trace.logtrace.ThreadLocalLogTrace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AopConfig {
    
    @Bean
    public LogTraceAspect logTraceAspect(LogTrace logTrace) {
        return new LogTraceAspect(logTrace);
    }
    
    @Bean
    public LockAspect lockAspect() {
        return new LockAspect();
    }
    
    @Bean
    public LogTrace logTrace() {
        return new ThreadLocalLogTrace();
    }
    
}
