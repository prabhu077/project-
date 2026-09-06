package com.validator;

import com.validator.engine.MaskingEngine;
import com.validator.engine.PolicyEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaskingEngineTest {

    private MaskingEngine maskingEngine;

    @BeforeEach
    void setUp() {
        maskingEngine = new MaskingEngine(new PolicyEngine());
    }

    @Test
    void masksEmailKeepingFirstCharAndDomain() {
        assertEquals("p****@gmail.com", maskingEngine.applyMasking("prabhu@gmail.com", "EMAIL"));
    }

    @Test
    void masksPhoneKeepingLastFourDigits() {
        assertEquals("******3210", maskingEngine.applyMasking("9876543210", "PHONE"));
    }

    @Test
    void masksCardKeepingLastFourDigits() {
        assertEquals("************9012", maskingEngine.applyMasking("4532123456789012", "CARD"));
    }

    @Test
    void masksPasswordAsSaltedHashNotAsterisks() {
        String masked = maskingEngine.applyMasking("MySecret123", "PASSWORD");
        assertNotEquals("MySecret123", masked);
        assertFalse(masked.contains("*"));
        assertTrue(masked.contains(":"));
    }

    @Test
    void passwordHashIsDifferentEachTimeDueToRandomSalt() {
        String masked1 = maskingEngine.applyMasking("MySecret123", "PASSWORD");
        String masked2 = maskingEngine.applyMasking("MySecret123", "PASSWORD");
        assertNotEquals(masked1, masked2);
    }

    @Test
    void nonSensitiveValuesAreLeftUnchanged() {
        assertEquals("2500", maskingEngine.applyMasking("2500", "NON_SENSITIVE"));
    }

    @Test
    void nullValueReturnsNull() {
        assertNull(maskingEngine.applyMasking(null, "EMAIL"));
    }
}
