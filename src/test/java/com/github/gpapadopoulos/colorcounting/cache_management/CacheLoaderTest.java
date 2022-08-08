package com.github.gpapadopoulos.colorcounting.cache_management;

import com.github.gpapadopoulos.colorcounting.ColorCountingApplication;
import com.github.gpapadopoulos.colorcounting.mongodb.repo.ColorDocumentRepository;
import com.github.gpapadopoulos.colorcounting.redis.model.Color;
import com.github.gpapadopoulos.colorcounting.redis.repo.ColorRepository;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    private ColorRepository colorRepository;

    @Autowired
    private ColorDocumentRepository colorDocumentRepository;

    @Test
    void whenLoadingFromMongo_thenRedisCache_ShouldMatch() {
        Set<String> database = colorDocumentRepository.findAll()
                .stream()
                .map(color -> color.getId())
                .collect(Collectors.toSet());

        Set<String> cache = StreamSupport.stream(colorRepository.findAll().spliterator(), false)
                .map(color -> color.getId())
                .collect(Collectors.toSet());

        assertEquals(database, cache, "Database and cache ids should match");
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