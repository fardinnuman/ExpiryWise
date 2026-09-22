package com.expirywise.dao;

import com.expirywise.database.DatabaseManager;
import com.expirywise.model.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class UserDAO {

    public static final String DEMO_EMAIL = "demo@expirywise.app";
    public static final String DEMO_PASS_1 = "password123";
    public static final String DEMO_PASS_2 = "demo2026";

    public UserDAO() {
        DatabaseManager.initializeDatabase();
        seedDemoUserIfNeeded();
    }

    public static String hashPassword(String password) {
        if (password == null) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public boolean emailExists(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String sql = "SELECT id FROM users WHERE LOWER(email) = LOWER(?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email.trim());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Failed to check email existence: " + e.getMessage());
            return false;
        }
    }

    public boolean registerUser(String name, String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return false;
        }

        String trimmedEmail = email.trim();
        if (emailExists(trimmedEmail)) {
            return false;
        }

        String sql = "INSERT INTO users (name, email, password_hash, created_at) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name != null ? name.trim() : "");
            statement.setString(2, trimmedEmail);
            statement.setString(3, hashPassword(password));
            statement.setString(4, LocalDateTime.now().toString());

            int affected = statement.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            System.err.println("Failed to register user: " + e.getMessage());
            return false;
        }
    }

    public boolean authenticate(String email, String password) {
        if (email == null || password == null) {
            return false;
        }

        String trimmedEmail = email.trim();

        if (trimmedEmail.equalsIgnoreCase(DEMO_EMAIL) &&
                (password.equals(DEMO_PASS_1) || password.equals(DEMO_PASS_2))) {
            return true;
        }

        String sql = "SELECT password_hash FROM users WHERE LOWER(email) = LOWER(?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, trimmedEmail);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    String inputHash = hashPassword(password);
                    return storedHash != null && storedHash.equals(inputHash);
                }
            }

        } catch (SQLException e) {
            System.err.println("Authentication query failed: " + e.getMessage());
        }

        return false;
    }

    public User getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        String sql = "SELECT id, name, email, password_hash, created_at FROM users WHERE LOWER(email) = LOWER(?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email.trim());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getString("created_at")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to retrieve user: " + e.getMessage());
        }

        return null;
    }

    private void seedDemoUserIfNeeded() {
        try {
            if (!emailExists(DEMO_EMAIL)) {
                String sql = "INSERT INTO users (name, email, password_hash, created_at) VALUES (?, ?, ?, ?)";
                try (Connection connection = DatabaseManager.getConnection();
                     PreparedStatement statement = connection.prepareStatement(sql)) {

                    statement.setString(1, "Demo Chef");
                    statement.setString(2, DEMO_EMAIL);
                    statement.setString(3, hashPassword(DEMO_PASS_1));
                    statement.setString(4, LocalDateTime.now().toString());
                    statement.executeUpdate();
                }
            }
        } catch (Exception e) {
        }
    }
}
