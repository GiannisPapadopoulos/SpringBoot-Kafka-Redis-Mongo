package com.github.gpapadopoulos.colorcounting.redis.service;

import com.github.gpapadopoulos.colorcounting.kafka.KafkaBatchConsumer;
import com.github.gpapadopoulos.colorcounting.mongodb.model.ColorDocument;
import com.github.gpapadopoulos.colorcounting.mongodb.repo.ColorDocumentRepository;
import com.github.gpapadopoulos.colorcounting.redis.model.Color;
import com.github.gpapadopoulos.colorcounting.redis.repo.ColorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ColorRepositoryService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaBatchConsumer.class);

    private final ColorRepository colorRepository;

    private final ColorDocumentRepository colorDocumentRepository;

    public ColorRepositoryService(ColorRepository colorRepository, ColorDocumentRepository colorDocumentRepository) {
        this.colorRepository = colorRepository;
        this.colorDocumentRepository = colorDocumentRepository;
    }

    public Color saveColor(Color color) {
        return colorRepository.save(color);
    }

    public Color getColor(String id) {
        return colorRepository.findById(id).orElse(null);
    }

    public Iterable<Color> findAll() {
        logger.info("cols " + colorRepository.findAll());
        logger.info("mongo " + colorDocumentRepository.findAll());
        return colorRepository.findAll();
    }

    public void saveAll(List<String> colorMessages) {
        colorRepository.saveAll(colorMessages.stream().map(m -> new Color(m)).collect(Collectors.toList()));
        colorDocumentRepository.saveAll(colorMessages.stream().map(m -> new ColorDocument(m)).collect(Collectors.toList()));
    }

    // other methods
}