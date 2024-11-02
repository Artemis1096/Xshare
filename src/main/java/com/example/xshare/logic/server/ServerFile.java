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

