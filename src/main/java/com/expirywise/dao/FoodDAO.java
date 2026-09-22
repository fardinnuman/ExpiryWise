package com.expirywise.dao;

import com.expirywise.database.DatabaseManager;
import com.expirywise.model.Food;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FoodDAO {

    public void addFood(Food food) {

        String sql = """
                INSERT INTO foods
                (name, category, quantity, purchase_date, expiry_date, location, is_favorite, image)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, food.getName());
            statement.setString(2, food.getCategory());
            statement.setString(3, food.getQuantity());

            if (food.getPurchaseDate() != null) {
                statement.setString(4, food.getPurchaseDate().toString());
            } else {
                statement.setNull(4, java.sql.Types.VARCHAR);
            }

            statement.setString(5, food.getExpiryDate().toString());
            statement.setString(6, food.getLocation());
            statement.setInt(7, food.isFav() ? 1 : 0);
            statement.setString(8, food.getImage());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    food.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to add food.");
            e.printStackTrace();
        }
    }

    public List<Food> getAllFoods() {

        List<Food> foods = new ArrayList<>();

        String sql = "SELECT * FROM foods ORDER BY expiry_date ASC";

        try (Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Food food = new Food();

                food.setId(resultSet.getInt("id"));
                food.setName(resultSet.getString("name"));
                food.setCategory(resultSet.getString("category"));
                food.setQuantity(resultSet.getString("quantity"));

                String purchaseDate = resultSet.getString("purchase_date");
                if (purchaseDate != null && !purchaseDate.isBlank()) {
                    food.setPurchaseDate(LocalDate.parse(purchaseDate));
                }

                String expiryDate = resultSet.getString("expiry_date");
                if (expiryDate != null && !expiryDate.isBlank()) {
                    food.setExpiryDate(LocalDate.parse(expiryDate));
                }

                food.setLocation(resultSet.getString("location"));
                food.setFav(resultSet.getInt("is_favorite") == 1);
                food.setImage(resultSet.getString("image"));

                foods.add(food);
            }

        } catch (SQLException e) {
            System.err.println("Failed to retrieve foods.");
            e.printStackTrace();
        }

        return foods;
    }

    public void updateFood(Food food) {

        String sql = """
                UPDATE foods
                SET name = ?,
                    category = ?,
                    quantity = ?,
                    purchase_date = ?,
                    expiry_date = ?,
                    location = ?,
                    is_favorite = ?,
                    image = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, food.getName());
            statement.setString(2, food.getCategory());
            statement.setString(3, food.getQuantity());

            if (food.getPurchaseDate() != null) {
                statement.setString(4, food.getPurchaseDate().toString());
            } else {
                statement.setNull(4, java.sql.Types.VARCHAR);
            }

            statement.setString(5, food.getExpiryDate().toString());
            statement.setString(6, food.getLocation());
            statement.setInt(7, food.isFav() ? 1 : 0);
            statement.setString(8, food.getImage());
            statement.setInt(9, food.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to update food.");
            e.printStackTrace();
        }
    }

    public void deleteFood(int id) {

        String sql = "DELETE FROM foods WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to delete food.");
            e.printStackTrace();
        }
    }
}