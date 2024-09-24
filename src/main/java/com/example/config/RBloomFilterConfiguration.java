package com.example.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Nruonan
 * @description
 */
@Configuration
public class RBloomFilterConfiguration {
    /**
     * 优惠券查询缓存穿透布隆过滤器
     */
    @Bean
    public RBloomFilter<String> couponTemplateQueryBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("ticketTemplateQueryBloomFilter");
        bloomFilter.tryInit(640L, 0.001);
        return bloomFilter;
    }
}
