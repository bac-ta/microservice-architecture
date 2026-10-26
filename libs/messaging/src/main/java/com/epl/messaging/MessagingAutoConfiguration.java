package com.epl.messaging;

import com.epl.database.DatabasePersistenceAutoConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.backoff.FixedBackOff;

import javax.sql.DataSource;

/**
 * Outbox writer, outbox relay, idempotent inbox and the listeners' error handling (retry, then
 * {@code <topic>.DLT}). Kafka connection settings are the usual {@code spring.kafka.*}.
 */
@AutoConfiguration(after = {KafkaAutoConfiguration.class, JacksonAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class, DatabasePersistenceAutoConfiguration.class})
@ConditionalOnClass(KafkaTemplate.class)
@EnableConfigurationProperties(MessagingProperties.class)
public class MessagingAutoConfiguration {

    @Bean
    public Outbox outbox(DataSource dataSource, ObjectProvider<ObjectMapper> objectMapper, MessagingProperties properties) {
        return new Outbox(new JdbcTemplate(dataSource), objectMapper.getIfAvailable(ObjectMapper::new), properties);
    }

    @Bean
    public Inbox inbox(DataSource dataSource, PlatformTransactionManager transactionManager) {
        return new Inbox(new JdbcTemplate(dataSource), new TransactionTemplate(transactionManager));
    }

    @ConditionalOnProperty(prefix = "epl.messaging.relay", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    public OutboxRelay outboxRelay(DataSource dataSource, PlatformTransactionManager transactionManager,
                                   KafkaTemplate<String, String> kafkaTemplate, MessagingProperties properties) {
        return new OutboxRelay(new JdbcTemplate(dataSource), new TransactionTemplate(transactionManager), kafkaTemplate, properties);
    }

    /** Picked up by Spring Boot's listener container factory. */
    @ConditionalOnMissingBean(CommonErrorHandler.class)
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaOperations<Object, Object> kafkaOperations, MessagingProperties properties) {
        var consumer = properties.consumer();
        var handler = new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaOperations),
                new FixedBackOff(consumer.backOff().toMillis(), consumer.retries()));
        // a payload that cannot be read will not get better on redelivery
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        return handler;
    }
}
