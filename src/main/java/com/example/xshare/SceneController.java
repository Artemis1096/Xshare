package com.example.xshare;

import com.example.xshare.backend.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SceneController {

    @Autowired
    private UserService userService;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    private Stage stage;
    private Scene scene;
    private Parent root;

    public void switchToLoginPage(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToRegisterPage(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("register.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

//    public void switchToMainPage(ActionEvent event) throws IOException {
//        Parent root = FXMLLoader.load(getClass().getResource("mainPage.fxml")); // assuming mainPage.fxml is your main app scene
//        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//        scene = new Scene(root);
//        stage.setScene(scene);
//        stage.show();
//    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            boolean loginSuccessful = userService.authenticate(username, password);
            if (loginSuccessful) {
                showAlert("Login Successful", "Welcome, " + username + "!");
//                switchToMainPage(event); // Navigate to the main application scene
            } else {
                showAlert("Login Failed", "Invalid credentials. Please try again.");
            }
        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred during login.");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
