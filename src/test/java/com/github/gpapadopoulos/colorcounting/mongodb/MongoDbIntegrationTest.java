package com.github.gpapadopoulos.colorcounting.mongodb;

import com.github.gpapadopoulos.colorcounting.ColorCountingApplication;
import com.github.gpapadopoulos.colorcounting.mongodb.repo.ColorDocumentRepository;
import com.github.gpapadopoulos.colorcounting.cache_management.CacheLoader;
import com.github.gpapadopoulos.colorcounting.redis.model.Color;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ColorCountingApplication.class)
@Import(MongoDbIntegrationTest.MongoDbContainersConfiguration.class)
@DirtiesContext
@Testcontainers
class MongoDbIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(com.github.gpapadopoulos.colorcounting.mongodb.MongoDbIntegrationTest.class);

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @Autowired
    private ColorDocumentRepository colorDocumentRepository;

    @MockBean
    private CacheLoader loader;

    @Test
    void savingAndRetrievingColor() {
        final Color color = new Color("62ef93c3fc6f9f0cf9c6ea2e", "red");
        colorDocumentRepository.save(color);
        Color retrievedColor = colorDocumentRepository.findById(color.getId()).get();
        assertEquals(color.getId(), retrievedColor.getId());
        assertEquals(color.getColor(), retrievedColor.getColor());
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
