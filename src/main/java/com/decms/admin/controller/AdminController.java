package com.decms.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/reports")
    public String reportGenerator() {
        return "admin/report-generator";
    }

    @GetMapping("/reports/view")
    public String reportView() {
        return "admin/report-view";
    }

    @GetMapping("/users")
    public String userManagement() {
        return "admin/user-management";
    }

    @GetMapping("/users/new")
    public String userForm() {
        return "admin/user-form";
    }

    @GetMapping("/access-requests")
    public String accessRequests() {
        return "admin/access-requests";
    }
}
