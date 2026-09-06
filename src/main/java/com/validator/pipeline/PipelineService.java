package com.validator.pipeline;

import com.validator.audit.AuditLogger;
import com.validator.engine.DiscoveryEngine;
import com.validator.engine.MaskingEngine;
import com.validator.engine.PolicyEngine;
import com.validator.engine.ValidationEngine;
import com.validator.model.FieldResult;
import com.validator.model.MetricsSummary;
import com.validator.model.ValidationResponse;
import com.validator.util.CsvLoader;
import com.validator.util.MetricsCalculator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PipelineService
 * ---------------
 * The single orchestrator that ties Discovery -> Masking -> Validation
 * together, and supports two modes:
 *   - "validator" (new pipeline): runs the full protection pipeline
 *   - "legacy": passes data through unchanged, no protection (for
 *     demonstrating coexistence with a legacy workflow, section 23)
 *
 * rollback() switches back to legacy mode - section 24's rollback demo.
 */
@Service
public class PipelineService {

    private final DiscoveryEngine discoveryEngine = new DiscoveryEngine();
    private final PolicyEngine policyEngine = new PolicyEngine();
    private final MaskingEngine maskingEngine = new MaskingEngine(policyEngine);
    private final ValidationEngine validationEngine = new ValidationEngine(policyEngine, maskingEngine);
    private final AuditLogger auditLogger;

    private volatile String mode = "validator"; // "validator" or "legacy"
    private volatile String version = "2.0";

    public PipelineService(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    public String getMode() {
        return mode;
    }

    public String getVersion() {
        return version;
    }

    /** Switches back to the legacy (no-protection) pipeline - section 24 rollback demo. */
    public void rollback() {
        String previous = mode;
        mode = "legacy";
        version = "1.0";
        auditLogger.logPipelineModeChange(previous, mode);
    }

    /** Re-activates the new validator pipeline after a rollback. */
    public void activateValidator() {
        String previous = mode;
        mode = "validator";
        version = "2.0";
        auditLogger.logPipelineModeChange(previous, mode);
    }

    /**
     * Runs the uploaded CSV through the pipeline (mode-dependent) and
     * returns a full ValidationResponse for the web layer to display.
     */
    public ValidationResponse process(String fileName, String[] header, List<String[]> rows) {
        Map<String, List<String>> columnSamples = CsvLoader.buildColumnSamples(header, rows);
        Map<String, String> fieldTypes = discoveryEngine.discoverFields(columnSamples);

        List<FieldResult> fieldResults = new ArrayList<>();
        List<Boolean> passFailResults = new ArrayList<>();
        int sensitiveFieldCount = 0;

        // Use the first data row as the representative example shown on screen.
        String[] exampleRow = rows.isEmpty() ? new String[header.length] : rows.get(0);

        for (int col = 0; col < header.length; col++) {
            String columnName = header[col];
            String fieldType = fieldTypes.getOrDefault(columnName, "NON_SENSITIVE");
            if (!"NON_SENSITIVE".equals(fieldType)) {
                sensitiveFieldCount++;
            }

            String originalExample = col < exampleRow.length ? exampleRow[col] : null;

            // LEGACY MODE: no masking is applied at all - values pass through unchanged.
            // VALIDATOR MODE: full masking + validation pipeline runs.
            String maskedExample = "legacy".equals(mode)
                    ? originalExample
                    : maskingEngine.applyMasking(originalExample, fieldType);

            boolean passed = "legacy".equals(mode)
                    ? "NON_SENSITIVE".equals(fieldType) // legacy mode "fails" anything sensitive by definition
                    : validationEngine.validate(originalExample, maskedExample, fieldType);

            fieldResults.add(new FieldResult(columnName, fieldType, maskedExample, passed));
            passFailResults.add(passed);
            auditLogger.logFieldResult(columnName, fieldType, passed);
        }

        MetricsSummary metrics = MetricsCalculator.calculate(passFailResults, sensitiveFieldCount);
        boolean publicationAllowed = metrics.failed() == 0;

        auditLogger.logScan(fileName, metrics.fieldsScanned(), metrics.sensitiveFields(),
                metrics.passed(), metrics.failed());

        return new ValidationResponse(fileName, fieldResults, metrics, publicationAllowed);
    }
}
