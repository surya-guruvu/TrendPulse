package com.trendpulse.alert_engine;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds alert‑threshold rules loaded from <code>rules.yml</code>.
 *
 * YAML structure expected:
 *
 * <pre>
 * rules:
 *   default:
 *     minSurge: 3.0
 *     minCount: 100
 *
 *   overrides:
 *     "#ai":
 *       minSurge: 2.5
 *       minCount: 50
 *     "#football":
 *       minSurge: 4.0
 *       minCount: 300
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "rules")
public class RuleConfig {

    // Immutable pair of minimum surge score and minimum count required to fire an alert.
    public record Rule(double minSurge, long minCount) { }

    // Default thresholds applied when no override exists.
    private Rule defaultRule = new Rule(3.0, 100);

    // Per‑hashtag overrides. Key must exactly match the tag (“#ai”).
    private Map<String, Rule> overrides = new HashMap<>();

    // -------------------- API --------------------

    // Return the Rule to apply for the given hashtag.
    // If no specific override exists, the defaultRule is returned.
    public Rule ruleFor(String hashtag) {
        return overrides.getOrDefault(hashtag, defaultRule);
    }

    public Rule getDefaultRule() {
        return defaultRule;
    }

    public void setDefaultRule(Rule defaultRule) {
        this.defaultRule = defaultRule;
    }

    public Map<String, Rule> getOverrides() {
        return Collections.unmodifiableMap(overrides);
    }

    public void setOverrides(Map<String, Rule> overrides) {
        this.overrides = overrides != null ? overrides : new HashMap<>();
    }

    @Override
    public String toString() {
        return "RuleConfig{" +
               "defaultRule=" + defaultRule +
               ", overrides=" + overrides +
               '}';
    }
}
