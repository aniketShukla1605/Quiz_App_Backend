package com.microservice.authService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(RedisSerializer.json())
                );

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .withCacheConfiguration("quiz",
                        config.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("questions",
                        config.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("resultHistory",
                        config.entryTtl(Duration.ofMinutes(5)))
                .build();
    }
}
