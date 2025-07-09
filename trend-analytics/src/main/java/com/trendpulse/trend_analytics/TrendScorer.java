package com.trendpulse.trend_analytics;

/** Very simple scoring: occurrences per second in the window. */
public final class TrendScorer {

    private TrendScorer() {}

    public static float computeScore(long count, long startMs, long endMs) {
        long seconds = (endMs - startMs) / 1_000;
        if (seconds == 0) return 0f;
        return count / (float) seconds;
    }
}
