package com.expirywise.dao;

import com.expirywise.database.DatabaseManager;
import com.expirywise.model.ShoppingItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ShoppingListDAO {

    public void addItem(ShoppingItem item) {

        String sql = """
                INSERT INTO shopping_items
                (item_name, is_completed)
                VALUES (?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, item.getItemName());
            statement.setInt(2, item.isCompleted() ? 1 : 0);

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {

                if (keys.next()) {
                    item.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to add shopping item.");
            e.printStackTrace();
        }
    }

    public List<ShoppingItem> getAllItems() {

        List<ShoppingItem> items = new ArrayList<>();

        String sql = """
                SELECT *
                FROM shopping_items
                ORDER BY id ASC
                """;

        try (Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                ShoppingItem item = new ShoppingItem();

                item.setId(resultSet.getInt("id"));
                item.setItemName(resultSet.getString("item_name"));
                item.setCompleted(
                        resultSet.getInt("is_completed") == 1);

                items.add(item);
            }

        } catch (SQLException e) {
            System.err.println("Failed to retrieve shopping items.");
            e.printStackTrace();
        }

        return items;
    }

    public void updateItem(ShoppingItem item) {

        String sql = """
                UPDATE shopping_items
                SET item_name = ?,
                    is_completed = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, item.getItemName());
            statement.setInt(2, item.isCompleted() ? 1 : 0);
            statement.setInt(3, item.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to update shopping item.");
            e.printStackTrace();
        }
    }

    public void deleteItem(int id) {

        String sql = """
                DELETE FROM shopping_items
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to delete shopping item.");
            e.printStackTrace();
        }
    }

    public void deleteCompletedItems() {
        String sql = "DELETE FROM shopping_items WHERE is_completed = 1";
        try (Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to delete completed shopping items.");
            e.printStackTrace();
        }
    }
}