package com.example.xshare.backend.utils;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class PasswordUtils {

    // Method to hash a password using BCrypt
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Method to verify a password against a hashed password
    public static boolean verifyPassword(String providedPassword, String securedPassword) {
        return BCrypt.checkpw(providedPassword, securedPassword);
    }
}
