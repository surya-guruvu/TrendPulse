package com.trendpulse.trend_analytics;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
// import org.springframework.kafka.support.serializer.JsonSerde;

import com.fasterxml.jackson.databind.ObjectMapper;

// public class KeywordCountSerde {
//     private static final JsonSerde<KeywordCount> INSTANCE = new JsonSerde<>(KeywordCount.class);

//     public static JsonSerde<KeywordCount> instance(){
//         return INSTANCE;
//     }
// }

public final class KeywordCountSerde implements Serde<KeywordCount> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Serializer<KeywordCount> serializer() {
        return (topic, data) -> {
            try { return mapper.writeValueAsBytes(data); }
            catch (Exception e) { throw new SerializationException(e); }
        };
    }

    @Override
    public Deserializer<KeywordCount> deserializer() {
        return (topic, bytes) -> {
            try { return mapper.readValue(bytes, KeywordCount.class); }
            catch (Exception e) { throw new SerializationException(e); }
        };
    }
}



