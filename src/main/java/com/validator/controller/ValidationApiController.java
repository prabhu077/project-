package com.validator.controller;

import com.validator.model.ValidationResponse;
import com.validator.pipeline.PipelineService;
import com.validator.util.CsvLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * ValidationApiController
 * ------------------------
 * Handles CSV upload from the Field Scanner page (section 10, 28: API stub).
 *
 *   POST /api/validate  -> pure JSON API (section 28)
 *   POST /scan          -> same logic, but renders the field-scanner.html
 *                          page with results (used by the upload form)
 */
@Controller
public class ValidationApiController {

    private final PipelineService pipelineService;

    public ValidationApiController(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @PostMapping("/api/validate")
    @ResponseBody
    public ValidationResponse validateApi(@RequestParam("file") MultipartFile file) throws Exception {
        return runPipeline(file);
    }

    @PostMapping("/scan")
    public String scanAndRenderPage(@RequestParam("file") MultipartFile file, Model model) {
        try {
            ValidationResponse response = runPipeline(file);
            model.addAttribute("result", response);
        } catch (Exception e) {
            model.addAttribute("error", "Could not process file: " + e.getMessage());
        }
        return "field-scanner";
    }

    private ValidationResponse runPipeline(MultipartFile file) throws Exception {
        CsvLoader.ParsedCsv parsed = CsvLoader.parse(file);
        return pipelineService.process(file.getOriginalFilename(), parsed.header(), parsed.rows());
    }
}
