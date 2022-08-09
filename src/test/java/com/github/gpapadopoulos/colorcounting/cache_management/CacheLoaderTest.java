package com.github.gpapadopoulos.colorcounting.cache_management;

import com.github.gpapadopoulos.colorcounting.ColorCountingApplication;
import com.github.gpapadopoulos.colorcounting.mongodb.repo.ColorDocumentRepository;
import com.github.gpapadopoulos.colorcounting.redis.RedisCacheService;
import com.github.gpapadopoulos.colorcounting.redis.model.Color;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.NestedTestConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {ColorCountingApplication.class, CacheLoaderTest.TestConfig.class}
)
@DirtiesContext
@Testcontainers
class CacheLoaderTest {

    @Container
    public static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:alpine")).withExposedPorts(6379);

    // @MockBean
    @Autowired
    private RedisCacheService redisCache;

    @Autowired
    private ColorDocumentRepository colorDocumentRepository;

    @Test
    void whenLoadingFromMongo_thenRedisCache_ShouldMatch() {
        List<String> databaseColors = colorDocumentRepository.findAll()
                .stream()
                .map(color -> color.getColor())
                .collect(Collectors.toList());

        Map<String, Long> redisCounts = redisCache.findAll().stream().collect(
                Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Map<String, Long> mongoCounts = colorDocumentRepository.findAll().stream().collect(
                Collectors.groupingBy(color -> color.getColor(), Collectors.counting()));

        assertEquals(mongoCounts, redisCounts, "Color counts in the database and cache should be equal");

    }

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.INHERIT)
    static class TestConfig {

        @Bean
        @Primary
        // Mocks mongoRepository to prefill it with some entries
        public ColorDocumentRepository service() {
            ColorDocumentRepository mongoRepository = Mockito.mock(ColorDocumentRepository.class);
            when(mongoRepository.findAll()).thenReturn(Arrays.asList(new Color("1", "red"), new Color("2", "blue")));
            return mongoRepository;
        }

    }

}