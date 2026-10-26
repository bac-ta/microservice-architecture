package com.epl.database;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;

import java.util.Map;
import java.util.Optional;

/**
 * JPA defaults for EPL services: the transaction manager that sets the RLS context and
 * auditing of created_by / updated_by.
 */
@AutoConfiguration(before = HibernateJpaAutoConfiguration.class)
@ConditionalOnClass(EntityManagerFactory.class)
@EnableJpaAuditing(auditorAwareRef = "eplAuditorAware")
public class DatabasePersistenceAutoConfiguration {

    /** No request context (batch, tests): no session settings, RLS then matches nothing. */
    @ConditionalOnMissingBean
    @Bean
    public DatabaseSessionSettings databaseSessionSettings() {
        return Map::of;
    }

    @ConditionalOnMissingBean(name = "eplAuditorAware")
    @Bean
    public AuditorAware<String> eplAuditorAware() {
        return () -> Optional.of("system");
    }

    @ConditionalOnMissingBean(TransactionManager.class)
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory,
                                                         DatabaseSessionSettings settings,
                                                         ObjectProvider<TransactionManagerCustomizers> customizers) {
        var transactionManager = new SessionSettingsJpaTransactionManager(entityManagerFactory, settings);
        customizers.ifAvailable(c -> c.customize((TransactionManager) transactionManager));
        return transactionManager;
    }
}
