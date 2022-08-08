package com.github.gpapadopoulos.colorcounting.kafka.testBeans;

import com.github.gpapadopoulos.colorcounting.kafka.KafkaBatchConsumer;
import com.github.gpapadopoulos.colorcounting.services.PushService;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class KafkaBatchConsumerWithLatch extends KafkaBatchConsumer {

    // Used to verify we processed messages
    private CountDownLatch latch = new CountDownLatch(1);

    public KafkaBatchConsumerWithLatch(PushService pushService) {
        super(pushService);
    }

    @Override
    public void consumeBatchOfMessages(List<String> messages) {
        super.consumeBatchOfMessages(messages);
        latch.countDown();
    }

    public void resetLatch(int count) {
        latch = new CountDownLatch(count);
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}
