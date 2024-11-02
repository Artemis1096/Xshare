package com.example.xshare.backend.controllers;

import com.example.xshare.SceneController;
import com.example.xshare.backend.models.User;
import com.example.xshare.backend.service.UserService;
import com.example.xshare.backend.config.SpringContext;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

import java.io.IOException;

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
    private void handleSignUp(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(password);

        userService.registerUser(newUser);
        System.out.println("User registered successfully.");
    }

    @FXML
    private void switchToLoginPage(ActionEvent event) throws IOException {
        sceneController.switchToLoginPage(event);
    }
}
