package com.trendpulse.text_enricher;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TextEnricherApplication {

	public static void main(String[] args) {
		SpringApplication.run(TextEnricherApplication.class, args);
	}

	@Bean
	CommandLineRunner startStream(EnrichmentProcessor enrichmentProcessor) {
		return args ->{
			Topology topology = enrichmentProcessor.buildTopology();
			KafkaStreams streams = new KafkaStreams(topology, enrichmentProcessor.streamsConfig());

			streams.setUncaughtExceptionHandler(exception -> {
				System.err.println("Uncaught exception in stream thread:");
				exception.printStackTrace();
				System.exit(1); // Let Kubernetes restart the pod
				return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_APPLICATION;
			});

			streams.start();

		};
	}

}
