# TrendPulse - Real-time Trend Detection Platform

**TrendPulse** is a distributed, real-time analytics system built with **Spring Boot**, **Apache Kafka**, and **Kafka Streams**, designed to ingest, enrich, and analyze user-generated social media posts at scale.

It demonstrates a modular microservices architecture that leverages:

- ✅ **Post Ingestion** with validation, deduplication, and Avro schema enforcement  
- 🧠 **Text Enrichment** including hashtag/mention extraction, language detection, and sentiment analysis  
- 📈 **Trend Analytics** to compute trending topics in real-time using Kafka windowing  
- 📡 **Real-time Push Notifications** for hot/trending keywords  
- 🔍 **Query APIs** and **WebSocket-based real-time feeds**  
- 📊 **Monitoring** with Prometheus + Grafana  
- 🛡️ **Auth-ready setup** with Keycloak stubbed for OAuth2  
