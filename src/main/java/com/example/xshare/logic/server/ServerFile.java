package com.example.xshare.logic.server;
import com.example.xshare.ServerFileController;
import javax.crypto.*;
import java.io.*;
import java.net.*;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.*;
import static com.example.xshare.logic.server.ZipCreator.zipFolder;

public class ServerFile {
    private static final int PORT = 3000; // Port for file transfer
    private static final int SERVER_NAME_ACCESS_PORT = 3001; // Port for server name
    private static final int SERVER_AVAILABLE_PORT = 3002; // Port for server availability
    private static List<String> FILE_PATH = new ArrayList<>();
    private static final String SERVER_NAME = System.getProperty("user.name"); // Custom server name
    private static ExecutorService pool = Executors.newCachedThreadPool();
    private static List<Socket> connectedClients = new ArrayList<>();
    private static AtomicBoolean transferTriggered = new AtomicBoolean(false);
    private static final List<ClientConnectionObserver> observers = new ArrayList<>();
    static String ClientName = "Unknown";
    private SecretKey aesKey;

    public ServerFile() throws IOException {
        this.aesKey = generateAESKey(); // Generate AES key on instantiation
    }
    public void setFiles(List<String>FILE_PATH){
        this.FILE_PATH = FILE_PATH;
        for(String s:FILE_PATH){
            System.out.println(s);
        }
    }
    // Start method to initialize server components
    public void start() {
        // Start three threads for each server task
        new Thread(() -> listenForServerNameRequests(SERVER_NAME_ACCESS_PORT)).start();
        new Thread(() -> listenForServerAvailabilityRequests(SERVER_AVAILABLE_PORT)).start();
        new Thread(() -> listenForFileTransfers(PORT, aesKey)).start();

        // Start a thread to listen for the "SEND" command from the console
        new Thread(ServerFile::listenForSendCommand).start();
    }

    public List<Socket> getConnectedClients() {
        return Collections.unmodifiableList(connectedClients); // Provides a safe copy for UI
    }

    public static void addObserver(ClientConnectionObserver observer) {
        observers.add(observer);
    }

    static void notifyClientConnected(String clientName) {
        for (ClientConnectionObserver observer : observers) {
            observer.onClientConnected(clientName);
        }
    }

    private static void notifyClientDisconnected(String clientInfo) {
        for (ClientConnectionObserver observer : observers) {
            observer.onClientDisconnected(clientInfo);
        }
    }


    public void triggerFileTransfer() {
        transferTriggered.set(true); // Start file transfer when called
    }

    public static void removeClient(String clientIp) {
        Optional<Socket> clientToRemove = connectedClients.stream()
                .filter(client -> client.getInetAddress().getHostAddress().equals(clientIp))
                .findFirst();

        if (clientToRemove.isPresent()) {
            try {
                clientToRemove.get().close();
                connectedClients.remove(clientToRemove.get());
                System.out.println("Client " + clientIp + " has been disconnected.");
            } catch (IOException e) {
                System.err.println("Error disconnecting client " + clientIp + ": " + e.getMessage());
            }
        } else {
            System.out.println("No client found with IP address: " + clientIp);
        }
    }

    private static void listenForSendCommand() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Commands: 'SEND' to start file transfer, 'LIST' to view clients, 'REMOVE <IP>' to disconnect a client.");

        while (!transferTriggered.get() && scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();

            if ("SEND".equalsIgnoreCase(command)) {
                transferTriggered.set(true);
                System.out.println("File transfer triggered.");
            } else if ("LIST".equalsIgnoreCase(command)) {
                listConnectedClients();
            } else if (command.startsWith("REMOVE ")) {
                String clientIp = command.substring(7).trim();
                removeClient(clientIp);
            } else {
                System.out.println("Invalid command. Type 'SEND', 'LIST', or 'REMOVE <IP>'.");
            }
        }
    }

    private static void listConnectedClients() {
        if (connectedClients.isEmpty()) {
            System.out.println("No clients currently connected.");
        } else {
            System.out.println("Connected clients:");
            for (Socket client : connectedClients) {
                System.out.println("- " + client.getInetAddress().getHostAddress());
            }
        }
    }

    static void listenForServerNameRequests(int port) {
        try (ServerSocket serverNameSocket = new ServerSocket(port)) {
            System.out.println("Listening for server name requests on port " + port + "...");

            while (true) {
                Socket clientSocket = serverNameSocket.accept();
                try (DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {
                    out.writeUTF(SERVER_NAME); // Send the server name to the client
                    out.flush();
                    System.out.println("Sent server name to client: " + clientSocket.getInetAddress());
                } finally {
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            System.err.println("Error in server name request handler: " + e.getMessage());
        }
    }

    static void listenForServerAvailabilityRequests(int port) {
        try (ServerSocket availabilitySocket = new ServerSocket(port)) {
            System.out.println("Listening for availability requests on port " + port + "...");

            while (true) {
                Socket clientSocket = availabilitySocket.accept();
                try (DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {
                    boolean isAvailable = !transferTriggered.get(); // Server is available if transfer not triggered
                    out.writeBoolean(isAvailable); // Send availability status to client
                    out.flush();
                    System.out.println("Sent availability status (" + isAvailable + ") to client: " + clientSocket.getInetAddress());
                } finally {
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            System.err.println("Error in server availability request handler: " + e.getMessage());
        }
    }

    static void listenForFileTransfers(int port, SecretKey aesKey) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server ready for file transfers on port " + port + ".");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                connectedClients.add(clientSocket);

                String clientIpAddress = clientSocket.getInetAddress().getHostAddress();
                System.out.println("Client connected for file transfer: " + clientIpAddress);

                // Handle file transfer in a new thread
                pool.execute(new ClientHandler(clientSocket, FILE_PATH, aesKey, transferTriggered, SERVER_NAME));
            }
        } catch (IOException e) {
            System.err.println("Error in file transfer handler: " + e.getMessage());
        }
    }

    static SecretKey generateAESKey() throws IOException {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128, new SecureRandom());
            return keyGen.generateKey();
        } catch (Exception e) {
            throw new IOException("Error generating AES key", e);
        }
    }
}

class ClientHandler implements Runnable {
    private static final int BUFFER_SIZE = 128 * 1024; // 64 KB buffer size for file transfer
    private final Socket clientSocket;
    private final List<String> filePaths;
    private final SecretKey aesKey;
    private final AtomicBoolean transferTriggered;
    private final String serverName;
    private ServerFileController controller = new ServerFileController();

    public ClientHandler(Socket socket, List<String> filePaths, SecretKey aesKey, AtomicBoolean transferTriggered, String serverName) {
        this.clientSocket = socket;
        this.filePaths = filePaths;
        this.aesKey = aesKey;
        this.transferTriggered = transferTriggered;
        this.serverName = serverName;
        this.controller = controller;
    }

    @Override
    public void run() {
        System.out.println("Started");
        try (DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream())) {

            String clientName = dataInputStream.readUTF();
            ServerFile.ClientName = clientName;
            ServerFile.notifyClientConnected(clientName);

            System.out.println("Client connected for file transfer: " + "clientUsername" + " (" + clientSocket.getRemoteSocketAddress() + ")");

            // Send server name to client for display
            dataOutputStream.writeUTF(serverName);
            dataOutputStream.flush();


            // Wait until transfer is triggered
            while (!transferTriggered.get()) {
                Thread.sleep(100); // Wait and check the flag periodically
            }

            // Begin file transfer
            for (String filePath : filePaths) {
                sendFile(dataOutputStream, filePath);
            }
            // Signal end of file transfer
            dataOutputStream.writeUTF("END_OF_TRANSFER");
            dataOutputStream.flush();


            System.out.println("All files sent to client: " + clientSocket.getRemoteSocketAddress());
        } catch (IOException | InterruptedException e) {
            System.err.println("Error in ClientHandler connection setup: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }

    }

private void sendFile(DataOutputStream dataOutputStream, String filePath) {
    File file = new File(filePath);
    if (file.isDirectory()) {
        try {
            zipFolder(Paths.get(filePath));
            filePath = filePath + ".zip";
        } catch (IOException e) {
            System.err.println("Error creating ZIP file: " + e.getMessage());
        }
    }
    try (FileInputStream fileInputStream = new FileInputStream(filePath)) {

        // Send file metadata first
        dataOutputStream.writeUTF(file.getName());  // Send the file name
        dataOutputStream.writeLong(file.length()); //send file size
        dataOutputStream.writeUTF(Base64.getEncoder().encodeToString(aesKey.getEncoded())); // Send AES key
        dataOutputStream.flush();

        // Initialize AES cipher for encryption
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        // Read file data, encrypt, and send in chunks
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            byte[] encryptedData = cipher.update(buffer, 0, bytesRead);
            if (encryptedData != null) {
                dataOutputStream.writeInt(encryptedData.length); // Send length of encrypted chunk
                dataOutputStream.write(encryptedData);           // Send encrypted chunk
                dataOutputStream.flush();
            }
        }

        // Finalize encryption and send any remaining bytes
        byte[] finalBlock = cipher.doFinal();
        if (finalBlock != null && finalBlock.length > 0) {
            dataOutputStream.writeInt(finalBlock.length); // Send length of final block
            dataOutputStream.write(finalBlock);           // Send final encrypted chunk
            dataOutputStream.flush();
        }

        // Signal end of this file
        dataOutputStream.writeInt(-1); // Send -1 as marker to indicate end of file
        dataOutputStream.flush();

        System.out.println("File sent: " + file.getName());

    } catch (IOException | GeneralSecurityException e) {
        System.err.println("Error sending file: " + e.getMessage());
    }
}
}