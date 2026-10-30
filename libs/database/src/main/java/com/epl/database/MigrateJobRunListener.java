package com.epl.database;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Migrate job (profile "migrate": Kubernetes Job or {@code ./gradlew :services:<service>:bootMigrate}):
 * applies the service's Flyway migrations as the schema owner ({@code spring.flyway.*}) as soon as the
 * configuration is read, then exits with 0 (or 1 on failure). The application context is never created,
 * so the job needs neither the runtime role's password nor Kafka.
 */
public class MigrateJobRunListener implements SpringApplicationRunListener, Ordered {

    public static final String PROFILE = "migrate";

    private static final Logger logger = LoggerFactory.getLogger(MigrateJobRunListener.class);

    public MigrateJobRunListener(SpringApplication application, String[] args) {
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        if (!environment.matchesProfiles(PROFILE)) {
            return;
        }
        int exitCode = 0;
        try {
            migrate(environment);
        } catch (RuntimeException e) {
            logger.error("Migration failed", e);
            exitCode = 1;
        }
        System.exit(exitCode);
    }

    private static void migrate(ConfigurableEnvironment environment) {
        var binder = Binder.get(environment);
        var properties = binder.bind("spring.flyway", FlywayProperties.class).orElseGet(FlywayProperties::new);
        var url = properties.getUrl() != null
                ? properties.getUrl()
                : binder.bind("spring.datasource.url", String.class)
                        .orElseThrow(() -> new IllegalStateException("No spring.flyway.url or spring.datasource.url"));

        var flyway = Flyway.configure()
                .dataSource(url, properties.getUser(), properties.getPassword())
                .schemas(properties.getSchemas().toArray(String[]::new))
                .defaultSchema(properties.getDefaultSchema())
                .createSchemas(properties.isCreateSchemas())
                .locations(properties.getLocations().toArray(String[]::new))
                .validateMigrationNaming(properties.isValidateMigrationNaming())
                .connectRetries(properties.getConnectRetries())
                .load();
        var result = flyway.migrate();
        var current = flyway.info().current();
        logger.info("Schema {}: applied {} migration(s), now at version {}", String.join(",", properties.getSchemas()),
                result.migrationsExecuted, current == null ? "-" : current.getVersion());
    }

    /** After EventPublishingRunListener, so that logging is configured. */
    @Override
    public int getOrder() {
        return 10;
    }
}
