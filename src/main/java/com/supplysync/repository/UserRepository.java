package com.supplysync.repository;

import com.supplysync.models.User;

import java.util.List;
import java.util.Optional;

/** User persistence (ISP). */
public interface UserRepository {
    void saveUser(User user);

    Optional<User> findUserByEmail(String email);

    List<User> findAllUsers();
}
