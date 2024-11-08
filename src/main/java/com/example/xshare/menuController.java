package com.example.xshare;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class menuController implements Initializable {
    @FXML
    public ListView<String> serverListView;

    @FXML
    private Label statusLabel;
    @FXML
    private Button receiveFileButton;

    @FXML
    private AnchorPane pane1, pane2;

    @FXML
    private ImageView menu;
    private Stage stage;
    private Scene scene;
    private FileTransferController fileTransferController = new FileTransferController();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TranslateTransition tt = new TranslateTransition(Duration.seconds(0.5), pane2);
        tt.setFromX(-600);
        tt.play();
        TranslateTransition tte2 = new TranslateTransition(Duration.seconds(0.5), pane1);
        tte2.setFromX(-90);
        tte2.play();
        menu.setOnMouseClicked((event) -> {
            pane1.setVisible(true);
            TranslateTransition tt2 = new TranslateTransition(Duration.seconds(0.5), pane2);
            tt2.setFromX(0);
            tt2.play();
            tte2.setFromX(0);
            tte2.play();
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
        // Load the receiving-select-page view
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

    @FXML
    public void handleScanServers(){
        fileTransferController.initialize();
        fileTransferController.handleScanServers(serverListView,statusLabel,receiveFileButton);
    }

    @FXML
    public void handleReceiveFile(){
        fileTransferController.initialize();
        fileTransferController.handleReceiveFile(serverListView,statusLabel,receiveFileButton);
    }

    @FXML
    public void switchToMainPage(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("main-landing-page.fxml")); // assuming mainPage.fxml is your main app scene
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


}
