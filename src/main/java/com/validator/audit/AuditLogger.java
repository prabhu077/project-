package com.validator.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AuditLogger
 * -----------
 * Logs pipeline events for compliance review. CRITICAL RULE: never log
 * raw sensitive values here - only metadata (file name, field type, pass/fail).
 */
@Component
public class AuditLogger {

    private static final Logger log = LoggerFactory.getLogger("AUDIT");

    public void logScan(String fileName, int fieldsScanned, int sensitiveFields, int passed, int failed) {
        log.info("SCAN file={} fieldsScanned={} sensitiveFields={} passed={} failed={}",
                fileName, fieldsScanned, sensitiveFields, passed, failed);
    }

    public void logFieldResult(String columnName, String fieldType, boolean passed) {
        log.info("FIELD column={} type={} result={}", columnName, fieldType, passed ? "PASS" : "FAIL");
    }

    public void logPipelineModeChange(String previousMode, String newMode) {
        log.info("PIPELINE_MODE_CHANGE from={} to={}", previousMode, newMode);
    }
}
