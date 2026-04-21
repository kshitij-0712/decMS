package com.decms.forensic.controller;

import com.decms.forensic.service.CustodyLogService;
import com.decms.model.CustodyLog;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/forensic")
public class ForensicController {

    private final CustodyLogService custodyLogService;
    private final com.decms.repository.EvidenceRepository evidenceRepository;

    public ForensicController(CustodyLogService custodyLogService, com.decms.repository.EvidenceRepository evidenceRepository) {
        this.custodyLogService = custodyLogService;
        this.evidenceRepository = evidenceRepository;
    }

    @GetMapping("/custody")
    public String custodyDashboard(Model model) {
        List<CustodyLog> recentLogs = custodyLogService.getRecentLogs();
        List<CustodyLog> suspiciousLogs = custodyLogService.getSuspiciousLogs();
        model.addAttribute("recentLogs", recentLogs);
        model.addAttribute("suspiciousLogs", suspiciousLogs);
        return "forensic/custody-dashboard";
    }

    @GetMapping("/custody/{evidenceId}")
    public String custodyLogEntry(@PathVariable String evidenceId, Model model) {
        model.addAttribute("evidenceId", evidenceId);
        model.addAttribute("custodyLogs", custodyLogService.getLogsForEvidence(evidenceId));
        return "forensic/custody-log-entry";
    }

    @GetMapping("/history")
    public String custodyHistory(@RequestParam(required = false) String evidenceId, Model model) {
        if (evidenceId != null && !evidenceId.isBlank()) {
            String trimmedEvidenceId = evidenceId.trim();
            return "redirect:/forensic/history/" + trimmedEvidenceId;
        }
        model.addAttribute("allEvidence", evidenceRepository.findAll());
        return "forensic/custody-history";
    }

    @GetMapping("/history/{evidenceId}")
    public String custodyHistoryByEvidence(@PathVariable String evidenceId, Model model) {
        model.addAttribute("allEvidence", evidenceRepository.findAll());
        model.addAttribute("evidenceId", evidenceId);
        model.addAttribute("history", custodyLogService.getLogsForEvidence(evidenceId));
        return "forensic/custody-history";
    }

    @GetMapping("/anomalies")
    public String flagAnomaly(Model model) {
        model.addAttribute("anomalies", custodyLogService.getSuspiciousLogs());
        return "forensic/flag-anomaly";
    }
}
