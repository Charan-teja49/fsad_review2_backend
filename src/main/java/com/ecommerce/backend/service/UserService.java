package com.ecommerce.backend.service;

import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserProfile(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }

    public User updateUserProfile(String email, User updatedUser) {
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setName(updatedUser.getName());
        if (updatedUser.getPhone() != null) user.setPhone(updatedUser.getPhone());
        return userRepository.save(user);
    }

    public User updateUserProfile(String email, String name, String phone) {
        User user = userRepository.findByEmail(email).orElseThrow();
        if (name != null && !name.trim().isEmpty()) {
            user.setName(name.trim());
        }
        if (phone != null) {
            user.setPhone(phone.trim());
        }
        return userRepository.save(user);
    }

    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email).orElseThrow();
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
