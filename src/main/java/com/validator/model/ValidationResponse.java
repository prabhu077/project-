package com.validator.model;

import java.util.List;

/**
 * Full response returned by the /api/validate endpoint and rendered on
 * the Field Scanner / Validation screens: every field's outcome, the
 * aggregate metrics, and the final publish/block decision (section 28).
 */
public record ValidationResponse(
        String fileName,
        List<FieldResult> fields,
        MetricsSummary metrics,
        boolean publicationAllowed
) {
}
