package com.fraudshield.fraudshield.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    // ─────────────────────────────────────────────────────────────────
    // PRODUCER SIDE — how FraudShield SENDS events to Kafka
    // ─────────────────────────────────────────────────────────────────
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Where is Kafka running?
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers);

        // Key is a String (senderId like "user_123")
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);

        // Value is a String (JSON like "{amount:80000,...}")
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);

        // Retry 3 times if send fails
        config.put(ProducerConfig.RETRIES_CONFIG, 3);

        // Wait 1 second between retries
        config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);

        // Batch small messages together for efficiency
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ─────────────────────────────────────────────────────────────────
    // CONSUMER SIDE — how FraudShield RECEIVES events from Kafka
    // ─────────────────────────────────────────────────────────────────
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers);

        // Which consumer group this consumer belongs to
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // How to convert bytes back to String for key
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);

        // How to convert bytes back to String for value
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);

        // If no offset found — read from very beginning
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest");

        // Commit offset automatically after processing
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        // Commit every 1 second
        config.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,
                "1000");

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        // 3 threads = process 3 partitions in parallel
        factory.setConcurrency(3);

        return factory;
    }
}