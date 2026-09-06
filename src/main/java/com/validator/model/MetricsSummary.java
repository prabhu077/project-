package com.validator.model;

/**
 * Aggregate metrics for one scan - feeds the dashboard/report screens (section 30).
 */
public record MetricsSummary(
        int fieldsScanned,
        int sensitiveFields,
        int passed,
        int failed
) {
    public double protectionRate() {
        if (fieldsScanned == 0) return 0.0;
        return (100.0 * passed) / fieldsScanned;
    }
}
