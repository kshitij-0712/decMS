package com.decms.legal.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

import com.decms.legal.dto.AccessRequestForm;
import com.decms.legal.dto.VerificationResponse;
import com.decms.legal.service.AccessRequestService;
import com.decms.legal.service.VerificationService;
import com.decms.legal.service.AccessRequestService.AccessRequestRecord;

@Controller
@RequestMapping("/legal")
public class LegalController {

    private final VerificationService verificationService;
    private final AccessRequestService accessRequestService;

    public LegalController(VerificationService verificationService, AccessRequestService accessRequestService) {
        this.verificationService = verificationService;
        this.accessRequestService = accessRequestService;
    }

    @GetMapping("/verify")
    public String showVerifyPage(Model model) {
        model.addAttribute("evidenceOptions", new ArrayList<>(verificationService.getAvailableEvidenceIds().keySet()));
        return "legal/verify-integrity";
    }

    @PostMapping("/verify/{evidenceId}")
    public String verifyEvidence(@PathVariable String evidenceId,
                                 Authentication authentication,
                                 HttpServletRequest request,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (authentication == null || authentication.getName() == null) {
            redirectAttributes.addFlashAttribute("error", "Unable to identify legal user.");
            return "redirect:/legal/verify";
        }

        VerificationResponse response;
        try {
            response = verificationService.verifyEvidenceIntegrity(
                    evidenceId,
                    authentication.getName(),
                    request.getRemoteAddr()
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/legal/verify";
        }
        model.addAttribute("verification", response);
        return "legal/verification-result";
    }

    @GetMapping("/access")
    public String showAccessRequestPage(Model model) {
        if (!model.containsAttribute("accessRequestForm")) {
            model.addAttribute("accessRequestForm", new AccessRequestForm());
        }
        model.addAttribute("evidenceOptions", new ArrayList<>(verificationService.getAvailableEvidenceIds().keySet()));
        return "legal/request-access";
    }

    @PostMapping("/access")
    public String submitAccessRequest(
            @ModelAttribute AccessRequestForm accessRequestForm,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (authentication == null || authentication.getName() == null) {
            redirectAttributes.addFlashAttribute("error", "Unable to identify legal user.");
            return "redirect:/legal/access";
        }

        try {
            AccessRequestRecord record = accessRequestService.submitRequest(accessRequestForm, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Access request submitted: " + record.requestId());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("accessRequestForm", accessRequestForm);
            return "redirect:/legal/access";
        }
        return "redirect:/legal/access/status";
    }

    @GetMapping("/access/status")
    public String showAccessRequestStatus(Authentication authentication, Model model) {
        if (authentication == null || authentication.getName() == null) {
            model.addAttribute("requests", List.of());
            model.addAttribute("error", "Unable to identify legal user.");
            return "legal/request-status";
        }

        List<AccessRequestRecord> requests = accessRequestService.getRequestsByRequester(authentication.getName());
        model.addAttribute("requests", requests);
        return "legal/request-status";
    }
}
