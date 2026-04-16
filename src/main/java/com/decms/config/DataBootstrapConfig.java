package com.decms.config;

import com.decms.common.factory.UserFactory;
import com.decms.model.Role;
import com.decms.model.User;
import com.decms.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataBootstrapConfig {

    @Bean
    public CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            seed(userRepository, passwordEncoder, "inv-001", Role.INVESTIGATOR, "Investigator Demo", "inv@decms.local", "Investigation");
            seed(userRepository, passwordEncoder, "foren-001", Role.FORENSIC_ANALYST, "Forensic Demo", "forensic@decms.local", "Forensics");
            seed(userRepository, passwordEncoder, "legal-001", Role.LEGAL_OFFICER, "Legal Demo", "legal@decms.local", "Legal");
            seed(userRepository, passwordEncoder, "admin-001", Role.ADMINISTRATOR, "Admin Demo", "admin@decms.local", "Administration");
        };
    }

    private void seed(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      String userId,
                      Role role,
                      String name,
                      String email,
                      String department) {
        if (userRepository.findById(userId).isPresent()) {
            return;
        }
        User user = UserFactory.createUser(role, name, email, passwordEncoder.encode("password"), department);
        user.setUserId(userId);
        userRepository.save(user);
    }
}
