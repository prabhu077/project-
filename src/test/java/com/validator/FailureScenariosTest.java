package com.validator;

import com.validator.engine.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FailureScenariosTest
 * ---------------------
 * Covers the realistic failure states from section 22 of the problem statement.
 */
class FailureScenariosTest {

    private DiscoveryEngine discoveryEngine;
    private MaskingEngine maskingEngine;
    private ValidationEngine validationEngine;

    @BeforeEach
    void setUp() {
        discoveryEngine = new DiscoveryEngine();
        PolicyEngine policyEngine = new PolicyEngine();
        maskingEngine = new MaskingEngine(policyEngine);
        validationEngine = new ValidationEngine(policyEngine, maskingEngine);
    }

    @Test
    void failure1_sensitiveFieldCompletelyUnmasked() {
        String card = "4532123456789012";
        assertFalse(validationEngine.validate(card, card, "CARD"));
    }

    @Test
    void failure2_unknownFieldNameButValueRevealsType() {
        // Column name "contact_info" gives no direct keyword match, but the value is clearly an email.
        String type = discoveryEngine.discoverFieldType("contact_info", List.of("prabhu@gmail.com"));
        assertEquals("EMAIL", type);
    }

    @Test
    void failure3_incorrectMasking() {
        String phone = "9876543210";
        String wronglyMasked = "****3210"; // should be 6 stars, not 4
        assertFalse(validationEngine.validate(phone, wronglyMasked, "PHONE"));
    }

    @Test
    void failure4_passwordLeftAsPlaintextIsBlocked() {
        String password = "MySecret123";
        assertFalse(validationEngine.validate(password, password, "PASSWORD"));
    }

    @Test
    void failure5_nullSensitiveFieldHandledSafely() {
        assertTrue(validationEngine.validate(null, null, "EMAIL"));
    }

    @Test
    void failure6_newlyAddedSensitiveColumnIsDiscovered() {
        // Simulates a new "gstin" column appearing that wasn't in the original schema.
        String type = discoveryEngine.discoverFieldType("gstin", List.of("22ABCDE1234F1Z5"));
        assertEquals("GST", type);
    }
}
