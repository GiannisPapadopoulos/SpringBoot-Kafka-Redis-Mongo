package com.github.gpapadopoulos.colorcounting.kafka;

import com.github.gpapadopoulos.colorcounting.services.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@Service
public class KafkaBatchConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaBatchConsumer.class);

    private CountDownLatch latch = new CountDownLatch(1);
    private final PushService pushService;

    public KafkaBatchConsumer(PushService pushService) {
        this.pushService = pushService;
    }

    @KafkaListener(topics = "${test.topic}")
    public void consumeBatchOfMessages(List<String> messages) {
        logger.info("Got messages : {}", messages);
        pushService.pushAll(messages);
        latch.countDown();
    }

    public void resetLatch(int count) {
        latch = new CountDownLatch(count);
    }

    public CountDownLatch getLatch() {
        return latch;
    }

}