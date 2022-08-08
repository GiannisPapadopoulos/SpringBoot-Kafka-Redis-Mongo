package com.github.gpapadopoulos.colorcounting.cache_management;

import com.github.gpapadopoulos.colorcounting.mongodb.repo.ColorDocumentRepository;
import com.github.gpapadopoulos.colorcounting.redis.model.Color;
import com.github.gpapadopoulos.colorcounting.redis.repo.ColorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class CacheLoader {

    private static Logger logger = LoggerFactory.getLogger(CacheLoader.class);

    private final ColorRepository colorRepository;
    private final ColorDocumentRepository colorDocumentRepository;

    public CacheLoader(ColorRepository colorRepository, ColorDocumentRepository colorDocumentRepository) {
        this.colorRepository = colorRepository;
        this.colorDocumentRepository = colorDocumentRepository;
    }

    @PostConstruct
    public void init() {
        logger.info("Populating redis cache from backup");
        logger.warn("before " + StreamSupport.stream(colorRepository.findAll().spliterator(), false)
                .collect(Collectors.toList()).size());
        // colorRepository.deleteAll();
        logger.warn("then " + StreamSupport.stream(colorRepository.findAll().spliterator(), false)
                .collect(Collectors.toList()).size());
        colorRepository.saveAll(() -> colorDocumentRepository.findAll().stream().map(colorDocument -> new Color(colorDocument.getId(), colorDocument.getColor())).iterator());
        var all = colorDocumentRepository.findAll();
        logger.warn("Found " + colorDocumentRepository.findAll().size());
        logger.warn("Added " + StreamSupport.stream(colorRepository.findAll().spliterator(), false)
                .collect(Collectors.toList()).size());

        logger.warn("mongo " + StreamSupport.stream(colorDocumentRepository.findAll().spliterator(), false)
                .collect(Collectors.toList()));
        logger.warn("redis " + StreamSupport.stream(colorRepository.findAll().spliterator(), false)
                .collect(Collectors.toList()));
    }

    @PreDestroy
    private void shutdown() {
        System.out.println("Shutdown All Resources");
    }



}
