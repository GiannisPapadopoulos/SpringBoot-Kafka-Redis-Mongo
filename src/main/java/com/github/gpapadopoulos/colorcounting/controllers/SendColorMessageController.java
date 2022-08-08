package com.github.gpapadopoulos.colorcounting.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Provided for testing
@RestController
@RequestMapping(path = "/producer")
public class SendColorMessageController {

    private static final Logger logger = LoggerFactory.getLogger(SendColorMessageController.class);

    private final KafkaTemplate<String, String> kafkaTemplate;


    String status;

    @Value(value = "${test.topic}")
    private String kafkaTopicName;

    public SendColorMessageController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping(path = "/send-message")
    public ResponseEntity<String> sendMessage(@RequestBody String message) {
        logger.info("sending message {}", message);

        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(kafkaTopicName, message);

        future.addCallback(new ListenableFutureCallback<>() {

            @Override
            public void onSuccess(SendResult<String, String> result) {
                status = "Message sent successfully";
                logger.info("successfully sent message = {}, with offset = {}", message,
                        result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(Throwable ex) {
                logger.info("Failed to send message = {}, error = {}", message, ex.getMessage());
                status = "Message sending failed";
            }
        });
        return ResponseEntity.ok(status);
    }
}
