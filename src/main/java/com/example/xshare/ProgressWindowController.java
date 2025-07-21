package com.example.xshare;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

public class ProgressWindowController {
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressLabel;
    @FXML
    private Button cancelButton;

    private boolean cancelled = false;

    // Updates progress value on progress bar
    public void updateProgress(double progress, String status) {
        System.out.println(progress);
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            progressLabel.setText(status);
        });
    }

    // Handler for cancel button
    @FXML
    private void handleCancel() {
        cancelled = true;
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
