package com.github.gpapadopoulos.colorcounting;

import com.github.gpapadopoulos.colorcounting.kafka.config.KafkaProducerConsumerConfig;
import com.github.gpapadopoulos.colorcounting.mongodb.repo.ColorDocumentRepository;
import com.github.gpapadopoulos.colorcounting.redis.RedisCacheService;
import com.github.gpapadopoulos.colorcounting.redis.model.Color;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.annotation.EnableCaching;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.BDDAssertions.then;
import static org.awaitility.Awaitility.waitAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@Import({com.github.gpapadopoulos.colorcounting.ColorCountingApplicationTests.KafkaTestContainersConfiguration.class,
		com.github.gpapadopoulos.colorcounting.ColorCountingApplicationTests.MongoDbContainersConfiguration.class})
@SpringBootTest()
@DirtiesContext
@Testcontainers
@EnableCaching
class ColorCountingApplicationTests {

	private static Logger logger = LoggerFactory.getLogger(ColorCountingApplication.class);

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
	private ColorDocumentRepository colorDocumentRepository;

	@Autowired
	private AggregateStatisticsService statisticsService;

	@Autowired
	RedisCacheService redisCache;

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
		logger.info("Sending " + messages.size());
		messages.forEach(m -> template.send(topic, m));

		waitAtMost(20, TimeUnit.SECONDS).untilAsserted(() -> then(messages.size()).isEqualTo(redisCache.count()));

		waitAtMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			then(messages.size()).isEqualTo(colorDocumentRepository.count());
		});
		List<String> cacheColors = new ArrayList<>();
		redisCache.findAll().forEach(cacheColors::add);

		List<Color> mongoColors = colorDocumentRepository.findAll();

		assertEquals(messages.size(), cacheColors.size(), "Message and cache size should be equal");
		assertEquals(messages.size(), mongoColors.size(), "Cache and database size should be equal");
		assertEquals(messageCounts, statisticsService.getColorCounts(), "Incorrect color counts reported by the service");

		Map<String, Long> redisCounts = redisCache.findAll().stream().collect(
				Collectors.groupingBy(Function.identity(), Collectors.counting()));
		Map<String, Long> mongoCounts = colorDocumentRepository.findAll().stream().collect(
				Collectors.groupingBy(color -> color.getColor(), Collectors.counting()));

		assertEquals(messageCounts, redisCounts, "Incorrect color counts on the cache");
		assertEquals(messageCounts, mongoCounts, "Incorrect color counts on the database");
	}

	@DynamicPropertySource
	static void redisProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.redis.host", redis::getHost);
		registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
	}

	@TestConfiguration
	static class KafkaTestContainersConfiguration extends KafkaProducerConsumerConfig {

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
		public Collection<String> getMappingBasePackages() {
			return Collections.singleton("com.github.gpapadopoulos.colorcounting.mongodb");
		}
	}


}
