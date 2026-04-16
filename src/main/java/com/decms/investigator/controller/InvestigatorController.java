package com.decms.investigator.controller;

import com.decms.investigator.dto.EvidenceDetailsResponse;
import com.decms.investigator.dto.EvidenceListItemResponse;
import com.decms.investigator.dto.EvidenceUploadRequest;
import com.decms.investigator.dto.EvidenceUploadResult;
import com.decms.investigator.service.EvidenceUploadService;
import com.decms.investigator.service.EvidenceViewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/investigator")
public class InvestigatorController {

    private final EvidenceUploadService evidenceUploadService;
    private final EvidenceViewService evidenceViewService;

    public InvestigatorController(EvidenceUploadService evidenceUploadService,
                                  EvidenceViewService evidenceViewService) {
        this.evidenceUploadService = evidenceUploadService;
        this.evidenceViewService = evidenceViewService;
    }

    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        if (!model.containsAttribute("uploadRequest")) {
            model.addAttribute("uploadRequest", new EvidenceUploadRequest());
        }
        model.addAttribute("evidenceTypes", List.of("DOCUMENT", "IMAGE", "VIDEO", "AUDIO", "ARCHIVE", "OTHER"));
        return "investigator/upload-evidence";
    }

    @PostMapping("/upload")
    public String uploadEvidence(@Valid @ModelAttribute("uploadRequest") EvidenceUploadRequest uploadRequest,
                                 BindingResult bindingResult,
                                 Principal principal,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.uploadRequest", bindingResult);
            redirectAttributes.addFlashAttribute("uploadRequest", uploadRequest);
            redirectAttributes.addFlashAttribute("error", "Please fix highlighted fields and try again.");
            return "redirect:/investigator/upload";
        }

        String actorUserId = principal != null ? principal.getName() : "inv-001";
        String ipAddress = request.getRemoteAddr();

        try {
            EvidenceUploadResult result = evidenceUploadService.uploadEvidence(uploadRequest, actorUserId, ipAddress);
            redirectAttributes.addFlashAttribute("uploadResult", result);
            return "redirect:/investigator/upload/success";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.uploadRequest", bindingResult);
            redirectAttributes.addFlashAttribute("uploadRequest", uploadRequest);
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/investigator/upload";
        }
    }

    @GetMapping("/upload/success")
    public String showUploadSuccess(Model model, RedirectAttributes redirectAttributes) {
        if (!model.containsAttribute("uploadResult")) {
            redirectAttributes.addFlashAttribute("error", "No recent upload result available.");
            return "redirect:/investigator/upload";
        }
        return "investigator/upload-success";
    }

    @GetMapping("/evidence")
    public String listEvidence(@RequestParam(name = "caseId", required = false) String caseId,
                               @RequestParam(name = "status", required = false) String status,
                               Principal principal,
                               Model model) {
        String actorUserId = principal != null ? principal.getName() : "inv-001";
        List<EvidenceListItemResponse> evidenceItems;
        try {
            evidenceItems = evidenceViewService.listEvidence(caseId, status, actorUserId);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            evidenceItems = List.of();
        }
        model.addAttribute("evidenceItems", evidenceItems);
        model.addAttribute("selectedCaseId", caseId == null ? "" : caseId);
        model.addAttribute("selectedStatus", status == null ? "" : status);
        model.addAttribute("statusOptions", List.of("SEALED", "ANALYZED", "VERIFIED", "ARCHIVED"));
        return "investigator/evidence-list";
    }

    @GetMapping("/evidence/{id}")
    public String getEvidenceDetails(@PathVariable("id") String evidenceId,
                                     Principal principal,
                                     HttpServletRequest request,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        String actorUserId = principal != null ? principal.getName() : "inv-001";
        String ipAddress = request.getRemoteAddr();

        try {
            EvidenceDetailsResponse details = evidenceViewService.getEvidenceDetails(evidenceId, actorUserId, ipAddress, true);
            model.addAttribute("details", details);
            return "investigator/evidence-details";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/investigator/evidence";
        }
    }
}
