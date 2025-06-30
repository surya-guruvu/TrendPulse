package com.trendpulse.post_ingest;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import com.trendpulse.avro.PostCreated;

import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    // Configuration for Kafka Producer
    // This class can be used to set up Kafka producer properties such as bootstrap servers, key/value serializers, etc.
    // For example:
    // public static final String BOOTSTRAP_SERVERS = "localhost:9092";
    // public static final String KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    // public static final String VALUE_SERIALIZER = "io.confluent.kafka.serializers.KafkaAvroSerializer";
    
    // Add methods to create and configure KafkaProducer instances if needed

    @Bean
    public ProducerFactory<String, PostCreated> producerFactory(KafkaProperties kafkaProperties) {
        // Create and return a ProducerFactory for PostCreated messages
        Map<String, Object> configProps = kafkaProperties.buildProducerProperties();

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, PostCreated> kafkaTemplate(ProducerFactory<String, PostCreated> factory) {
        // Create and return a KafkaTemplate for sending PostCreated messages
        return new KafkaTemplate<>(factory);
    }
    
}
