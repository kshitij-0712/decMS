package com.decms.admin.controller;

import com.decms.admin.dto.AuditReportResponse;
import com.decms.admin.dto.UserManagementForm;
import com.decms.admin.service.ReportService;
import com.decms.admin.service.UserManagementService;
import com.decms.model.AccessRequest;
import com.decms.model.AuditReport;
import com.decms.model.RequestStatus;
import com.decms.model.Role;
import com.decms.model.User;
import com.decms.repository.AccessRequestRepository;
import com.decms.repository.TamperingAlertRepository;
import com.decms.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserManagementService   userManagementService;
    private final ReportService           reportService;
    private final AccessRequestRepository accessRequestRepository;
    private final TamperingAlertRepository tamperingAlertRepository;
    private final UserRepository          userRepository;

    public AdminController(UserManagementService userManagementService,
                           ReportService reportService,
                           AccessRequestRepository accessRequestRepository,
                           TamperingAlertRepository tamperingAlertRepository,
                           UserRepository userRepository) {
        this.userManagementService    = userManagementService;
        this.reportService            = reportService;
        this.accessRequestRepository  = accessRequestRepository;
        this.tamperingAlertRepository = tamperingAlertRepository;
        this.userRepository           = userRepository;
    }

    // UC-04: Generate Audit Report

    @GetMapping("/reports")
    public String showReportGenerator(Model model) {
        model.addAttribute("caseIds",       reportService.getAllCaseIds());
        model.addAttribute("reportHistory", reportService.getAllReports());
        return "admin/report-generator";
    }

    @GetMapping("/reports/view")
    public String reportView() {
        return "redirect:/admin/reports";
    }

    @PostMapping("/reports/generate")
    public String generateReport(@RequestParam String caseId,
                                  @RequestParam(defaultValue = "HTML") String format,
                                  Principal principal,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            String adminId = resolveAdminId(principal);
            AuditReportResponse report = reportService.generateReport(caseId, format, adminId);
            model.addAttribute("report",        report);
            model.addAttribute("caseIds",       reportService.getAllCaseIds());
            model.addAttribute("reportHistory", reportService.getAllReports());
            return "admin/report-view";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/reports";
        }
    }

    @GetMapping("/reports/{id}/download")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String id) {
        try {
            AuditReport report = reportService.getReportById(id);
            if (!"PDF".equalsIgnoreCase(report.getFormat())) {
                return ResponseEntity.badRequest().build();
            }
            byte[] pdfBytes = Base64.getDecoder().decode(report.getContent());
            String filename = "audit-report-" + report.getCaseId() + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // UC-07: Manage Users & Roles

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users",            userManagementService.getAllUsers());
        model.addAttribute("activeAdminCount", userManagementService.countActiveAdmins());
        return "admin/user-management";
    }

    @GetMapping("/users/new")
    public String showCreateForm(Model model) {
        model.addAttribute("userForm", new UserManagementForm());
        model.addAttribute("roles",    Role.values());
        model.addAttribute("editMode", false);
        return "admin/user-form";
    }

    @PostMapping("/users/new")
    public String createUser(@Valid @ModelAttribute("userForm") UserManagementForm form,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles",    Role.values());
            model.addAttribute("editMode", false);
            return "admin/user-form";
        }
        try {
            User created = userManagementService.createUser(
                    form.getName(), form.getEmail(), form.getPassword(),
                    form.getDepartment(), form.getRole());
            redirectAttributes.addFlashAttribute("successMessage",
                    "User '" + created.getName() + "' created successfully.");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles",    Role.values());
            model.addAttribute("editMode", false);
            return "admin/user-form";
        }
    }

    @GetMapping("/users/{id}/edit")
    public String showEditForm(@PathVariable String id, Model model) {
        User user = userManagementService.getUserById(id);
        UserManagementForm form = new UserManagementForm();
        form.setUserId(user.getUserId());
        form.setName(user.getName());
        form.setEmail(user.getEmail());
        form.setDepartment(user.getDepartment());
        form.setRole(user.getRole());
        form.setEditMode(true);
        model.addAttribute("userForm", form);
        model.addAttribute("roles",    Role.values());
        model.addAttribute("editMode", true);
        return "admin/user-form";
    }

    @PostMapping("/users/{id}/edit")
    public String editUser(@PathVariable String id,
                            @Valid @ModelAttribute("userForm") UserManagementForm form,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles",    Role.values());
            model.addAttribute("editMode", true);
            return "admin/user-form";
        }
        try {
            User updated = userManagementService.editUser(
                    id, form.getName(), form.getEmail(), form.getPassword(),
                    form.getDepartment(), form.getRole());
            redirectAttributes.addFlashAttribute("successMessage",
                    "User '" + updated.getName() + "' updated successfully.");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles",    Role.values());
            model.addAttribute("editMode", true);
            return "admin/user-form";
        }
    }

    @PostMapping("/users/{id}/deactivate")
    public String deactivateUser(@PathVariable String id,
                                  RedirectAttributes redirectAttributes) {
        try {
            userManagementService.deactivateUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deactivated successfully.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/reactivate")
    public String reactivateUser(@PathVariable String id,
                                  RedirectAttributes redirectAttributes) {
        userManagementService.reactivateUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "User reactivated successfully.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String assignRole(@PathVariable String id,
                              @RequestParam Role newRole,
                              RedirectAttributes redirectAttributes) {
        try {
            userManagementService.assignRole(id, newRole);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Role updated to " + newRole.name() + " successfully.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // UC-08: Approve / Deny Access Requests

    @GetMapping("/access-requests")
    public String listAccessRequests(Model model) {
        List<AccessRequest> pending = accessRequestRepository
                .findByStatusOrderByRequestedAtAsc(RequestStatus.PENDING);
        List<AccessRequest> approved = accessRequestRepository
                .findByStatusOrderByRequestedAtAsc(RequestStatus.APPROVED);
        List<AccessRequest> denied = accessRequestRepository
                .findByStatusOrderByRequestedAtAsc(RequestStatus.DENIED);
        approved.addAll(denied);
        model.addAttribute("pendingRequests",  pending);
        model.addAttribute("resolvedRequests", approved);
        model.addAttribute("pendingCount",     pending.size());
        return "admin/access-requests";
    }

    @PostMapping("/access-requests/{id}/approve")
    public String approveRequest(@PathVariable String id,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            AccessRequest request = accessRequestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Request not found: " + id));
            User admin = resolveAdminUser(principal);
            request.setStatus(RequestStatus.APPROVED);
            request.setResolvedBy(admin);
            request.setResolvedAt(LocalDateTime.now());
            accessRequestRepository.save(request);
            redirectAttributes.addFlashAttribute("successMessage", "Access request approved.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed: " + e.getMessage());
        }
        return "redirect:/admin/access-requests";
    }

    @PostMapping("/access-requests/{id}/deny")
    public String denyRequest(@PathVariable String id,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            AccessRequest request = accessRequestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Request not found: " + id));
            User admin = resolveAdminUser(principal);
            request.setStatus(RequestStatus.DENIED);
            request.setResolvedBy(admin);
            request.setResolvedAt(LocalDateTime.now());
            accessRequestRepository.save(request);
            redirectAttributes.addFlashAttribute("successMessage", "Access request denied.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed: " + e.getMessage());
        }
        return "redirect:/admin/access-requests";
    }

    // Helpers

    private String resolveAdminId(Principal principal) {
        if (principal != null) return principal.getName();
        return "admin-001";
    }

    private User resolveAdminUser(Principal principal) {
        return userRepository.findById(resolveAdminId(principal)).orElse(null);
    }
}
