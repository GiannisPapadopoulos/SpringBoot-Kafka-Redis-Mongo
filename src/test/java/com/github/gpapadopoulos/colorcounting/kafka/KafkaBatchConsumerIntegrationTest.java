package com.github.gpapadopoulos.colorcounting.kafka;

import com.github.gpapadopoulos.colorcounting.ColorCountingApplication;
import com.github.gpapadopoulos.colorcounting.kafka.config.KafkaProducerConsumerConfig;
import com.github.gpapadopoulos.colorcounting.cache_management.CacheLoader;
import com.github.gpapadopoulos.colorcounting.services.PushService;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.*;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.NestedTestConfiguration;
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
@DirtiesContext
@Testcontainers
@SpringBootTest(classes = {ColorCountingApplication.class, KafkaBatchConsumerIntegrationTest.KafkaTestContainersConfiguration.class })
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

    @MockBean
    private CacheLoader loader;

    @MockBean
    private PushService pushService;


    @BeforeEach
    void setUp() {
        consumer.resetLatch(1);
    }

    @Test
    void messagesAreConsumedInBatches() throws InterruptedException {
        var data = Arrays.asList("red", "green", "red");

        consumer.resetLatch(1);
        data.forEach(m -> template.send(topic, m));

        boolean messageConsumed = consumer.getLatch().await(20, TimeUnit.SECONDS);
        assertTrue(messageConsumed);
        // TODO How to test that the messages are actually consumed in batches?
    }

    @TestConfiguration
    @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
    static class KafkaTestContainersConfiguration extends KafkaProducerConsumerConfig {

        private static final int maxRecords = 2;

        @Primary
        @Bean
        public ConcurrentKafkaListenerContainerFactory<Integer, String> kafkaListenerContainerFactory() {
            ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory());
            factory.setBatchListener(true);
            return factory;
        }

        @Bean
        public Map<String, Object> consumerConfigs() {
            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "color-messages");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG , maxRecords);
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

        @Bean
        public ConsumerFactory<Integer, String> consumerFactory() {
            return new DefaultKafkaConsumerFactory<>(consumerConfigs());
        }


    }

}