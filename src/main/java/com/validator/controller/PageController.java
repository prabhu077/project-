package com.validator.controller;

import com.validator.pipeline.PipelineService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * PageController
 * --------------
 * Serves the server-rendered Thymeleaf screens (section 26).
 * No business logic here - just passes data to templates.
 */
@Controller
public class PageController {

    private final PipelineService pipelineService;

    public PageController(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @GetMapping("/")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/field-scanner")
    public String fieldScanner() {
        return "field-scanner";
    }

    @GetMapping("/pipeline-management")
    public String pipelineManagement(Model model) {
        model.addAttribute("mode", pipelineService.getMode());
        model.addAttribute("version", pipelineService.getVersion());
        return "pipeline-management";
    }
}
