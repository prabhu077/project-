package com.validator;

import com.validator.engine.DiscoveryEngine;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiscoveryEngineTest {

    private final DiscoveryEngine engine = new DiscoveryEngine();

    @Test
    void detectsEmailByColumnName() {
        assertEquals("EMAIL", engine.detectByName("contact_email"));
    }

    @Test
    void detectsPhoneByColumnName() {
        assertEquals("PHONE", engine.detectByName("mobile_number"));
    }

    @Test
    void detectsPasswordByColumnName() {
        assertEquals("PASSWORD", engine.detectByName("user_password"));
    }

    @Test
    void returnsNullWhenNameHasNoKeywordMatch() {
        assertNull(engine.detectByName("purchase_amount"));
    }

    @Test
    void detectsEmailByValuePattern() {
        assertEquals("EMAIL", engine.detectByPattern(List.of("a@gmail.com", "b@yahoo.com")));
    }

    @Test
    void detectsPhoneByValuePattern() {
        assertEquals("PHONE", engine.detectByPattern(List.of("9876543210", "9000012345")));
    }

    @Test
    void detectsCardByValuePattern() {
        assertEquals("CARD", engine.detectByPattern(List.of("4111111111111111")));
    }

    @Test
    void detectsPanByValuePattern() {
        assertEquals("PAN", engine.detectByPattern(List.of("ABCDE1234F")));
    }

    @Test
    void detectsGstByValuePattern() {
        assertEquals("GST", engine.detectByPattern(List.of("22ABCDE1234F1Z5")));
    }

    @Test
    void detectsAadhaarByValuePattern() {
        assertEquals("AADHAAR", engine.detectByPattern(List.of("123456789012")));
    }

    @Test
    void patternWinsWhenNameIsVagueButValueIsClear() {
        assertEquals("EMAIL", engine.discoverFieldType("contact", List.of("prabhu@gmail.com")));
    }

    @Test
    void returnsNonSensitiveWhenNothingMatches() {
        assertEquals("NON_SENSITIVE", engine.discoverFieldType("purchase_amount", List.of("2500")));
    }

    @Test
    void discoversMultipleFieldsInADataset() {
        var columnSamples = new java.util.LinkedHashMap<String, List<String>>();
        columnSamples.put("customer_email", List.of("a@gmail.com"));
        columnSamples.put("phone", List.of("9876543210"));
        var results = engine.discoverFields(columnSamples);
        assertEquals("EMAIL", results.get("customer_email"));
        assertEquals("PHONE", results.get("phone"));
    }
}
