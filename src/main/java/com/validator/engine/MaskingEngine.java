package com.validator.engine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * MaskingEngine
 * -------------
 * Applies masking to a value once its sensitive type is known.
 * Does not decide the type (DiscoveryEngine) or the rule (PolicyEngine) - just applies it.
 */
public class MaskingEngine {

    private final PolicyEngine policyEngine;

    public MaskingEngine() {
        this.policyEngine = new PolicyEngine();
    }

    public MaskingEngine(PolicyEngine policyEngine) {
        this.policyEngine = policyEngine;
    }

    public String applyMasking(String value, String fieldType) {
        if (value == null) {
            return null;
        }
        Function<String, String> policy = policyEngine.getPolicy(fieldType);
        return policy.apply(value);
    }

    public List<String> applyMaskingToColumn(List<String> columnValues, String fieldType) {
        return columnValues.stream()
                .map(value -> applyMasking(value, fieldType))
                .toList();
    }

    public Map<String, String> applyMaskingToRow(Map<String, String> row, Map<String, String> fieldTypes) {
        Map<String, String> maskedRow = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : row.entrySet()) {
            String columnName = entry.getKey();
            String originalValue = entry.getValue();
            String fieldType = fieldTypes.getOrDefault(columnName, "NON_SENSITIVE");
            maskedRow.put(columnName, applyMasking(originalValue, fieldType));
        }
        return maskedRow;
    }
}
