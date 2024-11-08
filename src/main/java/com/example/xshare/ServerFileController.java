package com.example.xshare;

import com.example.xshare.logic.server.ClientConnectionObserver;
import com.example.xshare.logic.server.ServerFile;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

public class ServerFileController implements ClientConnectionObserver {
    @FXML
    private Button sendFileButton;
    @FXML
    private Button listClientsButton;
    @FXML
    private ListView<String> clientListView;

    private ServerFile server;

    private final ObservableList<String> connectedClientList = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        // Initialize server and add connected clients if necessary
        sendFileButton.setDisable(true);
        clientListView.setItems(connectedClientList);
        listClientsButton.setDisable(true);
        ServerFile.addObserver(this);
    }

    @Override
    public void onClientConnected(String clientInfo) {
        Platform.runLater(() -> {
            // Add the new client to your UI list
            System.out.println("New client connected: " + clientInfo);
            // Update UI component here
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
    private void handleStartServer() throws IOException {
        server = new ServerFile(); // Instantiate server

        // Start server components in a new thread to avoid blocking the UI
        new Thread(() -> {
            try {
                server.start(); // Assume start() method initializes all server sockets and threads
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
