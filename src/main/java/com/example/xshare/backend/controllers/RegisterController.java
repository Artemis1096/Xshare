package com.example.xshare.backend.controllers;

import com.example.xshare.SceneController;
import com.example.xshare.backend.models.User;
import com.example.xshare.backend.service.UserService;
import com.example.xshare.backend.config.SpringContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterController {

    private final SceneController sceneController = new SceneController();
    private final UserService userService = SpringContext.getContext().getBean(UserService.class);

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signUpButton;

    @FXML
    private void handleSignUp(ActionEvent event) throws IOException {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Validate inputs
        if (!isValidUsername(username)) {
            showAlert("Invalid Username", "Username must be at least 3 characters long.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert("Invalid Email", "Please enter a valid email address.");
            return;
        }

        if (!isValidPassword(password)) {
            showAlert("Weak Password", "Password must be at least 6 characters long.");
            return;
        }

        // Create and register the user
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(password);

        userService.registerUser(newUser);
        sceneController.switchToMainPage(event);
        System.out.println("User registered successfully.");
    }

    @FXML
    private void switchToLoginPage(ActionEvent event) throws IOException {
        sceneController.switchToLoginPage(event);
    }

    // Validate username: At least 3 characters
    private boolean isValidUsername(String username) {
        return username != null && username.length() >= 3;
    }

    // Validate email format
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        // Regular expression to match a valid email format
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // Validate password: At least 6 characters
    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // Show an alert with a given message
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
