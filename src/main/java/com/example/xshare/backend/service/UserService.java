package com.example.xshare.backend.service;

import com.example.xshare.backend.exception.UserAlreadyExistsException;
import com.example.xshare.backend.models.User;
import com.example.xshare.backend.repository.UserRepository;
import com.example.xshare.backend.utils.PasswordUtils;
import javafx.scene.control.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); // You could also inject this as a Bean
    }

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            showAlert("Email Exists","Email Already Exists, kindly login.");
            throw new UserAlreadyExistsException("Email already registered");
        }
        // Check if the username is already taken
        if (userRepository.existsByUsername(user.getUsername())) {
            showAlert("Username Taken","Username Already Exists, kindly choose another or login!.");

            throw new UserAlreadyExistsException("Username is already taken");
        }

        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    public boolean authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return false; // User not found
        }
        return PasswordUtils.verifyPassword(password, user.getPassword()); // Verify the password
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
