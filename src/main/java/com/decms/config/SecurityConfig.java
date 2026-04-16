package com.decms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/login", "/access-denied", "/ui-kit", "/error", "/h2-console/**").permitAll()
                        .requestMatchers("/investigator/**").hasRole("INVESTIGATOR")
                        .requestMatchers("/forensic/**").hasRole("FORENSIC_ANALYST")
                        .requestMatchers("/legal/**").hasRole("LEGAL_OFFICER")
                        .requestMatchers("/admin/**").hasRole("ADMINISTRATOR")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            String targetUrl = "/access-denied";
                            boolean investigator = authentication.getAuthorities().stream()
                                    .anyMatch(a -> "ROLE_INVESTIGATOR".equals(a.getAuthority()));
                            boolean forensic = authentication.getAuthorities().stream()
                                    .anyMatch(a -> "ROLE_FORENSIC_ANALYST".equals(a.getAuthority()));
                            boolean legal = authentication.getAuthorities().stream()
                                    .anyMatch(a -> "ROLE_LEGAL_OFFICER".equals(a.getAuthority()));
                            boolean admin = authentication.getAuthorities().stream()
                                    .anyMatch(a -> "ROLE_ADMINISTRATOR".equals(a.getAuthority()));

                            if (investigator) {
                                targetUrl = "/investigator/upload";
                            } else if (forensic) {
                                targetUrl = "/forensic/custody";
                            } else if (legal) {
                                targetUrl = "/legal/verify";
                            } else if (admin) {
                                targetUrl = "/admin/reports";
                            }

                            response.sendRedirect(targetUrl);
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied")
                );

        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails investigator = User.withUsername("inv-001")
                .password(passwordEncoder.encode("password"))
                .roles("INVESTIGATOR")
                .build();

        UserDetails admin = User.withUsername("admin-001")
                .password(passwordEncoder.encode("password"))
                .roles("ADMINISTRATOR")
                .build();

        UserDetails legal = User.withUsername("legal-001")
                .password(passwordEncoder.encode("password"))
                .roles("LEGAL_OFFICER")
                .build();

        UserDetails forensic = User.withUsername("foren-001")
                .password(passwordEncoder.encode("password"))
                .roles("FORENSIC_ANALYST")
                .build();

        return new InMemoryUserDetailsManager(investigator, admin, legal, forensic);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
