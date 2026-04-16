package com.decms.forensic.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/forensic")
public class ForensicController {

    @GetMapping("/custody")
    public String custodyDashboard() {
        return "forensic/custody-dashboard";
    }

    @GetMapping("/custody/{evidenceId}")
    public String custodyLogEntry(@PathVariable String evidenceId, Model model) {
        model.addAttribute("evidenceId", evidenceId);
        return "forensic/custody-log-entry";
    }

    @GetMapping("/history")
    public String custodyHistory() {
        return "forensic/custody-history";
    }

    @GetMapping("/history/{evidenceId}")
    public String custodyHistoryByEvidence(@PathVariable String evidenceId, Model model) {
        model.addAttribute("evidenceId", evidenceId);
        return "forensic/custody-history";
    }

    @GetMapping("/flag-anomaly")
    public String flagAnomaly() {
        return "forensic/flag-anomaly";
    }
}
