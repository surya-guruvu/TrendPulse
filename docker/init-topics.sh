#!/usr/bin/env bash
set -e

# Initialize topics in the Kafka cluster
# Usage: ./init-topics.sh

# Check if the required environment variables are set

kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists \
    --topic post.raw --partitions 1 --replication-factor 1


kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists \
    --topic post.enriched --partitions 1 --replication-factor 1

kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists \
    --topic trend.score --partitions 1 --replication-factor 1

kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists \
    --topic trend.score.topN --partitions 1 --replication-factor 1

kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists \
    --topic alert.trend.spike --partitions 1 --replication-factor 1

kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists \
    --topic user.interest --partitions 1 --replication-factor 1



