package com.github.gpapadopoulos.colorcounting.services;

import com.github.gpapadopoulos.colorcounting.redis.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AggregateStatisticsServiceImpl implements  AggregateStatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(PushServiceImpl.class);

    private final RedisCacheService redisCache;

    public AggregateStatisticsServiceImpl(RedisCacheService redisCache) {
        this.redisCache = redisCache;
    }

    @Override
    public Map<String, Long> getColorCounts() {
        ArrayList<String> repoColors = new ArrayList<>();
        redisCache.findAll().forEach(repoColors::add);

        Map<String, Long> frequencyMap = repoColors.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        logger.info("Returning counts: " + frequencyMap );
        return frequencyMap;
    }
}
