package com.project.alfa.aop;

import org.aspectj.lang.annotation.Pointcut;

public class Pointcuts {
    
    @Pointcut("execution(* *.*(..))")
    public void allMatch() {}
    
    @Pointcut("execution(* com.project.alfa.repositories..*.*(..))")
    public void allRepositories() {}
    
    @Pointcut("execution(* com.project.alfa.services..*.*(..))")
    public void allServices() {}
    
    @Pointcut("execution(* com.project.alfa.controllers..*.*(..))")
    public void allControllers() {}
    
    @Pointcut("allRepositories() || allServices() || allControllers()")
    public void allMvc() {}
    
    @Pointcut("execution(* com.project.alfa.utils..*.*(..))")
    public void allUtils() {}
    
    @Pointcut("execution(* com.project.alfa.security..*.*(..))")
    public void authentication() {}
    
}
