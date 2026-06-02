package com.fraudshield.fraudshield.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.cache.annotation.EnableCaching;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    // ─────────────────────────────────────────────────────────────────
    // BEAN 1: Connection Factory
    // How Spring connects to Redis server
    // ─────────────────────────────────────────────────────────────────
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    // ─────────────────────────────────────────────────────────────────
    // BEAN 2: RedisTemplate — for complex objects
    // Used when storing Java objects as JSON in Redis
    // ─────────────────────────────────────────────────────────────────
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setConnectionFactory(redisConnectionFactory());

        // Key is always a plain String
        // Example key: "user:hdfc_123:behavioral_profile"
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value is a Java object serialized to JSON
        // Example value: { avgAmount: 8000, usualHours: [9,21] }
        template.setValueSerializer(
                new GenericJackson2JsonRedisSerializer()
        );
        template.setHashValueSerializer(
                new GenericJackson2JsonRedisSerializer()
        );

        template.afterPropertiesSet();
        return template;
    }

    // ─────────────────────────────────────────────────────────────────
    // BEAN 3: StringRedisTemplate — for simple String values
    // Used when storing plain text or JSON strings in Redis
    // ─────────────────────────────────────────────────────────────────
    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }
}