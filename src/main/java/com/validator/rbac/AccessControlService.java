package com.validator.rbac;

import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * AccessControlService
 * ---------------------
 * Decides whether a given role may view the RAW (unmasked) value of a
 * sensitive field, based on the access table in section 17.
 *
 * Everyone can always see the MASKED value - this service only governs
 * raw-value visibility, which is far more restricted.
 */
@Service
public class AccessControlService {

    // Types each role is allowed to view in RAW form. Anything not listed
    // here is only ever visible in masked form to that role, regardless of value.
    private static final Map<Role, Set<String>> RAW_ACCESS = new EnumMap<>(Role.class);
    static {
        RAW_ACCESS.put(Role.ADMIN, Set.of("EMAIL", "PHONE", "CARD", "PAN", "GST", "AADHAAR"));
        RAW_ACCESS.put(Role.DATA_ANALYST, Set.of()); // sees masked values only
        RAW_ACCESS.put(Role.DATA_SCIENTIST, Set.of()); // sees masked values only
        RAW_ACCESS.put(Role.AUDITOR, Set.of()); // sees masked values only, for compliance review
    }

    /**
     * @return true if this role is allowed to see the raw, unmasked value for this field type.
     * PASSWORD is never returned as true for any role - it is a one-way hash, never viewable.
     */
    public boolean canViewRaw(Role role, String fieldType) {
        if ("PASSWORD".equals(fieldType)) {
            return false; // hashed values are never "raw" to anyone
        }
        if ("NON_SENSITIVE".equals(fieldType)) {
            return true;
        }
        return RAW_ACCESS.getOrDefault(role, Set.of()).contains(fieldType);
    }

    /** Every role can always view the masked/protected form of any field. */
    public boolean canViewMasked(Role role, String fieldType) {
        return true;
    }
}
