XShare Sharing App

XShare is a cross-operating-system file-sharing application developed in Java. It enables secure file transfers between devices over a network using AES encryption. This project features a server-client model, where clients can connect to a central server, select files, and monitor transfer progress through a JavaFX user interface.
Features

    Cross-Platform Support: Runs on any OS that supports Java (Windows, macOS, Linux).
    Secure Transfers: All files are encrypted using AES for secure sharing.
    JavaFX User Interface: Modern, interactive UI for file selection, progress tracking, and client management.
    Multi-Threaded Operations: Allows simultaneous file transfers with real-time progress updates.
    Drag-and-Drop Support: User-friendly file selection using drag-and-drop.
    Server-Client Model: Centralized server managing multiple client connections.

Installation Prerequisites

    Java Development Kit (JDK) 8 or later
    JavaFX (included in JDK 11 or later, or add manually for earlier versions)
    MySQL installed

Installation Steps

    Clone the Repository:

    git clone https://github.com/Artemis1096/Xshare-Softablitz.git
    cd Xshare-Softablitz

    Build and Run: Open the project in your preferred IDE, build, and run the application.

How to Use XShare

    Network Setup: Connect the sender and receiver devices to the same network.
    Start Sharing: Once connected, you’re ready to share files securely!

Troubleshooting JavaFX Errors

If you encounter a JavaFX-related error, follow these steps:

    Ensure JavaFX SDK is Added:
        Check that the JavaFX SDK is incorporated in the Project Structure settings of your IDE.
![screenshot](https://github.com/Artemis1096/Xshare-Softablitz/blob/6380cc1ef8837a4bbd66737db6c92974b428c855/screenshot/project-structure.png)
    
    Add VM Options to the Project:
        Set the following VM options (replace <path-to-javafx-sdk> with the actual path to your JavaFX SDK):
    --module-path <path-to-javafx-sdk>/lib --add-modules javafx.controls,javafx.fxml
![screenshot](https://github.com/Artemis1096/Xshare-Softablitz/blob/6380cc1ef8837a4bbd66737db6c92974b428c855/screenshot/add-vm.png)


Youtube Demo:

[![Watch the tutorial on YouTube](https://img.youtube.com/vi/YErdXhFl8Sw/0.jpg)](https://www.youtube.com/watch?v=YErdXhFl8Sw)
