package com.example.xshare;

import com.example.xshare.logic.server.ClientConnectionObserver;
import com.example.xshare.logic.server.ServerFile;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ServerFileController implements ClientConnectionObserver, Initializable {
    @FXML
    private Button sendFileButton;
    @FXML
    private Button listClientsButton;
    @FXML
    private AnchorPane pane1, pane2;
    private boolean isMenuOpen = false;
    @FXML
    private ImageView menu;
    @FXML
    private ListView<String> clientListView;
    private ServerFile server;
    @FXML
    private AnchorPane vbox1, vbox2;
    private boolean dashboardIsOpen = false;
    private Stage stage;
    private Scene scene;
    private final ObservableList<String> connectedClientList = FXCollections.observableArrayList();
    private boolean isServerRunning = false;

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize server and add connected clients if necessary
        sendFileButton.setDisable(true);
        clientListView.setItems(connectedClientList);
        listClientsButton.setDisable(true);
        ServerFile.addObserver(this);
        // Only attempt to start the server if it hasn't started yet
        if (!isServerRunning) {
            try {
                handleStartServer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        TranslateTransition tt = new TranslateTransition(Duration.seconds(0.5), pane2);
        tt.setFromX(-600);
        tt.play();
        TranslateTransition tte2 = new TranslateTransition(Duration.seconds(0.5), pane1);
        tte2.setFromX(-90);
        tte2.play();
        menu.setOnMouseClicked((event) -> {
            if (isMenuOpen) {
                TranslateTransition ttClose = new TranslateTransition(Duration.seconds(0.5), pane2);
                ttClose.setFromX(0);
                ttClose.setToX(-600);
                ttClose.play();

                TranslateTransition tte2Close = new TranslateTransition(Duration.seconds(0.5), pane1);
                tte2Close.setFromX(0);
                tte2Close.setToX(-90);
                tte2Close.play();

                isMenuOpen = false;
            } else {
                pane1.setVisible(true);
                TranslateTransition ttOpen = new TranslateTransition(Duration.seconds(0.5), pane2);
                ttOpen.setFromX(-600);
                ttOpen.setToX(0);
                ttOpen.play();

                TranslateTransition tte2Open = new TranslateTransition(Duration.seconds(0.5), pane1);
                tte2Open.setFromX(-90);
                tte2Open.setToX(0);
                tte2Open.play();

                isMenuOpen = true;
            }
        });
        pane1.setOnMouseClicked((event) -> {
            TranslateTransition tt3 = new TranslateTransition(Duration.seconds(0.5), pane2);
            tt3.setFromX(-600);
            tt3.play();
            tte2.setFromX(-90);
            tte2.play();
        });
    }

    @FXML
    private void openAccountDashboard() {
        if (!dashboardIsOpen) {
            vbox1.setVisible(false);
            vbox2.setVisible(true);
        } else {
            vbox1.setVisible(true);
            vbox2.setVisible(false);
        }
        dashboardIsOpen = !dashboardIsOpen;
    }

    @Override
    public void onClientConnected(String clientInfo) {
        Platform.runLater(() -> {
            // Add the new client to the UI list
            System.out.println("New client connected: " + clientInfo);
            connectedClientList.add(clientInfo);
        });
    }

    @Override
    public void onClientDisconnected(String clientInfo) {
        Platform.runLater(() -> {
            // Remove the disconnected client from your UI list

            System.out.println("Client disconnected: " + clientInfo);
            connectedClientList.remove(clientInfo);
            // Update UI component here
        });
    }
    @FXML
    public void switchToMainPage(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("main-landing-page.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void handleStartServer() throws IOException {
        server = new ServerFile(); // Instantiate server

        // Start server components in a new thread to avoid blocking the UI
        new Thread(() -> {
            try {
                server.start(); // Assume start() method initializes all server sockets and threads
                isServerRunning = true;
                Platform.runLater(() -> {
                    sendFileButton.setDisable(false);
                    listClientsButton.setDisable(false);
                    showAlert("Server Started", "The server has been successfully started.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to start the server: " + e.getMessage()));
            }
        }).start();
    }

    private void handleRemoveClient(String clientIp) {
        Optional<String> result = showConfirmation("Remove Client", "Are you sure you want to remove " + clientIp + "?");
        if (result.isPresent() && result.get().equals("OK")) {
            server.removeClient(clientIp);
            handleListClients(); // Refresh the client list after removal
        }
    }

    @FXML
    private void handleListClients() {
        Platform.runLater(() -> {
            clientListView.getItems().clear();
            if (server != null) {
                for (Socket client : server.getConnectedClients()) {
                    String clientInfo = client.getInetAddress().getHostAddress();
                    clientListView.getItems().add(clientInfo);
                }
            }
        });
    }

    @FXML
    private void handleSendFile() {
        if (server != null) {
            server.triggerFileTransfer();
            showAlert("File Transfer", "File transfer triggered.");
        }
    }

    private Optional<String> showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(message);
        return alert.showAndWait().map(button -> button.getText());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
