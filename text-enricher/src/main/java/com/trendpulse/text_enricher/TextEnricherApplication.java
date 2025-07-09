package com.trendpulse.text_enricher;

import java.time.Duration;

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

				// Close the streams gracefully, Kubernetes will restart the pod
				streams.close(Duration.ofSeconds(5));
				System.exit(1); // Let Kubernetes restart the pod

				// (return never reached, but required by the handler API)
				return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_APPLICATION;
			});

			streams.start();

			// Register a JVM shutdown hook for graceful close, runs when JVM is shutting down
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("Shutting down streams...");
				streams.close(Duration.ofSeconds(5));

				System.out.println("Kafka Streams closed.");
			}));


		};
	}

}
