package com.github.gpapadopoulos.colorcounting.services;

import com.github.gpapadopoulos.colorcounting.mongodb.repo.ColorDocumentRepository;
import com.github.gpapadopoulos.colorcounting.redis.RedisCacheService;
import com.github.gpapadopoulos.colorcounting.redis.model.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class PushServiceImpl implements PushService {

    private static final Logger logger = LoggerFactory.getLogger(PushServiceImpl.class);

    private final RedisCacheService redisCache;

    private final ColorDocumentRepository colorDocumentRepository;

    private final ExecutorService mongoPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final ExecutorService redisPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public PushServiceImpl( RedisCacheService redisCache, ColorDocumentRepository colorDocumentRepository) {
        this.redisCache = redisCache;
        this.colorDocumentRepository = colorDocumentRepository;
    }

    @Override
    public void push(String color) {
        redisCache.save(color);
        mongoPool.submit(() -> colorDocumentRepository.save(new Color(color)));
    }

    @Override
    public void pushAll(List<String> colorMessages) {
        Iterable<Color> batch = colorMessages.stream().map(m -> new Color(m)).collect(Collectors.toList());
        logger.info("Pushing to cache");
        long start = System.currentTimeMillis();
        redisCache.saveAll(colorMessages);
        // redisPool.submit(() -> {
        //     redisCache.saveAll(colorMessages);
        // });
        long elapsedRedis = System.currentTimeMillis() - start;
        logger.info("Pushing to cache time: " + (elapsedRedis / 1000.0) + "s");
        mongoPool.submit(() -> {
            colorDocumentRepository.saveAll(batch);
        });
    }
}
