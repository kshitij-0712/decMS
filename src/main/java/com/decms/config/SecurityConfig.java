package com.decms.config;

import com.decms.model.UserStatus;
import com.decms.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return usernameOrEmail -> {
            com.decms.model.User domainUser = userRepository.findById(usernameOrEmail)
                    .or(() -> userRepository.findByEmailIgnoreCase(usernameOrEmail))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail));

            boolean active = domainUser.getStatus() == UserStatus.ACTIVE;

            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(domainUser.getUserId())
                    .password(domainUser.getPasswordHash())
                    .roles(domainUser.getRole().name())
                    .disabled(!active)
                    .build();

            return userDetails;
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
