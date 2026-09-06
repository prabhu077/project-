package com.validator.tools;

import com.opencsv.CSVReader;
import com.validator.engine.DiscoveryEngine;
import com.validator.engine.MaskingEngine;
import com.validator.engine.PolicyEngine;
import com.validator.engine.ValidationEngine;

import java.io.FileReader;
import java.util.*;

/**
 * RunExperiment
 * -------------
 * End-to-end check of the full pipeline against real synthetic data,
 * printed to console. Run with:
 *   mvn compile exec:java -Dexec.mainClass="com.validator.tools.RunExperiment"
 */
public class RunExperiment {

    private static final String[] DATA_FILES = {
            "data/raw/store_data.csv",
            "data/raw/app_data.csv",
            "data/raw/loyalty_data.csv",
            "data/raw/supplier_data.csv"
    };

    private static final int SAMPLE_SIZE = 10;

    public static void main(String[] args) throws Exception {
        DiscoveryEngine discoveryEngine = new DiscoveryEngine();
        PolicyEngine policyEngine = new PolicyEngine();
        MaskingEngine maskingEngine = new MaskingEngine(policyEngine);
        ValidationEngine validationEngine = new ValidationEngine(policyEngine, maskingEngine);

        int totalFieldsScanned = 0;
        int totalSensitiveFields = 0;
        int totalPassed = 0;
        int totalFailed = 0;

        for (String filePath : DATA_FILES) {
            System.out.println("\n=========================================");
            System.out.println("FILE: " + filePath);
            System.out.println("=========================================");

            try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
                List<String[]> allRows = reader.readAll();
                if (allRows.isEmpty()) {
                    System.out.println("  (empty file, skipping)");
                    continue;
                }

                String[] header = allRows.get(0);
                List<String[]> dataRows = allRows.subList(1, allRows.size());

                Map<String, List<String>> columnSamples = new LinkedHashMap<>();
                for (int col = 0; col < header.length; col++) {
                    List<String> samples = new ArrayList<>();
                    for (int row = 0; row < Math.min(SAMPLE_SIZE, dataRows.size()); row++) {
                        samples.add(dataRows.get(row)[col]);
                    }
                    columnSamples.put(header[col], samples);
                }

                Map<String, String> fieldTypes = discoveryEngine.discoverFields(columnSamples);

                System.out.println("\nDetected field types:");
                for (Map.Entry<String, String> entry : fieldTypes.entrySet()) {
                    System.out.printf("  %-20s -> %s%n", entry.getKey(), entry.getValue());
                }

                int fileFieldsScanned = 0;
                int fileSensitiveFields = 0;
                int filePassed = 0;
                int fileFailed = 0;

                for (String[] row : dataRows) {
                    for (int col = 0; col < header.length; col++) {
                        String columnName = header[col];
                        String originalValue = row[col];
                        String fieldType = fieldTypes.get(columnName);

                        fileFieldsScanned++;
                        if (!"NON_SENSITIVE".equals(fieldType)) {
                            fileSensitiveFields++;
                        }

                        String maskedValue = maskingEngine.applyMasking(originalValue, fieldType);
                        boolean passed = validationEngine.validate(originalValue, maskedValue, fieldType);

                        if (passed) filePassed++; else fileFailed++;
                    }
                }

                System.out.printf("%nFields scanned:        %d%n", fileFieldsScanned);
                System.out.printf("Sensitive fields:      %d%n", fileSensitiveFields);
                System.out.printf("Validation passed:     %d%n", filePassed);
                System.out.printf("Validation failed:     %d%n", fileFailed);

                if (!dataRows.isEmpty()) {
                    System.out.println("\nExample masked row (row 1):");
                    String[] firstRow = dataRows.get(0);
                    for (int col = 0; col < header.length; col++) {
                        String original = firstRow[col];
                        String type = fieldTypes.get(header[col]);
                        String masked = maskingEngine.applyMasking(original, type);
                        System.out.printf("  %-20s [%s]  %s  ->  %s%n",
                                header[col], type, original, masked);
                    }
                }

                totalFieldsScanned += fileFieldsScanned;
                totalSensitiveFields += fileSensitiveFields;
                totalPassed += filePassed;
                totalFailed += fileFailed;

            } catch (Exception e) {
                System.out.println("  ERROR reading file: " + e.getMessage());
            }
        }

        System.out.println("\n=========================================");
        System.out.println("OVERALL SUMMARY");
        System.out.println("=========================================");
        System.out.printf("Total fields scanned:   %d%n", totalFieldsScanned);
        System.out.printf("Total sensitive fields: %d%n", totalSensitiveFields);
        System.out.printf("Total passed:           %d%n", totalPassed);
        System.out.printf("Total failed:           %d%n", totalFailed);
        double accuracy = totalFieldsScanned == 0 ? 0 : (100.0 * totalPassed / totalFieldsScanned);
        System.out.printf("Overall protection rate: %.2f%%%n", accuracy);
    }
}
