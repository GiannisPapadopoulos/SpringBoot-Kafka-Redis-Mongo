package com.github.gpapadopoulos.colorcounting.redis;

import com.github.gpapadopoulos.colorcounting.ColorCountingApplication;
import com.github.gpapadopoulos.colorcounting.cache_management.CacheLoader;
import com.github.gpapadopoulos.colorcounting.redis.model.Color;
import com.github.gpapadopoulos.colorcounting.redis.repo.ColorRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ColorCountingApplication.class)
// @SpringBootTest()
@DirtiesContext
@Testcontainers
class RedisRepositoryIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(RedisRepositoryIntegrationTest.class);

    @Container
    public static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:alpine")).withExposedPorts(6379); // .waitingFor(Wait.forHealthcheck())

    @Autowired
    private ColorRepository colorRepository;

    @MockBean
    private CacheLoader loader;

    @Test
    void savingAndRetrievingColor() {
        final Color color = new Color("07c6850e-ae0a-4aa9-b4c8-3b06a0ea47fd", "red");
        colorRepository.save(color);
        Color retrievedColor = colorRepository.findById(color.getId()).get();
        assertEquals(color.getId(), retrievedColor.getId());
        assertEquals(color.getColor(), retrievedColor.getColor());
    }

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        logger.info("Override properties to connect to Testcontainers:");
        logger.info("* Test-Container 'Redis': spring.redis.host = {} ; spring.redis.port = {}",
                redis.getHost(), redis.getMappedPort(6379));

        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }
}
