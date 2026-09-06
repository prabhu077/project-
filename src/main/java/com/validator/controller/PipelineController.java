package com.validator.controller;

import com.validator.pipeline.PipelineService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * PipelineController
 * -------------------
 * Rollback / legacy-mode demo endpoints (sections 23-24).
 */
@Controller
public class PipelineController {

    private final PipelineService pipelineService;

    public PipelineController(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @PostMapping("/pipeline/rollback")
    public String rollback(RedirectAttributes redirectAttributes) {
        pipelineService.rollback();
        redirectAttributes.addFlashAttribute("message", "Rolled back to legacy pipeline (v1.0).");
        return "redirect:/pipeline-management";
    }

    @PostMapping("/pipeline/activate")
    public String activate(RedirectAttributes redirectAttributes) {
        pipelineService.activateValidator();
        redirectAttributes.addFlashAttribute("message", "Validator pipeline (v2.0) active.");
        return "redirect:/pipeline-management";
    }
}
