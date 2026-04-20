package com.decms.common.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return "redirect:/login";
        }

        boolean anonymous = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ANONYMOUS".equals(a.getAuthority()));
        if (anonymous) {
            return "redirect:/login";
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if ("ROLE_INVESTIGATOR".equals(role)) {
                return "redirect:/investigator/upload";
            }
            if ("ROLE_FORENSIC_ANALYST".equals(role)) {
                return "redirect:/forensic/custody";
            }
            if ("ROLE_LEGAL_OFFICER".equals(role)) {
                return "redirect:/legal/verify";
            }
            if ("ROLE_ADMINISTRATOR".equals(role)) {
                return "redirect:/admin/reports";
            }
        }

        return "redirect:/access-denied";
    }
}
