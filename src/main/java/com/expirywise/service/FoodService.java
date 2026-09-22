package com.expirywise.service;

import com.expirywise.dao.FoodDAO;
import com.expirywise.model.Food;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class FoodService {

    private final FoodDAO foodDAO;

    public FoodService() {
        foodDAO = new FoodDAO();
    }

    public void addFood(Food food) {
        foodDAO.addFood(food);
    }

    public List<Food> getAllFoods() {
        return foodDAO.getAllFoods();
    }

    public void updateFood(Food food) {
        foodDAO.updateFood(food);
    }

    public void deleteFood(int id) {
        foodDAO.deleteFood(id);
    }

    public long getDaysRemaining(Food food) {
        if (food == null || food.getExpiryDate() == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), food.getExpiryDate());
    }

    public String getExpiryStatus(Food food) {
        if (food == null || food.getExpiryDate() == null) {
            return "Unknown";
        }

        long daysRemaining = getDaysRemaining(food);

        if (daysRemaining < 0) {
            return "Expired";
        }

        if (daysRemaining == 0) {
            return "Expires Today";
        }

        if (daysRemaining <= 3) {
            return "Use Soon";
        }

        return "Safe";
    }

    public String getExpiryBadgeText(Food food) {
        if (food == null || food.getExpiryDate() == null) {
            return "Unknown";
        }

        long days = getDaysRemaining(food);

        if (days < -1) {
            return "Expired " + Math.abs(days) + "d ago";
        }
        if (days == -1) {
            return "Expired yesterday";
        }
        if (days == 0) {
            return "Expires today";
        }
        if (days == 1) {
            return "Expires tomorrow";
        }
        if (days <= 3) {
            return "Expires in " + days + " days";
        }
        return days + " days remaining";
    }

    public double getFreshnessProgress(Food food) {
        if (food == null || food.getExpiryDate() == null) {
            return 0.5;
        }
        long daysRemaining = getDaysRemaining(food);
        if (daysRemaining <= 0) {
            return 0.0;
        }

        LocalDate purchase = food.getPurchaseDate();
        if (purchase != null && !purchase.isAfter(food.getExpiryDate())) {
            long totalDays = ChronoUnit.DAYS.between(purchase, food.getExpiryDate());
            if (totalDays > 0) {
                double ratio = (double) daysRemaining / totalDays;
                return Math.max(0.0, Math.min(1.0, ratio));
            }
        }

        return Math.min(1.0, daysRemaining / 14.0);
    }
}