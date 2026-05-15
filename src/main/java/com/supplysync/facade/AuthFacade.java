package com.supplysync.facade;

import com.supplysync.models.User;
import com.supplysync.models.Marketer;
import com.supplysync.repository.UserRepository;
import com.supplysync.repository.MarketerRepository;
import com.supplysync.services.auth.AuthService;

import java.util.List;
import java.util.Optional;

/**
 * Authentication and session boundary (SRP).
 */
public final class AuthFacade {
    private final AuthService authService;
    private final UserRepository users;
    private final MarketerRepository marketers;

    public AuthFacade(AuthService authService, UserRepository users) {
        this.authService = authService;
        this.users = users;
        this.marketers = (MarketerRepository) users; // Storage implements both
    }

    public Optional<User> login(String email, String password) {
        return authService.login(email, password);
    }

    public void logout() {
        authService.logout();
    }

    public User getCurrentUser() {
        return authService.getCurrentUser();
    }

    public void register(User user) {
        authService.register(user);
    }

    public boolean resetPassword(String email, String newPassword) {
        return authService.resetPassword(email, newPassword);
    }

    public List<User> getAllUsers() {
        return users.findAllUsers();
    }

    public boolean emailTakenByOtherUser(String email, String excludeUserId) {
        if (email == null) return false;
        return users.findUserByEmail(email.trim())
                .filter(u -> excludeUserId == null || !excludeUserId.equals(u.getId()))
                .isPresent();
    }

    public void saveUser(User user) {
        users.saveUser(user);
    }

    public void addMarketer(Marketer marketer) {
        marketers.saveMarketer(marketer);
    }

    public void persistCheckoutContactForCurrentUser(String customerName, String phone, String country, String address) {
        User u = getCurrentUser();
        if (u == null) {
            return;
        }
        u.setPrefCustomerName(customerName != null ? customerName.trim() : "");
        u.setPrefCustomerPhone(phone != null ? phone.trim() : "");
        u.setPrefCustomerCountry(country != null ? country.trim() : "");
        u.setPrefShippingAddress(address != null ? address.trim() : "");
        users.saveUser(u);
    }
}
