package com.validator.util;

import com.validator.model.MetricsSummary;

import java.util.List;

public class MetricsCalculator {

    public static MetricsSummary calculate(List<Boolean> results, int sensitiveFieldCount) {
        int passed = (int) results.stream().filter(Boolean::booleanValue).count();
        int failed = results.size() - passed;
        return new MetricsSummary(results.size(), sensitiveFieldCount, passed, failed);
    }
}
