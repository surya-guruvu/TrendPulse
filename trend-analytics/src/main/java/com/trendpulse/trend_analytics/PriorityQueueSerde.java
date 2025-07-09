package com.trendpulse.trend_analytics;

// import java.util.ArrayList;
// import java.util.Comparator;
// import java.util.List;
import java.util.PriorityQueue;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
// import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
// import org.springframework.kafka.support.serializer.JsonDeserializer;
// import org.springframework.kafka.support.serializer.JsonSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JavaType;

public class PriorityQueueSerde implements Serde<PriorityQueue<KeywordCount>> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Serializer<PriorityQueue<KeywordCount>> serializer() {
        return (topic, data) -> {
            try {
                return mapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new SerializationException("Failed to serialize PriorityQueue", e);
            }
        };
    }

    @Override
    public Deserializer<PriorityQueue<KeywordCount>> deserializer() {
        return (topic, bytes) -> {
            try {
                // JavaType for PriorityQueue<KeywordCount>
                JavaType type = mapper.getTypeFactory()
                        .constructCollectionType(PriorityQueue.class, KeywordCount.class);
                return mapper.readValue(bytes, type);
            } catch (Exception e) {
                throw new SerializationException("Failed to deserialize PriorityQueue", e);
            }
        };
    }
}


// public class PriorityQueueSerde implements Serde<PriorityQueue<KeywordCount>> {



//     private final Serde<List<KeywordCount>> listSerde = Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>());

//     @Override
//     public Serializer<PriorityQueue<KeywordCount>> serializer(){
//         return (topic, data) -> listSerde.serializer().serialize(topic, new ArrayList<>(data));
//     }

//     @Override
//     public Deserializer<PriorityQueue<KeywordCount>> deserializer(){
//         return (topic, bytes) -> {
//             List<KeywordCount> list = listSerde.deserializer().deserialize(topic, bytes);

//             PriorityQueue<KeywordCount> pq = new PriorityQueue<>(10,Comparator.comparingLong(kc -> kc.count()));

//             pq.addAll(list);
//             return pq;
//         };
//     }
// }
