package com.example.xshare.logic.server;

import javax.crypto.*;
import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.*;

public class ServerFile {
    private static final int PORT = 3000;
    private static final String FILE_PATH = "cc.pdf";
    private static final int EXPECTED_CLIENTS = 1; // Set the number of clients to wait for
    private static final int BUFFER_SIZE = 64 * 1024; // 64 KB buffer
    private static CountDownLatch latch = new CountDownLatch(EXPECTED_CLIENTS);
    private static ExecutorService pool = Executors.newFixedThreadPool(EXPECTED_CLIENTS);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for clients...");

            // Generate the AES key
            SecretKey aesKey = generateAESKey();
            String encodedKey = Base64.getEncoder().encodeToString(aesKey.getEncoded());
//            System.out.println("AES Key generated and encoded: " + encodedKey);

            // Accept the expected number of clients
            for (int i = 0; i < EXPECTED_CLIENTS; i++) {
                Socket clientSocket = serverSocket.accept();

                //getting the IP address
                InetSocketAddress socketAddress = (InetSocketAddress) clientSocket.getRemoteSocketAddress();
                InetAddress clientAddress = socketAddress.getAddress();
                String clientIpAddress = clientAddress.getHostAddress();
                String clientHostname = "";
                Boolean hostnameFetched = false;
                //hostname from IP
                try {
                    // Define the shell command with the variable IP address
                    String command = "host " + clientIpAddress + " | grep -v '\\.local' | awk '/domain name pointer/ {print $5}' | sed 's/\\.$//'";

                    // Use ProcessBuilder to run the shell command
                    ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
                    Process process = processBuilder.start();

                    // Capture the output of the command
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder output = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append(" ");
                    }
                    // Wait for the process to finish
                    process.waitFor();
                    String hostname = output.toString().trim();
                    hostnameFetched=true;
                    System.out.println(hostname + " Connected");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(!hostnameFetched)
                    System.out.println(clientAddress + " Connected");

                // Decrement the latch count when a client connects
                latch.countDown();

                // Add client to the thread pool for handling later
                pool.execute(new ClientHandler(clientSocket, FILE_PATH, latch, aesKey));
            }

            // Wait until all clients are connected
            latch.await();
            System.out.println("All clients connected. Starting file transfer to all clients...");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
            System.out.println("Server has stopped.");
        }
    }

    private static SecretKey generateAESKey() throws IOException {
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
    private static final int BUFFER_SIZE = 64 * 1024;
    private final Socket clientSocket;
    private final String filePath;
    private final SecretKey aesKey;
    private final CountDownLatch latch;

    public ClientHandler(Socket socket, String filePath, CountDownLatch latch, SecretKey aesKey) {
        this.clientSocket = socket;
        this.filePath = filePath;
        this.aesKey = aesKey;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            // Wait for all clients to connect
            latch.await();

            try (DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                 FileInputStream fileInputStream = new FileInputStream(filePath)) {

                // Send the file name and AES key to the client
                File file = new File(filePath);
                dataOutputStream.writeUTF(file.getName());
                dataOutputStream.writeUTF(Base64.getEncoder().encodeToString(aesKey.getEncoded()));  // Send AES key
                dataOutputStream.flush();

                // Initialize AES Cipher with padding
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, aesKey);

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;

                // Transfer file in encrypted chunks
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    byte[] encryptedData = cipher.update(buffer, 0, bytesRead);

//                    this is to test that encryption works (uncomment to test)
//                    System.out.println("Encrypted data sample (base64): " + Base64.getEncoder().encodeToString(encryptedData).substring(0, 50)); // Only printing a part

                    if (encryptedData != null) {
                        dataOutputStream.write(encryptedData);
                        dataOutputStream.flush();
                    }
                }
                byte[] finalBlock = cipher.doFinal();
                if (finalBlock != null) {
                    dataOutputStream.write(finalBlock);
                    dataOutputStream.flush();
                }

                System.out.println("File sent! ");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                clientSocket.close();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
