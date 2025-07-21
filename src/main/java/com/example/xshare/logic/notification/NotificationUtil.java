package com.example.xshare.logic.notification;

import java.awt.*;
import java.io.IOException;

public class NotificationUtil {

    public static void sendNotification(String title, String message) {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            // Windows notification
            sendWindowsNotification(title, message);
        } else if (os.contains("mac")) {
            // macOS notification
            sendMacNotification(title, message);
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            // Linux notification
            sendLinuxNotification(title, message);
        } else {
            System.err.println("Unsupported operating system for notifications");
        }
    }

    private static void sendWindowsNotification(String title, String message) {
        if (!SystemTray.isSupported()) {
            System.err.println("SystemTray is not supported on this Windows system.");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().createImage("com/example/xshare/images/logo.png"); // Provide icon path if needed
        TrayIcon trayIcon = new TrayIcon(image, "Java Notification");

        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("Notification");
        try {
            tray.add(trayIcon);
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        } catch (AWTException e) {
            System.err.println("Error displaying Windows notification: " + e.getMessage());
        }
    }

    private static void sendMacNotification(String title, String message) {
        try {
            new ProcessBuilder("osascript", "-e",
                    "display notification \"" + message + "\" with title \"" + title + "\"")
                    .start();
        } catch (IOException e) {
            System.err.println("Error displaying macOS notification: " + e.getMessage());
        }
    }

    private static void sendLinuxNotification(String title, String message) {
        try {
            new ProcessBuilder("notify-send", title, message).start();
        } catch (IOException e) {
            System.err.println("Error displaying Linux notification: " + e.getMessage());
        }
    }
}
