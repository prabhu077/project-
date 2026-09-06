package com.validator.engine;

/**
 * ValidationEngine
 * ----------------
 * Checks whether a value that is supposed to be protected was ACTUALLY
 * protected correctly before it's allowed into the analytics system.
 */
public class ValidationEngine {

    private final PolicyEngine policyEngine;
    private final MaskingEngine maskingEngine;

    public ValidationEngine() {
        this.policyEngine = new PolicyEngine();
        this.maskingEngine = new MaskingEngine(this.policyEngine);
    }

    public ValidationEngine(PolicyEngine policyEngine, MaskingEngine maskingEngine) {
        this.policyEngine = policyEngine;
        this.maskingEngine = maskingEngine;
    }

    public boolean validate(String originalValue, String actualValue, String fieldType) {
        if (originalValue == null) {
            return true; // nothing sensitive to protect
        }
        if ("NON_SENSITIVE".equals(fieldType)) {
            return true;
        }
        if ("PASSWORD".equals(fieldType)) {
            return validatePassword(originalValue, actualValue);
        }
        return validateDeterministicMasking(originalValue, actualValue, fieldType);
    }

    private boolean validateDeterministicMasking(String originalValue, String actualValue, String fieldType) {
        String expectedMasked = maskingEngine.applyMasking(originalValue, fieldType);
        return expectedMasked != null && expectedMasked.equals(actualValue);
    }

    private boolean validatePassword(String originalValue, String actualValue) {
        if (actualValue == null) {
            return false;
        }
        if (actualValue.equals(originalValue)) {
            return false; // plaintext leaked through unmasked
        }
        return PolicyEngine.verifyPassword(originalValue, actualValue);
    }

    public record ValidationResult(String columnName, String fieldType, boolean passed) {
    }

    public ValidationResult validateField(String columnName, String originalValue, String actualValue, String fieldType) {
        boolean passed = validate(originalValue, actualValue, fieldType);
        return new ValidationResult(columnName, fieldType, passed);
    }
}
