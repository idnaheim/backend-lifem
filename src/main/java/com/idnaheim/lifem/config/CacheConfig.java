package com.idnaheim.lifem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${cache.expenses.ttl-minutes:10}")
    private long expensesTtlMinutes;

    @Value("${cache.incomes.ttl-minutes:10}")
    private long incomesTtlMinutes;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        GenericJacksonJsonRedisSerializer jsonSerializer = GenericJacksonJsonRedisSerializer.builder()
                .enableUnsafeDefaultTyping()
                .build();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                "expenses",        defaultConfig.entryTtl(Duration.ofMinutes(expensesTtlMinutes)),
                "expenseById",     defaultConfig.entryTtl(Duration.ofMinutes(expensesTtlMinutes)),
                "expenseRunRate",  defaultConfig.entryTtl(Duration.ofMinutes(expensesTtlMinutes)),
                "incomes",         defaultConfig.entryTtl(Duration.ofMinutes(incomesTtlMinutes)),
                "activeIncomes",   defaultConfig.entryTtl(Duration.ofMinutes(incomesTtlMinutes)),
                "incomeById",      defaultConfig.entryTtl(Duration.ofMinutes(incomesTtlMinutes)),
                "incomeRunRate",   defaultConfig.entryTtl(Duration.ofMinutes(incomesTtlMinutes))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
