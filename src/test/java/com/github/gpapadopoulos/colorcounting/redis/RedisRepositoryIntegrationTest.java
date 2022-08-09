package com.github.gpapadopoulos.colorcounting.redis;

import com.github.gpapadopoulos.colorcounting.ColorCountingApplication;
import com.github.gpapadopoulos.colorcounting.cache_management.CacheLoader;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ColorCountingApplication.class)
// @SpringBootTest()
@DirtiesContext
@Testcontainers
class RedisRepositoryIntegrationTest {

    @Container
    public static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:alpine")).withExposedPorts(6379); // .waitingFor(Wait.forHealthcheck())

    @Autowired
    private RedisCacheService redisCache;

    @MockBean
    private CacheLoader loader;

    @Test
    void savingAndRetrievingColor() {
        final String color = "red";
        redisCache.save(color);
        var allColors = redisCache.findAll();
        assertTrue("Color should be in cache", allColors.contains(color));

    }

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }
}
