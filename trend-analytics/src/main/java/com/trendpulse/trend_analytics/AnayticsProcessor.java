package com.trendpulse.trend_analytics;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.EmitStrategy;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
// import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.trendpulse.avro.PostEnriched;
import com.trendpulse.avro.TrendScore;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;

import java.util.Set;


@Component
public class AnayticsProcessor {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${spring.kafka.properties.processing.guarantee}")
    private String processingGuarantee;

    @Value("${spring.kafka.properties.application.id}")
    private String applicationId;
    
    /** Build the Streams configuration */
    public Properties streamsConfig() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, processingGuarantee);
        return props;
    }

    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        /*Source */
        KStream<String,PostEnriched>  posts= builder.stream("post.enriched",
                Consumed.with(Serdes.String(), AvroSerdes.postEnriched()));

        KStream<String, String> keywords = posts.flatMapValues(p -> {
            Set<String> kwords = new HashSet<>();

            if(p.getHashtags() != null){
                p.getHashtags().stream().map(CharSequence::toString).map(String::toLowerCase).forEach(kwords::add);
            }
            if(p.getMentions() != null){
                p.getMentions().stream().map(CharSequence::toString).map(String::toLowerCase).forEach(kwords::add);
            }

            return kwords;
        });

        TimeWindows timeWindows = TimeWindows.ofSizeAndGrace(java.time.Duration.ofMinutes(5),java.time.Duration.ofSeconds(10));


        // --- Has Windowed count, but suppressed until window closes ----
        KTable<Windowed<String>, Long> counts = keywords
                .groupBy((key, kWord) -> kWord, Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(timeWindows)
                .emitStrategy(EmitStrategy.onWindowClose())
                .count(Materialized.with(Serdes.String(), Serdes.Long()));
                // .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()));

        KTable<Long, PriorityQueue<KeywordCount>> topNTable = counts.toStream()
                .map((windowedKey, count) -> {
                    long winStart = windowedKey.window().start();
                    long winEnd = windowedKey.window().end();
                    String key = windowedKey.key();
                    return KeyValue.pair(winStart, new KeywordCount(winStart,winEnd,key,count));
                })

                .groupByKey(Grouped.with(Serdes.Long(), new KeywordCountSerde()))

                .aggregate(
                    () -> new PriorityQueue<KeywordCount>(10,Comparator.comparingLong(kc -> kc.count())), //Intializer
                        
                        (key, value, heap)->{ //aggregator
                            heap.add(value);
                            if(heap.size() > 10){
                                heap.poll();
                            }

                            return heap;
                        },
                        Materialized.with(Serdes.Long(), new PriorityQueueSerde())
                );

        KStream<String, TrendScore> topNStream = topNTable.toStream()
                        .flatMap(
                            (start, heap) -> {
                                
                                return heap.stream().map(
                                    kc -> {
                                        long winStart = kc.winStart();
                                        long winEnd   = kc.winEnd();

                                        float surge = TrendScorer.computeScore(kc.count(), winStart, winEnd);

                                        TrendScore ts = TrendScore.newBuilder()
                                            .setHashtag(kc.key())
                                            .setCount((int)kc.count())
                                            .setWindowStart(winStart)
                                            .setWindowEnd(winEnd)
                                            .setSurgeScore(surge)
                                            .build();

                                        return KeyValue.pair(kc.key(), ts);
                                    }
                                ).toList();
                            }
                        );

        topNStream.to("trend.score.topN",Produced.with(Serdes.String(), AvroSerdes.trendScore()));


        KStream<String, TrendScore> trendScores = counts.toStream()
                .map((windowedKey, count) -> {
                    String keyword = windowedKey.key();
                    long windowStart = windowedKey.window().start();
                    long windowEnd   = windowedKey.window().end();
                    
                    float score = TrendScorer.computeScore(count, windowStart, windowEnd);

                    TrendScore trendScore = TrendScore.newBuilder()
                            .setHashtag(keyword)
                            .setCount(count.intValue())
                            .setWindowStart(windowStart)
                            .setWindowEnd(windowEnd)
                            .setSurgeScore(score)
                            .build();

                    return KeyValue.pair(keyword, trendScore);
                });

        trendScores.to("trend.score", 
                Produced.with(Serdes.String(), AvroSerdes.trendScore()));

        return builder.build();

    }

    



}
