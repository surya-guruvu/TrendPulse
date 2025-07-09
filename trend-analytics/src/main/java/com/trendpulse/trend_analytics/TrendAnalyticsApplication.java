package com.trendpulse.trend_analytics;

import java.time.Duration;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TrendAnalyticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrendAnalyticsApplication.class, args);
	}

	@Value("${top.n:10}")
	private int TOP_N;

	@Bean
	CommandLineRunner startStream(AnayticsProcessor anayticsProcessor) {
		return args -> {
			// Build the topology and start the stream processing
			Topology topology = anayticsProcessor.buildTopology();
			
			KafkaStreams streams = new KafkaStreams(topology, anayticsProcessor.streamsConfig());

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

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("Shutting down streams...");
				streams.close(Duration.ofSeconds(5));

				System.out.println("Kafka Streams closed.");
			}));
			
		};
	}

}
