package com.example.jobportal.service;

import com.example.jobportal.entity.User;
import com.example.jobportal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void saveUser(User user) {
        // Ensure every persisted user has a role — default to ROLE_STUDENT
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("ROLE_STUDENT");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public void saveUserRawPassword(User user) {
        // Ensure every persisted user has a role — default to ROLE_STUDENT
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("ROLE_STUDENT");
        }
        // Save password as-is (already encoded externally or manually)
        userRepository.save(user);
    }

    @SuppressWarnings("null")
    public void updateUser(User user) {
        userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userRepository.save(user);
    }

    public User findByResetToken(String resetToken) {
        return userRepository.findByResetToken(resetToken);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @SuppressWarnings("null")
    public User findById(Long id) {
        Optional<User> result = userRepository.findById(id);
        return result.orElse(null);
    }

    @SuppressWarnings("null")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
