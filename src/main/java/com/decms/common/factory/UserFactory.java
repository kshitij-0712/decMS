package com.decms.common.factory;

import com.decms.model.Role;
import com.decms.model.User;

public final class UserFactory {

    private UserFactory() {
    }

    public static User createUser(Role role, String name, String email, String encodedPassword, String department) {
        User user = new User();
        user.setRole(role);
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(encodedPassword);
        user.setDepartment(department);
        return user;
    }
}
