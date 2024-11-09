package com.example.xshare;

import com.example.xshare.backend.service.UserService;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class SceneController implements Initializable{


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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Add event filter to trigger login on Enter key press in the username or password field
        EventHandler<KeyEvent> enterKeyEventHandler = event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loginButton.fire();
                event.consume();
            }
        };

        usernameField.addEventFilter(KeyEvent.KEY_PRESSED, enterKeyEventHandler);
        passwordField.addEventFilter(KeyEvent.KEY_PRESSED, enterKeyEventHandler);
    }

    public void switchToLoginPage(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
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

    public void switchToMainPage(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("main-landing-page.fxml")); // assuming mainPage.fxml is your main app scene
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            boolean loginSuccessful = userService.authenticate(username, password);
            if (loginSuccessful) {
                showAlertConfirm("Login Successful", "Welcome, " + username + "!");
                switchToMainPage(event); // Navigate to the main application scene
            } else {
                showAlertError("Login Failed", "Invalid credentials. Please try again.");
            }
        } catch (Exception e) {
            showAlertError("Error", "An unexpected error occurred during login.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSendButton(Event event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("FileSelection.fxml"));
        Stage stage;

        if (event.getSource() instanceof Node) {
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        } else {
            throw new IllegalArgumentException("Event source is not a valid UI element.");
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void handleReceiveButton(Event event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("receiving-select-page.fxml"));
        Stage stage;

        if (event.getSource() instanceof Node) {
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        } else {
            throw new IllegalArgumentException("Event source is not a valid UI element.");
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }



    private void showAlertConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showAlertError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
