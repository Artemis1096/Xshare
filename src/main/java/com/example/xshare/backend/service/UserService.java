package com.example.xshare.backend.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class UserService {

    private static final String CREDENTIALS_FILE = System.getenv("CREDENTIALS_FILE");

    public boolean authenticate(String username, String password) {
        if (CREDENTIALS_FILE == null) {
            System.err.println("Environment variable CREDENTIALS_FILE is not set.");
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(CREDENTIALS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] credentials = line.split(",");
                if (credentials.length == 3) {
                    String storedUsername = credentials[0].trim();
                    String storedPassword = credentials[1].trim();
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
