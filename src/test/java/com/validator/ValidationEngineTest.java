package com.validator;

import com.validator.engine.MaskingEngine;
import com.validator.engine.PolicyEngine;
import com.validator.engine.ValidationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationEngineTest {

    private ValidationEngine validationEngine;
    private MaskingEngine maskingEngine;

    @BeforeEach
    void setUp() {
        PolicyEngine policyEngine = new PolicyEngine();
        maskingEngine = new MaskingEngine(policyEngine);
        validationEngine = new ValidationEngine(policyEngine, maskingEngine);
    }

    @Test
    void passesWhenEmailIsCorrectlyMasked() {
        String original = "prabhu@gmail.com";
        String actual = maskingEngine.applyMasking(original, "EMAIL");
        assertTrue(validationEngine.validate(original, actual, "EMAIL"));
    }

    @Test
    void passesWhenPasswordIsCorrectlyHashed() {
        String original = "MySecret123";
        String actual = maskingEngine.applyMasking(original, "PASSWORD");
        assertTrue(validationEngine.validate(original, actual, "PASSWORD"));
    }

    @Test
    void failsWhenSensitiveFieldIsCompletelyUnmasked() {
        String original = "4532123456789012";
        assertFalse(validationEngine.validate(original, original, "CARD"));
    }

    @Test
    void failsWhenPasswordIsLeftAsPlaintext() {
        String original = "MySecret123";
        assertFalse(validationEngine.validate(original, original, "PASSWORD"));
    }

    @Test
    void passesForNonSensitiveField() {
        assertTrue(validationEngine.validate("2500", "2500", "NON_SENSITIVE"));
    }

    @Test
    void passesWhenOriginalValueIsNull() {
        assertTrue(validationEngine.validate(null, null, "EMAIL"));
    }
}
