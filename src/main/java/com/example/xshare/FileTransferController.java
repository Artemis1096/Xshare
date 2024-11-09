package com.example.xshare;

import com.example.xshare.logic.client.FileTransferCallback;
import com.example.xshare.logic.client.NetworkScanner;
import com.example.xshare.logic.notification.NotificationUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;
import com.example.xshare.logic.client.ClientFile;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class FileTransferController implements FileTransferCallback {
    @FXML
    private ListView<String> serverListView;
    @FXML
    private Label statusLabel;
    @FXML
    private Button receiveFileButton;

    private List<String> availableServers;
    private String chosenServer;
    private ProgressWindowController progressWindowController;
    private Stage progressStage;

    @FXML
    public void initialize() {
        System.out.println("initialize called");
        ClientFile.addObserver(this);
    }

    // Handler for "Scan for Servers" button
    @FXML
    void handleScanServers(ListView<String> serverListView,Label statusLabel,Button receiveFileButton) {
        this.serverListView = serverListView;
        this.statusLabel = statusLabel;
        this.receiveFileButton = receiveFileButton;

        String subnet = ClientFile.getSubnet();
        if (subnet == null) {
            updateStatus("Could not determine the subnet.");
            return;
        }

        // Run network scan in a separate thread to avoid freezing the UI
        new Thread(() -> {
            ConcurrentSkipListSet<String> serverAddresses = NetworkScanner.scan(subnet + ".0", 254);

            // Check server availability and retrieve names
            availableServers = serverAddresses.stream()
                    .filter(ClientFile::isServerAvailable)
                    .map(ClientFile::getServerName)

                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                if (availableServers.isEmpty()) {
                    updateStatus("No valid servers found on the network.");
                } else {
                    serverListView.setItems(FXCollections.observableArrayList(availableServers));
                    receiveFileButton.setDisable(false);
                    updateStatus("Servers found. Select one to receive a file.");
                }
            });
        }).start();
    }

    // Handler for "Receive File" button
    @FXML
    void handleReceiveFile(ListView<String>serverListView,Label statusLabel,Button receiveFileButton) {
        this.serverListView = serverListView;
        this.receiveFileButton =  receiveFileButton;
        int selectedIndex = serverListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            updateStatus("Please select a server first.");
            return;
        }

        // Extract the server IP from the selected item
        chosenServer = availableServers.get(selectedIndex).split(" - ")[0];

        // Load and display the progress window
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ProgressWindow.fxml"));
            Parent root = loader.load();
            progressWindowController = loader.getController();

            progressStage = new Stage();
            progressStage.setTitle("File Transfer Progress");
            progressStage.setScene(new Scene(root));
            progressStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Start file transfer in a separate thread
        new Thread(() -> {
            updateStatus("Connecting to server: " + chosenServer);
            ClientFile.initiateFileTransfer(chosenServer); // Refactor main file transfer code to a method in ClientFile
        }).start();
    }

    private void updateStatus(String message) {
        Platform.runLater(() -> statusLabel.setText("Status: " + message));
    }



    @Override
    public void onTransferComplete(String fileName, double fileSize){
        System.out.println("here in onTransferComplete!");
        Platform.runLater(() -> {
            // Add the new client to your UI list
            System.out.println("File: " + fileName + " Transfer Completed!");
            progressWindowController.updateProgress(1.0, "Transfer Complete!");
            // Update UI component here
            NotificationUtil.sendNotification("Transfer Completed!", fileName + "received!");
            updateStatus(fileName + " Transfer Completed!");

            // Close progress window after completion
            if (progressStage != null) {
                progressStage.close();
            }
        });
    }

    @Override
    public void onTransferFailed(String errorMessage){
        Platform.runLater(() -> {
            // Add the new client to your UI list
            System.out.println("Transfer Failed: " + errorMessage);
            // Update UI component here
            NotificationUtil.sendNotification("Transfer Failed", "retry!");
            updateStatus("Transfer Failed!");
            progressWindowController.updateProgress(0.0, "Transfer Failed");

            // Close progress window after failure
            if (progressStage != null) {
                progressStage.close();
            }
        });
    }

    @Override
    public void onProgressUpdate(String file,double progress) {
        // Assuming message is a percentage between 0.0 and 1.0
//        double progress = Double.parseDouble(message);
        String status =  String.format("Transfer in progress: %.2f", progress);

        // Update the progress bar in the new window
        progressWindowController.updateProgress(progress, (status+"%"));
    }
}
