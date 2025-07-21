package com.example.xshare.backend.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class UserService {

    private static final String CREDENTIALS_FILE = "/home/pranay/Documents/Xshare2/Xshare-Softablitz/src/main/java/com/example/xshare/backend/service/credentials.txt";

    public boolean authenticate(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(CREDENTIALS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] credentials = line.split(",");
                if (credentials.length == 3) {
                    String storedUsername = credentials[0];
                    String storedPassword = credentials[1];
                    if (storedUsername.equals(username) && storedPassword.equals(password)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
