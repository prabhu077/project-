package com.validator.engine;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * PolicyEngine
 * ------------
 * Defines HOW each sensitive type should be protected.
 * Each policy is a pure function: original value -> protected value.
 *
 * This engine only decides the RULE. It does not apply masking itself
 * (that's MaskingEngine's job) and does not check correctness
 * (that's ValidationEngine's job).
 *
 * Covers exactly 7 sensitive types: EMAIL, PHONE, CARD, PAN, GST, AADHAAR (masked)
 * and PASSWORD (one-way encrypted via salted SHA-256 hash, never masked with asterisks).
 */
public class PolicyEngine {

    private static final Map<String, Function<String, String>> POLICIES = new LinkedHashMap<>();

    static {
        // EMAIL: keep first char + domain -> p****@gmail.com
        POLICIES.put("EMAIL", value -> {
            int at = value.indexOf('@');
            if (at <= 0) return maskFully(value); // malformed, fall back to full mask
            String domain = value.substring(at); // includes '@'
            return value.charAt(0) + "****" + domain;
        });

        // PHONE: keep last 4 digits -> ******3210
        POLICIES.put("PHONE", value -> maskKeepLast(value, 4));

        // CARD: keep last 4 digits -> ************9012
        POLICIES.put("CARD", value -> maskKeepLast(value, 4));

        // PAN: keep last 4 chars -> ******1234F
        POLICIES.put("PAN", value -> maskKeepLast(value, 4));

        // GST: keep last 4 chars
        POLICIES.put("GST", value -> maskKeepLast(value, 4));

        // AADHAAR: keep last 4 digits -> **** **** 1234
        POLICIES.put("AADHAAR", value -> maskKeepLast(value.replace(" ", ""), 4));

        // PASSWORD: never mask with asterisks (that's reversible-looking and misleading).
        // Instead, apply a one-way salted SHA-256 hash. The original value is
        // never recoverable from the stored/analytics copy - this is encryption-grade
        // protection, not cosmetic masking, appropriate for credential fields.
        POLICIES.put("PASSWORD", PolicyEngine::hashPassword);

        // NON_SENSITIVE: no masking applied
        POLICIES.put("NON_SENSITIVE", value -> value);
    }

    /**
     * Returns the masking function registered for a given sensitive type.
     * Falls back to full masking if the type is unknown (safer default -
     * better to over-mask an unrecognized type than leak it).
     */
    public Function<String, String> getPolicy(String fieldType) {
        return POLICIES.getOrDefault(fieldType, PolicyEngine::maskFully);
    }

    /** Returns true if a masking policy exists for this type (other than the safe fallback). */
    public boolean hasPolicy(String fieldType) {
        return POLICIES.containsKey(fieldType);
    }

    // ---------- shared masking helpers ----------

    /** Replaces every character with '*'. */
    private static String maskFully(String value) {
        if (value == null) return null;
        return "*".repeat(value.length());
    }

    /** Masks all but the last n characters. If value is too short, masks fully. */
    private static String maskKeepLast(String value, int n) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.length() <= n) {
            return maskFully(trimmed);
        }
        int maskLength = trimmed.length() - n;
        return "*".repeat(maskLength) + trimmed.substring(maskLength);
    }

    // ---------- password encryption (hashing) ----------

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH_BYTES = 16;

    /**
     * Hashes a plaintext password using SHA-256 with a random salt.
     * Output format: "<base64-salt>:<base64-hash>" so the salt travels
     * with the hash and can be used later to verify (but never reverse) the value.
     *
     * This is one-way: there is no method to recover the original password
     * from the output. That is intentional - passwords must never be
     * reversible, unlike email/phone/card which use partial masking.
     */
    public static String hashPassword(String plainValue) {
        if (plainValue == null) return null;

        try {
            byte[] salt = new byte[SALT_LENGTH_BYTES];
            new SecureRandom().nextBytes(salt);

            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(salt);
            byte[] hashedBytes = digest.digest(plainValue.getBytes(StandardCharsets.UTF_8));

            String saltB64 = Base64.getEncoder().encodeToString(salt);
            String hashB64 = Base64.getEncoder().encodeToString(hashedBytes);

            return saltB64 + ":" + hashB64;
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed available on every standard JVM, so this
            // should never happen - but fail safe rather than leak plaintext.
            throw new IllegalStateException("Password hashing algorithm unavailable", e);
        }
    }

    /**
     * Verifies a plaintext value against a previously hashed "<salt>:<hash>" string.
     * Used by ValidationEngine to confirm a password field was correctly
     * encrypted, without ever needing to store or compare raw plaintext.
     */
    public static boolean verifyPassword(String plainValue, String hashedValue) {
        if (plainValue == null || hashedValue == null || !hashedValue.contains(":")) {
            return false;
        }
        try {
            String[] parts = hashedValue.split(":", 2);
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            String expectedHashB64 = parts[1];

            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(salt);
            byte[] actualHashBytes = digest.digest(plainValue.getBytes(StandardCharsets.UTF_8));
            String actualHashB64 = Base64.getEncoder().encodeToString(actualHashBytes);

            return MessageDigest.isEqual(
                    actualHashB64.getBytes(StandardCharsets.UTF_8),
                    expectedHashB64.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            return false;
        }
    }
}
