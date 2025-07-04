package com.trendpulse.text_enricher;


import com.trendpulse.avro.PostCreated;
import com.trendpulse.avro.PostEnriched;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import java.util.HashMap;
import java.util.Map;

public class AvroSerdes {

    private static final Map<String, String> serdeConfig = new HashMap<>();

    static {
        // Let Kafka Streams register the schema automatically
        serdeConfig.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, "true");
        // Set the schema registry URL
        // NOTE: This should match the schema registry URL defined in application.yml
        serdeConfig.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
    
        
        // NOTE: Do NOT put schema.registry.url here if already defined in application.yml
    }

    public static SpecificAvroSerde<PostCreated> postCreated() {
        SpecificAvroSerde<PostCreated> serde = new SpecificAvroSerde<>();
        serde.configure(serdeConfig, false); // false = not key, Only for value serialization
        // If you want to use this serde for keys, set the second parameter to true
        // serde.configure(serdeConfig, true); // true = key serde
        // If you want to use this serde for both keys and values, you can call configure twice:
        // serde.configure(serdeConfig, true); // for key
        // serde.configure(serdeConfig, false); // for value
        return serde;
    }

    public static SpecificAvroSerde<PostEnriched> postEnriched() {
        SpecificAvroSerde<PostEnriched> serde = new SpecificAvroSerde<>();
        serde.configure(serdeConfig, false);
        return serde;
    }
}

