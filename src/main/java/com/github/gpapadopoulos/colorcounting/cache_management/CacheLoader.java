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
    private void init() {
        logger.info("Populating redis cache from backup");
        // colorRepository.deleteAll();
        colorRepository.saveAll(() -> colorDocumentRepository.findAll()
                .stream()
                .iterator());
        logger.info("Loaded redis cache");
    }

}
