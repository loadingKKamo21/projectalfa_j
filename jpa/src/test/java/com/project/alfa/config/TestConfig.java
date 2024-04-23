package com.project.alfa.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {
    
    @Bean
    public DummyGenerator dummyGenerator() {
        return new DummyGenerator();
    }
    
}
