package com.example.database;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Migrations are applied by a separate run of the service (profile "migrate": Kubernetes Job,
 * init container or {@code ./gradlew :services:<service>:bootMigrate}), never by the running service,
 * which connects with its runtime role and only validates that its schema is up to date.
 */
@AutoConfiguration(before = FlywayAutoConfiguration.class)
@ConditionalOnClass(Flyway.class)
@EnableConfigurationProperties(DatabaseMigrationProperties.class)
public class DatabaseMigrationAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationAutoConfiguration.class);

    @ConditionalOnMissingBean
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy(DatabaseMigrationProperties properties) {
        return flyway -> {
            var schemas = String.join(",", flyway.getConfiguration().getSchemas());
            switch (properties.getMode()) {
                case MIGRATE -> {
                    var result = flyway.migrate();
                    var current = flyway.info().current();
                    logger.info("Schema {}: applied {} migration(s), now at version {}",
                            schemas, result.migrationsExecuted, current == null ? "-" : current.getVersion());
                }
                case VALIDATE -> {
                    try {
                        flyway.validate();
                    } catch (FlywayValidateException e) {
                        throw new IllegalStateException("Schema " + schemas + " does not match the migrations in this build."
                                + " Run the migrate job first (profile 'migrate', see database/README.md).", e);
                    }
                    logger.info("Schema {}: all migrations applied", schemas);
                }
            }
        };
    }
}
