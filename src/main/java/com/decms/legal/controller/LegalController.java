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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

import com.decms.legal.dto.AccessRequestForm;
import com.decms.legal.dto.VerificationResponse;
import com.decms.legal.service.AccessRequestService;
import com.decms.legal.service.VerificationService;
import com.decms.legal.service.AccessRequestService.AccessRequestRecord;
import com.decms.investigator.dto.EvidenceListItemResponse;
import com.decms.investigator.service.EvidenceViewService;

@Controller
@RequestMapping("/legal")
public class LegalController {

    private final VerificationService verificationService;
    private final AccessRequestService accessRequestService;
    private final EvidenceViewService evidenceViewService;

    public LegalController(VerificationService verificationService,
                           AccessRequestService accessRequestService,
                           EvidenceViewService evidenceViewService) {
        this.verificationService = verificationService;
        this.accessRequestService = accessRequestService;
        this.evidenceViewService = evidenceViewService;
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
                                 RedirectAttributes redirectAttributes) {
        if (authentication == null || authentication.getName() == null) {
            redirectAttributes.addFlashAttribute("error", "Unable to identify legal user.");
            return "redirect:/legal/verify";
        }

        try {
            VerificationResponse response = verificationService.verifyEvidenceIntegrity(
                    evidenceId,
                    authentication.getName(),
                    request.getRemoteAddr()
            );

            redirectAttributes.addFlashAttribute("lastVerification", response);
            if (response.isIntegrityVerified()) {
                redirectAttributes.addFlashAttribute("success", "Integrity verified for evidence " + evidenceId + ".");
            } else {
                redirectAttributes.addFlashAttribute("warning", "Tampering detected for evidence " + evidenceId + ".");
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/legal/verify";
        }

        return "redirect:/legal/verify";
    }

    @GetMapping("/access")
    public String showAccessRequestPage(@RequestParam(required = false) String evidenceId,
                                        @RequestParam(required = false) String caseNumber,
                                        Model model) {
        if (!model.containsAttribute("accessRequestForm")) {
            AccessRequestForm form = new AccessRequestForm();
            if (evidenceId != null && !evidenceId.isBlank()) {
                form.setEvidenceId(evidenceId.trim());
            }
            if (caseNumber != null && !caseNumber.isBlank()) {
                form.setCaseNumber(caseNumber.trim());
            }
            model.addAttribute("accessRequestForm", form);
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

    @GetMapping("/evidence")
    public String showApprovedEvidence(Authentication authentication,
                                       Model model) {
        if (authentication == null || authentication.getName() == null) {
            model.addAttribute("approvedEvidence", List.of());
            model.addAttribute("error", "Unable to identify legal user.");
            return "legal/approved-evidence";
        }

        List<String> approvedIds = accessRequestService.getApprovedEvidenceIdsForRequester(authentication.getName());

        List<EvidenceListItemResponse> approvedEvidence = approvedIds.stream()
                .map(id -> {
                    try {
                        return evidenceViewService.listEvidence(null, null).stream()
                                .filter(e -> id.equals(e.getEvidenceId()))
                                .findFirst()
                                .orElse(null);
                    } catch (Exception ex) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .toList();

        model.addAttribute("approvedEvidence", approvedEvidence);
        model.addAttribute("approvedEvidenceIds", approvedIds);
        return "legal/approved-evidence";
    }

    @GetMapping("/evidence/{evidenceId}")
    public String showEvidenceDetails(@PathVariable String evidenceId,
                                      Authentication authentication,
                                      HttpServletRequest request,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        if (authentication == null || authentication.getName() == null) {
            redirectAttributes.addFlashAttribute("error", "Unable to identify legal user.");
            return "redirect:/legal/evidence";
        }

        String username = authentication.getName();
        List<String> approvedIds = accessRequestService.getApprovedEvidenceIdsForRequester(username);
        
        if (!approvedIds.contains(evidenceId)) {
            redirectAttributes.addFlashAttribute("error", "You do not have approved access to evidence " + evidenceId);
            return "redirect:/legal/evidence";
        }

        try {
            com.decms.investigator.dto.EvidenceDetailsResponse details = 
                evidenceViewService.getEvidenceDetails(evidenceId, username, request.getRemoteAddr(), true);
            model.addAttribute("details", details);
            return "investigator/evidence-details";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/legal/evidence";
        }
    }
}
