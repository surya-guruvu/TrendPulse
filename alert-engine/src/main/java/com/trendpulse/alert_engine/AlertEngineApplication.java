package com.trendpulse.alert_engine;

import java.time.Duration;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AlertEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlertEngineApplication.class, args);
	}

	@Bean 
	CommandLineRunner startStream(AlertProcessor alertProcessor){
		return args -> {
			Topology topology = alertProcessor.buildTopology();

			KafkaStreams streams = new KafkaStreams(topology, alertProcessor.streamsConfig());

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
