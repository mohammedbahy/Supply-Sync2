package com.supplysync.services.auth;

import com.supplysync.models.User;
import com.supplysync.repository.Storage;
import java.util.Optional;

public class DefaultAuthService implements AuthService {
    private final Storage storage;
    private User currentUser;

    public DefaultAuthService(Storage storage) {
        this.storage = storage;
    }

    @Override
    public Optional<User> login(String email, String password) {
        Optional<User> user = storage.findUserByEmail(email);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            this.currentUser = user.get();
            return user;
        }
        return Optional.empty();
    }

    @Override
    public void register(User user) {
        if (storage.findUserByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists.");
        }
        storage.saveUser(user);
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public void logout() {
        this.currentUser = null;
    }

    @Override
    public boolean resetPassword(String email, String newPassword) {
        Optional<User> user = storage.findUserByEmail(email);
        if (user.isPresent()) {
            User u = user.get();
            u.setPassword(newPassword);
            storage.saveUser(u);
            return true;
        }
        return false;
    }
}
