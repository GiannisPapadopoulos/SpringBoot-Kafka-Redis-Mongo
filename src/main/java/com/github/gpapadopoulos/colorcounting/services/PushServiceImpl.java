package com.github.gpapadopoulos.colorcounting.services;

import com.github.gpapadopoulos.colorcounting.kafka.KafkaBatchConsumer;
import com.github.gpapadopoulos.colorcounting.mongodb.model.ColorDocument;
import com.github.gpapadopoulos.colorcounting.mongodb.repo.ColorDocumentRepository;
import com.github.gpapadopoulos.colorcounting.redis.model.Color;
import com.github.gpapadopoulos.colorcounting.redis.repo.ColorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
        colorRepository.save(new Color(color));
        colorDocumentRepository.save(new ColorDocument(color));
        threadPool.submit(() -> colorDocumentRepository.save(new ColorDocument(color)));
    }

    @Override
    public void pushAll(Collection<String> colorMessages) {
        colorRepository.saveAll(colorMessages.stream().map(m -> new Color(m)).collect(Collectors.toList()));
        threadPool.submit(() -> {
            colorDocumentRepository.saveAll(
                    colorMessages.stream()
                            .map(m -> new ColorDocument(m))
                            .collect(Collectors.toList()));
        });
    }
}
