package com.trendpulse.trend_analytics;

import java.io.Serializable;

/* Keyword + count wrapper */
record KeywordCount(long winStart, long winEnd, String key, long count) implements Serializable, Comparable<KeywordCount> { 
    @Override
    public int compareTo(KeywordCount other) {
        // ascending by count so PriorityQueue is a min‑heap
        return Long.compare(this.count, other.count);
    }
}
