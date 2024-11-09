package com.example.xshare;

import com.example.xshare.logic.client.ClientFile;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.util.Duration;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class menuController  implements Initializable {
    @FXML
    public ListView<String> serverListView;

    @FXML
    private Label statusLabel;
    @FXML
    private Button receiveFileButton;
    @FXML
    private AnchorPane vbox1,vbox2;
    private boolean isMenuOpen = false;
    private boolean dashboardIsOpen = false;
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
            if (isMenuOpen) {
                // Close the menu
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
                // Open the menu
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
    private void openAccountDashboard(){
        if(dashboardIsOpen==false){
            vbox1.setVisible(false);
            vbox2.setVisible(true);
        }
        else{
            vbox1.setVisible(true);
            vbox2.setVisible(false);
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
