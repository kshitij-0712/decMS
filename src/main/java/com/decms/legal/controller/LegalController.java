package com.decms.legal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/legal")
public class LegalController {

    @GetMapping("/verify")
    public String verifyIntegrity() {
        return "legal/verify-integrity";
    }

    @GetMapping("/verify/result")
    public String verificationResult() {
        return "legal/verification-result";
    }

    @GetMapping("/access")
    public String requestAccess() {
        return "legal/request-access";
    }

    @GetMapping("/access/status")
    public String accessStatus(Model model) {
        model.addAttribute("statusHint", "Connect AccessRequestService here during integration.");
        return "legal/request-status";
    }
}
