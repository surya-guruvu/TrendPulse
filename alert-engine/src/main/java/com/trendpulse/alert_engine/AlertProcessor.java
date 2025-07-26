package com.trendpulse.alert_engine;

import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import com.trendpulse.alert_engine.RuleConfig.Rule;
import com.trendpulse.avro.TrendAlert;
import com.trendpulse.avro.TrendScore;
import com.trendpulse.avro.UserInterest;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;

@Component
public class AlertProcessor {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${spring.kafka.properties.processing.guarantee}")
    private String processingGuarantee;

    @Value("${spring.kafka.properties.application.id}")
    private String applicationId;

    @Autowired
    private RuleConfig ruleConfig;


    public Properties streamsConfig() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, processingGuarantee);
        return props;
    }

    public Topology buildTopology(){
        StreamsBuilder builder = new StreamsBuilder();

        // Step 1A:  Source: top‑N trending hashtags
        KStream<String, TrendScore> trendTopN = builder
            .stream("trend.score.topN", Consumed.with(Serdes.String(), AvroSerdes.trendScore()))
            .peek((key, value) -> System.out.println("[trendTopN] key=" + key + ", value=" + value));

        // Step 1B:  Source: User Interests
        KTable<String, UserInterest> userInterests = builder
            .table("user.interest", Consumed.with(Serdes.String(), AvroSerdes.userInterest()));

        // Print userInterests as they are updated
        userInterests
            .toStream()
            .peek((key, value) -> System.out.println("[userInterests] key=" + key + ", value=" + value));

        // Step 2A: explode interests into tag → userId pairs
        KStream<String, String> tagUserPairs = userInterests
            .toStream()
            .flatMap((userId, uI) -> {
                return uI.getHashtags().stream()
                    .map(CharSequence::toString)
                    .map(tag -> KeyValue.pair(tag.toString(), userId.toString()))
                    .toList();
            })
            .peek((key, value) -> System.out.println("[tagUserPairs] tag=" + key + ", userId=" + value));

        // Step 2B: aggregate into Set<userId> per tag
        KTable<String, TagFollowers> tagFollowers  = tagUserPairs
            .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
            .aggregate(
                TagFollowers::new,                        // new empty wrapper
                (tag, user, agg) -> agg.add(user),        // add
                Materialized.with(Serdes.String(), new JsonSerde<>(TagFollowers.class))
            );

        // Print tagFollowers as they are updated
        tagFollowers
            .toStream()
            .peek((key, value) -> System.out.println("[tagFollowers] tag=" + key + ", followers=" + value));
            

        KStream<String, TrendAlert> alerts = trendTopN
            .join(tagFollowers,
                //   (tag, trend) -> tag,                // map key for join
                  (trend, followers) -> makeAlerts(trend, followers))  // returns List<TrendAlert>
            // .flatMapValues(list ->list)
                  .flatMap((key,value)-> value.stream().map(v -> KeyValue.pair(v.getUserId().toString(), v)).toList());

        alerts.to("alert.trend.spike", Produced.with(Serdes.String(), AvroSerdes.trendAlertSerde()));

        return builder.build();
    }

    /* Build alert list for a single TrendScore + follower set */
    private List<TrendAlert> makeAlerts(TrendScore ts, TagFollowers followers) {
        System.out.println("makeAlerts called with:");
        System.out.println("  TrendScore: " + ts);
        System.out.println("  TagFollowers: " + followers.getUserIds());

        if (followers == null || followers.getUserIds().isEmpty()) return List.of();

        // Rule rule = ruleConfig.ruleFor(ts.getHashtag().toString());
        // if (ts.getSurgeScore() < rule.minSurge() || ts.getCount() < rule.minCount())
        //     return List.of();

        List<TrendAlert> alerts = followers.getUserIds().stream()
                .map(uId -> TrendAlert.newBuilder()
                    .setUserId(uId)
                    .setHashtag(ts.getHashtag().toString())
                    .setSurgeScore(ts.getSurgeScore())
                    .setWindowStart(ts.getWindowStart())
                    .setWindowEnd(ts.getWindowEnd())
                    .setCount(ts.getCount())
                    .setMsg("🔥 " + ts.getHashtag() + " spiked to " + ts.getCount())
                    .build())
                .toList();

        System.out.println("Generated alerts: " + alerts);

        return alerts;
    }
    
}