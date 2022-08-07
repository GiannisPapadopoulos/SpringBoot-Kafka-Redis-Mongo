package com.github.gpapadopoulos.colorcounting.kafka;

import com.github.gpapadopoulos.colorcounting.redis.model.Color;
import com.github.gpapadopoulos.colorcounting.redis.repo.ColorRepository;
import com.github.gpapadopoulos.colorcounting.redis.service.ColorRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class KafkaBatchConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaBatchConsumer.class);

    private CountDownLatch latch = new CountDownLatch(1);
    private List<String> allMessages = new ArrayList<>();

    private final ColorRepositoryService colorService;

    public KafkaBatchConsumer(ColorRepositoryService colorService) {
        this.colorService = colorService;
    }

    @KafkaListener(topics = "color-messages-test-topic")
    public void consumeBatchOfMessages(List<String> messages) {
        LOGGER.info("Got messages : {}", messages);
        allMessages.addAll(messages);

        // colorRepo.saveAll(colors);
        colorService.saveAll(messages);
        latch.countDown();
    }

    public void resetLatch(int count) {
        latch = new CountDownLatch(count);
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public List<String> getAllMessages() {
        return allMessages;
    }
}