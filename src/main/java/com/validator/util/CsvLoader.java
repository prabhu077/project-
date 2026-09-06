package com.validator.util;

import com.opencsv.CSVReader;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CsvLoader
 * ---------
 * Reads an uploaded CSV file into a header row + data rows, and builds
 * the column -> sample-values map that DiscoveryEngine needs.
 */
public class CsvLoader {

    private static final int SAMPLE_SIZE = 10;

    public record ParsedCsv(String[] header, List<String[]> rows) {
    }

    public static ParsedCsv parse(MultipartFile file) throws Exception {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String[]> all = reader.readAll();
            if (all.isEmpty()) {
                return new ParsedCsv(new String[0], List.of());
            }
            String[] header = all.get(0);
            List<String[]> rows = all.size() > 1 ? all.subList(1, all.size()) : List.of();
            return new ParsedCsv(header, rows);
        }
    }

    /** Builds column name -> first N sample values, for DiscoveryEngine's pattern detection. */
    public static Map<String, List<String>> buildColumnSamples(String[] header, List<String[]> rows) {
        Map<String, List<String>> samples = new LinkedHashMap<>();
        for (int col = 0; col < header.length; col++) {
            List<String> values = new ArrayList<>();
            for (int row = 0; row < Math.min(SAMPLE_SIZE, rows.size()); row++) {
                values.add(rows.get(row)[col]);
            }
            samples.put(header[col], values);
        }
        return samples;
    }
}
