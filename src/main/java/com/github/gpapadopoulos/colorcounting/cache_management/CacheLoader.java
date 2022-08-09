package com.github.gpapadopoulos.colorcounting.cache_management;

import com.github.gpapadopoulos.colorcounting.mongodb.repo.ColorDocumentRepository;
import com.github.gpapadopoulos.colorcounting.redis.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.stream.Collectors;

@Component
public class CacheLoader {

    private static Logger logger = LoggerFactory.getLogger(CacheLoader.class);

    private final RedisCacheService redisCache;

    private final ColorDocumentRepository colorDocumentRepository;

    public CacheLoader(RedisCacheService redisCache, ColorDocumentRepository colorDocumentRepository) {
        this.redisCache = redisCache;
        this.colorDocumentRepository = colorDocumentRepository;
    }

    @PostConstruct
    private void init() {
        logger.info("Populating redis cache from backup");
        redisCache.saveAll(colorDocumentRepository.findAll().stream()
                .map(color -> color.getColor())
                .collect(Collectors.toList())
        );
        logger.info("Loaded redis cache");
    }

}
