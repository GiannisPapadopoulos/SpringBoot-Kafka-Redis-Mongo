package com.github.gpapadopoulos.colorcounting.kafka;

import com.github.gpapadopoulos.colorcounting.ColorCountingApplication;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
@RunWith(SpringRunner.class)
@Import(KafkaBatchConsumerIntegrationTest.KafkaTestContainersConfiguration.class)
@SpringBootTest(classes = ColorCountingApplication.class)
@DirtiesContext
@Testcontainers
class KafkaBatchConsumerIntegrationTest {

    @Container
    public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"));

    @Autowired
    public KafkaTemplate<String, String> template;

    @Autowired
    private KafkaBatchConsumer consumer;

    @Autowired
    private KafkaSimpleProducer producer;

    @Value("${test.topic}")
    private String topic;

    @BeforeEach
    void setUp() {
        consumer.resetLatch(1);
    }

    @Test
    void messagesAreConsumedInBatches() throws InterruptedException {
        var data = Arrays.asList("Message1", "message2", "message3", "m4", "m5", "m6");

        data.forEach(m -> template.send(topic, m));

        boolean messageConsumed = consumer.getLatch().await(10, TimeUnit.SECONDS);
        assertTrue(messageConsumed);
        assertTrue(!consumer.getAllMessages().isEmpty(), "Should have received at least one message");
        // TODO Not a very good test
        assertTrue(consumer.getAllMessages().size() <= KafkaTestContainersConfiguration.batchSize,
                   String.format("Should have received at most %d messages, instead got %d",
                                 KafkaTestContainersConfiguration.batchSize,
                                 consumer.getAllMessages().size()));

        // assertEquals(data, consumer.getAllMessages());
    }

    @TestConfiguration
    static class KafkaTestContainersConfiguration {

        private static final int batchSize = 5;

        @Bean
        public ConcurrentKafkaListenerContainerFactory<Integer, String> kafkaListenerContainerFactory() {
            ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory());
            factory.setBatchListener(true);
            // factory.getContainerProperties().setBatchErrorHandler(new BatchLoggingErrorHandler());
            return factory;
        }

        @Bean
        public ConsumerFactory<Integer, String> consumerFactory() {
            return new DefaultKafkaConsumerFactory<>(consumerConfigs());
        }

        @Bean
        public Map<String, Object> consumerConfigs() {
            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "color-messages");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

            // props.put(ConsumerConfig.GROUP_ID_CONFIG, "batch");
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, batchSize);
            return props;
        }

        @Bean
        public ProducerFactory<String, String> producerFactory() {
            Map<String, Object> configProps = new HashMap<>();
            configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            return new DefaultKafkaProducerFactory<>(configProps);
        }

        @Bean
        public KafkaTemplate<String, String> kafkaTemplate() {
            return new KafkaTemplate<>(producerFactory());
        }

    }
}