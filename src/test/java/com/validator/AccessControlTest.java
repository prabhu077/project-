package com.validator;

import com.validator.rbac.AccessControlService;
import com.validator.rbac.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccessControlTest {

    private final AccessControlService accessControlService = new AccessControlService();

    @Test
    void adminCanViewRawEmail() {
        assertTrue(accessControlService.canViewRaw(Role.ADMIN, "EMAIL"));
    }

    @Test
    void dataAnalystCannotViewRawCard() {
        assertFalse(accessControlService.canViewRaw(Role.DATA_ANALYST, "CARD"));
    }

    @Test
    void noRoleCanViewRawPassword() {
        assertFalse(accessControlService.canViewRaw(Role.ADMIN, "PASSWORD"));
        assertFalse(accessControlService.canViewRaw(Role.AUDITOR, "PASSWORD"));
    }

    @Test
    void everyoneCanViewMaskedValues() {
        assertTrue(accessControlService.canViewMasked(Role.DATA_ANALYST, "EMAIL"));
        assertTrue(accessControlService.canViewMasked(Role.AUDITOR, "CARD"));
    }

    @Test
    void nonSensitiveFieldsAreViewableRawByAnyRole() {
        assertTrue(accessControlService.canViewRaw(Role.DATA_ANALYST, "NON_SENSITIVE"));
    }
}
