package com.github.gpapadopoulos.colorcounting;

import com.github.gpapadopoulos.colorcounting.config.KafkaProducerConsumerConfig;
import com.github.gpapadopoulos.colorcounting.redis.model.Color;
import com.github.gpapadopoulos.colorcounting.redis.repo.ColorRepository;
import com.github.gpapadopoulos.colorcounting.services.AggregateStatisticsService;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.BDDAssertions.then;
import static org.awaitility.Awaitility.waitAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@Import({com.github.gpapadopoulos.colorcounting.ColorCountingApplicationTests.KafkaTestContainersConfiguration.class,
		com.github.gpapadopoulos.colorcounting.ColorCountingApplicationTests.MongoDbContainersConfiguration.class})
@SpringBootTest()
@DirtiesContext
@Testcontainers
class ColorCountingApplicationTests {

	@Container
	public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"));

	@Container
	public static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:alpine")).withExposedPorts(6379);

	@Container
	public static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

	@Autowired
	private KafkaTemplate<String, String> template;

	@Value("${test.topic}")
	private String topic;

	@Autowired
	private ColorRepository colorRepository;

	@Autowired
	private AggregateStatisticsService statisticsService;

	@Test
	void context_ShouldLoad() {
	}

	@Test
	void sendSomeMessages_ThenFrequenciesShouldBeCorrect() {
		Map<String, Long> messageCounts = Map.of(
				"red", 10L,
				"green", 5L,
				"blue", 20L,
				"white", 7L
		);
		List<String> messages = new ArrayList<>();
		messageCounts.forEach((color, count) -> messages.addAll(Collections.nCopies(Math.toIntExact(count), color)));
		messages.forEach(m -> template.send(topic, m));

		waitAtMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			then(messages.size()).isEqualTo(StreamSupport.stream(colorRepository.findAll().spliterator(), false)
					.collect(Collectors.toList()).size());
		});
		List<Color> cacheColors = StreamSupport.stream(colorRepository.findAll().spliterator(), false).collect(Collectors.toList());

		assertEquals(messages.size(), cacheColors.size());
		assertEquals(messageCounts, statisticsService.getColorCounts());
	}


	@DynamicPropertySource
	static void redisProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.redis.host", redis::getHost);
		registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
	}

	@TestConfiguration
	static class KafkaTestContainersConfiguration extends KafkaProducerConsumerConfig {

		private static final int maxRecords = 5;

		@Primary
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
			props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxRecords);
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

	@TestConfiguration
	@EnableMongoRepositories(basePackages = { "com.github.gpapadopoulos.colorcounting.mongodb.repo" })
	static class MongoDbContainersConfiguration extends AbstractMongoClientConfiguration {

		@Override
		protected String getDatabaseName() {
			return "test";
		}

		@Override
		public MongoClient mongoClient() {
			ConnectionString connectionString = new ConnectionString(mongoDBContainer.getConnectionString());
			MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
					.applyConnectionString(connectionString)
					.build();

			return MongoClients.create(mongoClientSettings);
		}

		@Override
		public Collection getMappingBasePackages() {
			return Collections.singleton("com.github.gpapadopoulos.colorcounting.mongodb");
		}
	}


}
