package com.example.xshare;

import com.example.xshare.logic.server.ServerFile;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
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
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.catalina.Server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class FileSelectionController implements Initializable {

    @FXML
    private ListView<String> fileListView; // This will refer to the ListView in FXML
    private List<String> filePaths = new ArrayList<>();

    @FXML
    private AnchorPane pane1, pane2;
    private boolean isMenuOpen = false;
    @FXML
    private ImageView menu;
    @FXML
    private Button removeFileButton;
    @FXML
    private Pane dropArea; // Reference to the drag-and-drop area (add this to your FXML)
    private Stage stage;
    private Scene scene;
    @FXML
    private AnchorPane vbox1,vbox2;
    private boolean dashboardIsOpen = false;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Enable drag-and-drop functionality on the drop area
        setupDragAndDrop();
        //menu functionality
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
        removeFileButton.setOnAction(event -> removeSelectedFile());
//        vbox2.setVisible(false);
    }

    // Method to open the file explorer
    public void openFileExplorer() {
        // Create a FileChooser instance
        FileChooser fileChooser = new FileChooser();

        // Optionally set extensions to filter file types
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("All Files (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show the open dialog and get the selected files
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);

        // Check if the user selected any files
        if (selectedFiles != null) {
            // Add the names of the selected files to the ListView
            for (File file : selectedFiles) {
                // Check if the file is already in the ListView to avoid duplicates
                if (!fileListView.getItems().contains(file.getName())) {
                    fileListView.getItems().add(file.getName());
                    filePaths.add(file.getAbsolutePath());
                }
            }
        }
    }

    private void setupDragAndDrop() {
        dropArea.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        dropArea.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    if (!fileListView.getItems().contains(file.getName())) {
                        fileListView.getItems().add(file.getName());
                        filePaths.add(file.getAbsolutePath());
                    }
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
    private void removeSelectedFile() {
        String selectedFile = fileListView.getSelectionModel().getSelectedItem();
        if (selectedFile != null) {
            fileListView.getItems().remove(selectedFile);
            filePaths.removeIf(filePath -> filePath.endsWith(selectedFile));
        }
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
    private  void goToSendingSelectPage(ActionEvent event) throws IOException {
        ServerFile sf = new ServerFile();
        sf.setFiles(filePaths);

        Parent root = FXMLLoader.load(getClass().getResource("sending-select-page.fxml"));
        Stage stage;

        if(event.getSource() instanceof Node) {
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        }
        else{
            throw new IllegalArgumentException("Event source is not a valid UI element.");
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private HBox deviceList;

    // Method to update nearby devices dynamically
    public void updateNearbyDevices(List<String> devices) {
        deviceList.getChildren().clear(); // Clear existing entries
        for (String device : devices) {
            Label deviceLabel = new Label(device);
            deviceLabel.setStyle("-fx-background-color: lightgray; -fx-padding: 5;");
            deviceList.getChildren().add(deviceLabel);
        }
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
