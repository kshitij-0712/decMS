package com.decms.controller;

import com.decms.model.CustodyLog;
import com.decms.model.User;
import com.decms.repository.UserRepository;
import com.decms.service.CustodyHistoryService;
import com.decms.service.CustodyHistoryService.CustodyHistorySummary;
import com.decms.service.CustodyLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Forensic module.
 * Handles UC-02: Maintain Chain-of-Custody Log
 * Handles UC-08: View Custody History
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/forensic")
public class ForensicController {

    private final CustodyLogService custodyLogService;
    private final CustodyHistoryService custodyHistoryService;
    private final UserRepository userRepository;

    /**
     * UC-02: Custody Dashboard
     * Displays latest custody log entries and suspicious activities.
     * Endpoint: GET /forensic/custody
     */
    @GetMapping("/custody")
    public String getCustodyDashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            Model model,
            Authentication authentication
    ) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            log.info("Accessing custody dashboard: User={}", currentUser.getUsername());

            // Get suspicious logs
            Pageable pageable = PageRequest.of(page, pageSize);
            Page<CustodyLog> suspiciousLogs = custodyHistoryService.getSuspiciousCustodyHistory(pageable);

            // Get recent logs for current user
            List<CustodyLog> userLogs = custodyLogService.getActorCustodyLogs(currentUser.getId());

            model.addAttribute("suspiciousLogs", suspiciousLogs.getContent());
            model.addAttribute("userLogs", userLogs);
            model.addAttribute("totalSuspiciousCount", suspiciousLogs.getTotalElements());
            model.addAttribute("currentPage", page);
            model.addAttribute("currentUser", currentUser.getFullName());

            return "forensic/custody-dashboard";
        } catch (Exception e) {
            log.error("Error accessing custody dashboard", e);
            model.addAttribute("error", "Error loading custody dashboard: " + e.getMessage());
            return "forensic/custody-dashboard";
        }
    }

    /**
     * UC-02: Custody Log for Specific Evidence
     * Displays custody chain for a particular evidence item.
     * Endpoint: GET /forensic/custody/{evidenceId}
     */
    @GetMapping("/custody/{evidenceId}")
    public String getCustodyLogForEvidence(
            @PathVariable Long evidenceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            Model model,
            Authentication authentication
    ) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            log.info("Accessing custody log for evidence: EvidenceID={}, User={}", evidenceId, currentUser.getUsername());

            Pageable pageable = PageRequest.of(page, pageSize);
            Page<CustodyLog> custodyLogs = custodyHistoryService.getCustodyHistoryPaginated(evidenceId, pageable);

            model.addAttribute("evidenceId", evidenceId);
            model.addAttribute("custodyLogs", custodyLogs.getContent());
            model.addAttribute("totalPages", custodyLogs.getTotalPages());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalElements", custodyLogs.getTotalElements());

            return "forensic/custody-log-entry";
        } catch (Exception e) {
            log.error("Error accessing custody log for evidence: {}", evidenceId, e);
            model.addAttribute("error", "Error loading custody log: " + e.getMessage());
            return "forensic/custody-log-entry";
        }
    }

    /**
     * UC-08: Custody History View
     * Displays complete custody history with analysis.
     * Endpoint: GET /forensic/history/{evidenceId}
     */
    @GetMapping("/history/{evidenceId}")
    public String getCustodyHistory(
            @PathVariable Long evidenceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            Model model,
            Authentication authentication
    ) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            log.info("Accessing custody history for evidence: EvidenceID={}, User={}", evidenceId, currentUser.getUsername());

            // Get paginated history
            Pageable pageable = PageRequest.of(page, pageSize);
            Page<CustodyLog> historyPage = custodyHistoryService.getCustodyHistoryPaginated(evidenceId, pageable);

            // Generate summary statistics
            CustodyHistorySummary summary = custodyHistoryService.generateHistorySummary(evidenceId);

            model.addAttribute("evidenceId", evidenceId);
            model.addAttribute("history", historyPage.getContent());
            model.addAttribute("summary", summary);
            model.addAttribute("totalPages", historyPage.getTotalPages());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalElements", historyPage.getTotalElements());

            return "forensic/custody-history";
        } catch (Exception e) {
            log.error("Error accessing custody history for evidence: {}", evidenceId, e);
            model.addAttribute("error", "Error loading custody history: " + e.getMessage());
            return "forensic/custody-history";
        }
    }

    /**
     * Flag anomalous custody activities for further investigation.
     * This supports anomaly detection features.
     * Endpoint: GET /forensic/anomalies
     */
    @GetMapping("/anomalies")
    public String viewAnomalies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int pageSize,
            Model model,
            Authentication authentication
    ) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            log.info("Accessing anomalies view: User={}", currentUser.getUsername());

            Pageable pageable = PageRequest.of(page, pageSize);
            Page<CustodyLog> anomalies = custodyHistoryService.getSuspiciousCustodyHistory(pageable);

            model.addAttribute("anomalies", anomalies.getContent());
            model.addAttribute("totalPages", anomalies.getTotalPages());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalElements", anomalies.getTotalElements());

            return "forensic/flag-anomaly";
        } catch (Exception e) {
            log.error("Error accessing anomalies", e);
            model.addAttribute("error", "Error loading anomalies: " + e.getMessage());
            return "forensic/flag-anomaly";
        }
    }

    /**
     * REST endpoint to get custody logs as JSON (for AJAX/API consumption).
     */
    @GetMapping(value = "/api/custody/{evidenceId}", produces = "application/json")
    @ResponseBody
    public List<CustodyLog> getCustodyLogsJson(@PathVariable Long evidenceId) {
        log.info("API: Fetching custody logs for evidence: {}", evidenceId);
        return custodyLogService.getCustodyHistory(evidenceId);
    }

    /**
     * REST endpoint to get custody history summary as JSON.
     */
    @GetMapping(value = "/api/history-summary/{evidenceId}", produces = "application/json")
    @ResponseBody
    public CustodyHistorySummary getHistorySummaryJson(@PathVariable Long evidenceId) {
        log.info("API: Generating history summary for evidence: {}", evidenceId);
        return custodyHistoryService.generateHistorySummary(evidenceId);
    }
}
