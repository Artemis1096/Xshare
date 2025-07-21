package com.example.xshare.logic.client;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class ClientFile{
    private static final int SERVER_PORT = 3000; // File transfer port
    private static final int SERVER_NAME_ACCESS_PORT = 3001;
    private static final int SERVER_AVAILABLE_PORT = 3002;
    private static final int BUFFER_SIZE = 65536;
    private static final long PROGRESS_THRESHOLD = 1048576L;
    private static final String USERNAME = System.getProperty("user.name");
    private static final Set<FileTransferCallback> observers = new HashSet<>();

    public static void addObserver(FileTransferCallback observer) { observers.add(observer);}

    public static void notifyCompletion(String filename,double fileSize) {
        System.out.println("notify completion" + observers.size());
        for(FileTransferCallback observer : observers) {
            observer.onTransferComplete(filename,fileSize);
        }
    }

    public static void notifyFailure(String filename) {
        for(FileTransferCallback observer : observers) {
            observer.onTransferFailed(filename);
        }
    }
    public static void notifyProgress(String filename, double progress) {
        for (FileTransferCallback observer : observers) {
            observer.onProgressUpdate(filename, progress);
        }
    }

    public static void initiateFileTransfer(String chosenServer) {
        try {
            Socket socket = new Socket(chosenServer, SERVER_PORT);
            socket.setSoTimeout(30000); // Set a timeout for the file transfer connection

            // Send client username to the server
            try (DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                 DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {

                dataOutputStream.writeUTF(USERNAME); // Send the username to the server
                dataOutputStream.flush(); // Send the username to the server

                dataInputStream.readUTF(); // Skip the server name confirmation
                while (true) {
                    // Read the file name or END_OF_TRANSFER message
                    String fileName = dataInputStream.readUTF();

                    // Check for end of transfer
                    if ("END_OF_TRANSFER".equals(fileName)) {
                        System.out.println("All files have been successfully received.");
                        break;
                    }
                    // Read the file size
                    long fileSize = dataInputStream.readLong();
                    System.out.printf("Receiving file '%s' of size %.2f MB%n", fileName, fileSize / 1048576.0);

                    // Decode the AES key for the file
                    String encodedKey = dataInputStream.readUTF();
                    SecretKeySpec aesKey = new SecretKeySpec(Base64.getDecoder().decode(encodedKey), "AES");

                    try (FileOutputStream fileOutputStream = new FileOutputStream("received_" + fileName)) {
                        System.out.println("Starting to receive file: " + fileName);

                        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                        cipher.init(Cipher.DECRYPT_MODE, aesKey);

                        long bytesReadTotal = 0L;
                        long lastProgressDisplay = 0L;

                        // Decrypt file in chunks
                        while (true) {
                            // Get chunk length or break if end of chunks
                            int chunkLength = dataInputStream.readInt();
                            if (chunkLength == -1) break;

                            // Read and decrypt the chunk
                            byte[] encryptedChunk = new byte[chunkLength];
                            dataInputStream.readFully(encryptedChunk);  // Ensure full chunk is read

                            // Decrypt and write chunk to the file output stream
                            byte[] decryptedData = cipher.update(encryptedChunk);
                            if (decryptedData != null) {
                                fileOutputStream.write(decryptedData);
                                bytesReadTotal += decryptedData.length;

                                if (bytesReadTotal - lastProgressDisplay >= PROGRESS_THRESHOLD) {
                                    double progressPercentage = (((double) bytesReadTotal / fileSize) * 100);
                                    double progressInMB = bytesReadTotal / 1048576.0;
                                    System.out.printf("Received: %.2f MB (%.2f%%)%n", progressInMB, progressPercentage);

                                    lastProgressDisplay = bytesReadTotal;
                                    notifyProgress(fileName, progressPercentage);
                                }

                            }
                        }

                        // Finalize decryption for any remaining data
                        byte[] finalBlock = cipher.doFinal();
                        if (finalBlock != null) {
                            fileOutputStream.write(finalBlock);
                            bytesReadTotal += finalBlock.length;
                        }
                        notifyCompletion(fileName,bytesReadTotal);
                        System.out.printf("File '%s' received successfully. Total size: %.2f MB%n", fileName, bytesReadTotal / 1048576.0);
                    }
                }
            } catch (IOException | GeneralSecurityException e) {
                notifyFailure(e.getMessage());
                System.err.println("Error during file reception: " + e.getMessage());
            } finally {
                socket.close();
            }
        } catch (IOException e) {
            notifyFailure(e.getMessage());
            System.err.println("Failed to connect to the server: " + e.getMessage());
        }
    }

    // Get subnet for network scanning
    public static String getSubnet() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // Skip down or loopback interfaces
                if (!networkInterface.isUp() || networkInterface.isLoopback()) continue;

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress inetAddress = interfaceAddress.getAddress();

                    if (inetAddress.isSiteLocalAddress()) {
                        // Convert IP to a subnet string by zeroing out host bits
                        String ip = inetAddress.getHostAddress();
                        int subnetMaskLength = interfaceAddress.getNetworkPrefixLength();
                        return calculateSubnet(ip, subnetMaskLength);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error detecting subnet: " + e.getMessage());
        }
        return null;
    }

    private static String calculateSubnet(String ip, int prefixLength) {
        String[] octets = ip.split("\\.");
        int subnetMask = 0xffffffff << (32 - prefixLength);
        int subnetAddress = (ipToInt(octets) & subnetMask) >>> (32 - 24); // Class C subnet mask
        return ((subnetAddress >> 16) & 0xff) + "." + ((subnetAddress >> 8) & 0xff) + "." + (subnetAddress & 0xff);
    }

    private static int ipToInt(String[] octets) {
        return (Integer.parseInt(octets[0]) << 24)
                | (Integer.parseInt(octets[1]) << 16)
                | (Integer.parseInt(octets[2]) << 8)
                | Integer.parseInt(octets[3]);
    }

    public static String getServerName(String ipAddress) {
        try (Socket socket = new Socket(ipAddress, SERVER_NAME_ACCESS_PORT);
             DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {
            String serverName = dataInputStream.readUTF();
            return ipAddress + " - " + serverName;
        } catch (IOException e) {
            return ipAddress + " - Unknown";
        }
    }

    public static boolean isServerAvailable(String address) {
        try (Socket socket = new Socket(address, SERVER_AVAILABLE_PORT);
             DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {
            return dataInputStream.readBoolean();
        } catch (IOException e) {
            return false;
        }
    }
}
