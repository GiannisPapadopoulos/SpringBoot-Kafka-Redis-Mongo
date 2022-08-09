package com.github.gpapadopoulos.colorcounting.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RedisCacheService {

    private static final String hashKey = "colors-cache";

    private static Logger logger = LoggerFactory.getLogger(RedisCacheService.class);

    private final StringRedisTemplate redisStringTemplate;


    public RedisCacheService(StringRedisTemplate redisStringTemplate) {
        this.redisStringTemplate = redisStringTemplate;
    }

    public void save(String color) {
        redisStringTemplate.opsForList().rightPush(hashKey, color);
    }

    public void saveAll(List<String> colors) {
        colors.stream().forEach(color -> redisStringTemplate.opsForList().rightPush(hashKey, color));
    }

    public long count() {
        return redisStringTemplate.opsForList().size(hashKey);
    }

    public List<String> findAll() {
        List<String> values = redisStringTemplate.opsForList().range(hashKey, 0, -1);
        return values;
    }

}
