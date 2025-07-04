package com.trendpulse.text_enricher;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.trendpulse.avro.PostCreated;
import com.trendpulse.avro.PostEnriched;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;


@Component
public class EnrichmentProcessor {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${spring.kafka.properties.processing.guarantee}")
    private String processingGuarantee;

    @Value("${spring.kafka.properties.application.id}")
    private String applicationId;


    /** Build the Streams configuration */
    public Properties streamsConfig(){
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, processingGuarantee);

        return props;
    }

    /** Enrichment Logic */
    private PostEnriched enrichRecord(PostCreated inPostCreated){
        Set<String> hashtags = RegexUtils.extract(inPostCreated.getText().toString(), RegexUtils.HASHTAG);
        Set<String> mentions = RegexUtils.extract(inPostCreated.getText().toString(), RegexUtils.MENTION);

        String language = LanguageDetectorStub.detectLanguage(inPostCreated.getText().toString());
        float sentimentScore = SentimentStub.getSentimentScore(inPostCreated.getText().toString());

        return PostEnriched.newBuilder()
                .setPostId(inPostCreated.getPostId())
                .setUserId(inPostCreated.getUserId())
                .setText(inPostCreated.getText())
                .setHashtags(new ArrayList<>(hashtags))
                .setMentions(new ArrayList<>(mentions))
                .setLanguage(language)
                .setSentimentScore(sentimentScore)
                .setTimestamp(inPostCreated.getTimestamp())
                .build();
    }

    public Topology buildTopology(){
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, PostCreated> inputStream = builder.stream("post.raw",Consumed.with(Serdes.String(), AvroSerdes.postCreated()));

        KStream<String, PostEnriched> enrichedStream = inputStream.mapValues(this::enrichRecord);

        enrichedStream.to("post.enriched", Produced.with(Serdes.String(), AvroSerdes.postEnriched()));

        return builder.build();
    }



}
