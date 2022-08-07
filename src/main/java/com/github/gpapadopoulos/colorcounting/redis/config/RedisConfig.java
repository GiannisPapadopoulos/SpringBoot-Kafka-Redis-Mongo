package com.github.gpapadopoulos.colorcounting.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

@Configuration
@ComponentScan("com.github.gpapadopoulos.colorcounting.redis")
@EnableRedisRepositories(basePackages = "com.github.gpapadopoulos.colorcounting.redis.repo")
@PropertySource("classpath:application.properties")
public class RedisConfig {

    @Value("${spring.redis.port}")
    private int redisPort;

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory jedisConFactory  = new JedisConnectionFactory();
        // jedisConFactory.getStandaloneConfiguration().setHostName("localhost");
        jedisConFactory.getStandaloneConfiguration().setPort(redisPort);
        return jedisConFactory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        final RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setValueSerializer(new GenericToStringSerializer<Object>(Object.class));
        return template;
    }

    // @Bean
    // MessageListenerAdapter messageListener() {
    //     return new MessageListenerAdapter(new RedisMessageSubscriber());
    // }
    //
    // @Bean
    // RedisMessageListenerContainer redisContainer() {
    //     final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    //     container.setConnectionFactory(jedisConnectionFactory());
    //     container.addMessageListener(messageListener(), topic());
    //     return container;
    // }

    // @Bean
    // MessagePublisher redisPublisher() {
    //     return new RedisMessagePublisher(redisTemplate(), topic());
    // }
    //
    // @Bean
    // ChannelTopic topic() {
    //     return new ChannelTopic("pubsub:queue");
    // }
}
