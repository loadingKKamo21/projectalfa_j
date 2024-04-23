package com.project.alfa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@EnableCaching
@Configuration
public class CacheConfig extends CachingConfigurerSupport {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        return RedisCacheManager.builder(redisConnectionFactory)
                                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig(
                                                                              Thread.currentThread().getContextClassLoader())
                                                                      .entryTtl(Duration.ofHours(1))
                                                                      .serializeKeysWith(
                                                                              RedisSerializationContext.SerializationPair.fromSerializer(
                                                                                      new StringRedisSerializer()))
                                                                      .serializeValuesWith(
                                                                              RedisSerializationContext.SerializationPair.fromSerializer(
                                                                                      new GenericJackson2JsonRedisSerializer(
                                                                                              objectMapper))))
                                .transactionAware()
                                .build();
    }
    
    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }
    
    @Bean
    public KeyGenerator customKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            for (Object param : params) {
                if (sb.length() > 0)
                    sb.append(",");
                sb.append(param.toString());
            }
            return sb.toString();
        };
    }
    
}
