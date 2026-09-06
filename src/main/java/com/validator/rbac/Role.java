package com.validator.rbac;

/**
 * The four roles from section 17 of the problem statement.
 * Each has different visibility rights over sensitive fields.
 */
public enum Role {
    ADMIN,
    DATA_ANALYST,
    DATA_SCIENTIST,
    AUDITOR
}
