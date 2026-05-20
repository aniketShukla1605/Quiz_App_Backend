package com.microservice.question.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Configuration
public class CacheConfig {
    ObjectMapper objectMapper = new ObjectMapper();
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJacksonJsonRedisSerializer(objectMapper))
                );

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .withCacheConfiguration("allQuestions",        config.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("questionsByCategory", config.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("generatedQuestions",  config.entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration("questionsFromId",     config.entryTtl(Duration.ofMinutes(30)))
                .build();
    }
}

