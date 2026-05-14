package com.supplysync.services.auth;

import com.supplysync.models.User;
import java.util.Optional;

public interface AuthService {
    Optional<User> login(String email, String password);
    void register(User user);
    User getCurrentUser();
    void logout();
    boolean resetPassword(String email, String newPassword);
}
