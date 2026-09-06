package com.validator.model;

/**
 * Result for a single column after running the full pipeline on one sample row:
 * what type it was detected as, its masked value, and whether validation passed.
 */
public record FieldResult(
        String columnName,
        String fieldType,
        String maskedValue,
        boolean passed
) {
}
