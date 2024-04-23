package com.project.alfa.aop.trace.logtrace;

import com.project.alfa.aop.trace.TraceStatus;

public interface LogTrace {
    
    TraceStatus begin(String message);
    
    void end(TraceStatus status);
    
    void exception(TraceStatus status, Exception e);
    
}
