package com.github.gpapadopoulos.colorcounting.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Service
public class KafkaBatchConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaBatchConsumer.class);

    private CountDownLatch latch = new CountDownLatch(1);
    private List<String> allMessages = new ArrayList<>();

    @KafkaListener(topics = "color-messages-test-topic")
    public void consumeBatchOfMessages(List<String> messages) {
        LOGGER.info("Got messages : {}", messages);
        allMessages.addAll(messages);
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