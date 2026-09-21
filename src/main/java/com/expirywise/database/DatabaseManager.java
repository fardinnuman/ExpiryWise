package com.expirywise.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DATABASE_URL = "jdbc:sqlite:expirywise.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    public static void initializeDatabase() {

        String foodSql = """
                CREATE TABLE IF NOT EXISTS foods (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    category TEXT,
                    quantity TEXT,
                    purchase_date TEXT,
                    expiry_date TEXT NOT NULL,
                    location TEXT,
                    is_favorite INTEGER DEFAULT 0,
                    image TEXT
                )
                """;

        String shoppingSql = """
                CREATE TABLE IF NOT EXISTS shopping_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_name TEXT NOT NULL,
                    is_completed INTEGER DEFAULT 0
                )
                """;

        String userSql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT,
                    email TEXT UNIQUE NOT NULL COLLATE NOCASE,
                    password_hash TEXT NOT NULL,
                    created_at TEXT NOT NULL
                )
                """;

        try (Connection connection = getConnection();
                Statement statement = connection.createStatement()) {

            statement.execute(foodSql);
            statement.execute(shoppingSql);
            statement.execute(userSql);

            try {
                statement.execute("ALTER TABLE users ADD COLUMN name TEXT;");
            } catch (SQLException ignored) {
            }

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed.");
            e.printStackTrace();
        }
    }
}