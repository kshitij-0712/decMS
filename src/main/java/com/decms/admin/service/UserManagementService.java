package com.decms.admin.service;

import com.decms.common.factory.UserFactory;
import com.decms.model.Role;
import com.decms.model.User;
import com.decms.model.UserStatus;
import com.decms.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UserManagementService
 *
 * Business logic for UC-07: Manage Users & Roles.
 * Owned by Khizer Pasha (Administrator module).
 *
 * Demonstrates DIP — depends on UserRepository interface,
 * not a concrete implementation.
 * Uses UserFactory (Factory Method pattern) for user creation.
 */
@Service
public class UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(UserRepository userRepository,
                                  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ── Read operations ──────────────────────────────────────────

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public long countActiveAdmins() {
        return userRepository.findByRole(Role.ADMINISTRATOR)
                .stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .count();
    }

    // ── UC-07: Create User ───────────────────────────────────────

    /**
     * Creates a new user via UserFactory (Factory Method pattern).
     * Guard: duplicate email check.
     */
    @Transactional
    public User createUser(String name, String email, String rawPassword,
                            String department, Role role) {

        // Guard: duplicate email
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException(
                    "A user with email '" + email + "' already exists.");
        }

        String encoded = passwordEncoder.encode(
                (rawPassword == null || rawPassword.isBlank()) ? "password" : rawPassword);

        // Factory Method pattern — UserFactory creates the User
        User newUser = UserFactory.createUser(role, name, email, encoded, department);
        return userRepository.save(newUser);
    }

    // ── UC-07: Edit User ─────────────────────────────────────────

    /**
     * Updates an existing user's details.
     * Guard: email collision check if email changed.
     */
    @Transactional
    public User editUser(String userId, String name, String email,
                          String rawPassword, String department, Role role) {

        User existing = getUserById(userId);

        // Guard: email collision
        if (!existing.getEmail().equalsIgnoreCase(email)) {
            userRepository.findByEmail(email).ifPresent(u -> {
                throw new IllegalArgumentException(
                        "Email '" + email + "' is already in use.");
            });
        }

        existing.setName(name);
        existing.setEmail(email);
        existing.setDepartment(department);
        existing.setRole(role);

        if (rawPassword != null && !rawPassword.isBlank()) {
            existing.setPasswordHash(passwordEncoder.encode(rawPassword));
        }

        return userRepository.save(existing);
    }

    // ── UC-07: Deactivate User ───────────────────────────────────

    /**
     * Soft-deletes a user (status = INACTIVE).
     * Guard: cannot deactivate the last active Administrator.
     */
    @Transactional
    public void deactivateUser(String userId) {
        User user = getUserById(userId);

        if (user.getRole() == Role.ADMINISTRATOR) {
            long activeAdmins = countActiveAdmins();
            if (activeAdmins <= 1) {
                throw new IllegalStateException(
                        "Cannot deactivate the last active Administrator.");
            }
        }

        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    // ── UC-07: Reactivate User ───────────────────────────────────

    @Transactional
    public void reactivateUser(String userId) {
        User user = getUserById(userId);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    // ── UC-07: Assign Role ───────────────────────────────────────

    /**
     * Updates the role of an existing user.
     * Guard: cannot demote last active Administrator.
     */
    @Transactional
    public void assignRole(String userId, Role newRole) {
        User user = getUserById(userId);

        if (user.getRole() == Role.ADMINISTRATOR
                && newRole != Role.ADMINISTRATOR
                && countActiveAdmins() <= 1) {
            throw new IllegalStateException(
                    "Cannot change role of the last active Administrator.");
        }

        user.setRole(newRole);
        userRepository.save(user);
    }
}
