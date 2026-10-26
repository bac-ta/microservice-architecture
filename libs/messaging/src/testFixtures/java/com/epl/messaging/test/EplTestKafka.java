package com.epl.messaging.test;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.kafka.KafkaContainer;

/**
 * One Kafka broker per test JVM for tests that exercise the outbox relay or listeners
 * (other tests run with the relay off, see EplTestDatabase).
 * <pre>{@code
 * @DynamicPropertySource
 * static void infrastructure(DynamicPropertyRegistry registry) {
 *     EplTestDatabase.register(registry, "inb");
 *     EplTestKafka.register(registry);
 * }
 * }</pre>
 */
public final class EplTestKafka {

    private static KafkaContainer kafka;

    private EplTestKafka() {
    }

    public static synchronized KafkaContainer kafka() {
        if (kafka == null) {
            kafka = new KafkaContainer("apache/kafka:3.8.0");
            kafka.start();
        }
        return kafka;
    }

    /** Points spring.kafka at the test broker, turns the relay and listeners on. */
    public static void register(DynamicPropertyRegistry registry) {
        var container = kafka();
        registry.add("spring.kafka.bootstrap-servers", container::getBootstrapServers);
        registry.add("spring.kafka.listener.auto-startup", () -> "true");
        registry.add("epl.messaging.relay.enabled", () -> "true");
        registry.add("epl.messaging.relay.poll-interval", () -> "100ms");
    }
}
