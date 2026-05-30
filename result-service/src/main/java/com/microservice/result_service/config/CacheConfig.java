package com.microservice.result_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(GenericJacksonJsonRedisSerializer.builder().build())
                );

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .withCacheConfiguration("attemptResult",       config.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("resultHistory",       config.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("scoreSummary",        config.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("leaderboard",         config.entryTtl(Duration.ofMinutes(2)))
                .build();
    }
}