package com.github.gpapadopoulos.colorcounting.services;

import com.github.gpapadopoulos.colorcounting.mongodb.repo.ColorDocumentRepository;
import com.github.gpapadopoulos.colorcounting.redis.model.Color;
import com.github.gpapadopoulos.colorcounting.redis.repo.ColorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class PushServiceImpl implements PushService {

    private static final Logger logger = LoggerFactory.getLogger(PushServiceImpl.class);

    private final ColorRepository colorRepository;
    private final ColorDocumentRepository colorDocumentRepository;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public PushServiceImpl(ColorRepository colorRepository, ColorDocumentRepository colorDocumentRepository) {
        this.colorRepository = colorRepository;
        this.colorDocumentRepository = colorDocumentRepository;
    }

    @Override
    public void push(String color) {
        logger.info("Pushing to cache");
        var cached = colorRepository.save(new Color(color));
        logger.info("Submitting task for writing to the database");
        threadPool.submit(() -> colorDocumentRepository.save(cached));
    }

    @Override
    public void pushAll(Collection<String> colorMessages) {
        logger.info("Pushing to cache");
        var cached = colorRepository.saveAll(colorMessages.stream().map(m -> new Color(m)).collect(Collectors.toList()));
        logger.info("Submitting task for writing to the database");
        threadPool.submit(() -> {
            colorDocumentRepository.saveAll(cached);
        });
    }
}
