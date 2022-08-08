package com.github.gpapadopoulos.colorcounting.mongodb.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
// import com.mongodb.reactivestreams.client.MongoClient;
// import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Collection;
import java.util.Collections;

@Configuration
@EnableMongoRepositories(basePackages = { "com.github.gpapadopoulos.colorcounting.mongodb.repo" })
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "test";
    }

    @Override
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017/test");
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
