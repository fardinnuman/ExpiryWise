package com.expirywise.controller;

import com.expirywise.model.Food;
import com.expirywise.service.FoodService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnalyticsController {

    @FXML
    private Label wasteRiskLabel;

    @FXML
    private Label freshnessScoreLabel;

    @FXML
    private Label activeCategoriesLabel;

    @FXML
    private Label totalItemsLabel;

    @FXML
    private Label safeItemsLabel;

    @FXML
    private Label useSoonItemsLabel;

    @FXML
    private Label expiresTodayItemsLabel;

    @FXML
    private Label expiredItemsLabel;

    @FXML
    private HBox healthBanner;

    @FXML
    private Label healthIcon;

    @FXML
    private Label healthTitle;

    @FXML
    private Label healthDesc;

    @FXML
    private VBox categoryContainer;

    @FXML
    private VBox statusContainer;

    @FXML
    private HBox storageContainer;

    private final FoodService foodService = new FoodService();

    @FXML
    public void initialize() {
        loadAnalytics();
    }

    private void loadAnalytics() {
        List<Food> foods = foodService.getAllFoods();

        int total = foods.size();
        int safe = 0;
        int useSoon = 0;
        int expiresToday = 0;
        int expired = 0;

        for (Food food : foods) {
            String status = foodService.getExpiryStatus(food);
            switch (status) {
                case "Safe" -> safe++;
                case "Use Soon" -> useSoon++;
                case "Expires Today" -> expiresToday++;
                case "Expired" -> expired++;
            }
        }

        long activeCategories = foods.stream()
                .map(Food::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .count();

        int freshnessScore = total > 0 ? (int) Math.round(((double) safe / total) * 100.0) : 0;

        int wasteRisk = 0;
        if (total > 0) {
            if (safe == 0) {
                wasteRisk = 100;
            } else {
                wasteRisk = (int) Math.round(((double) (expired + expiresToday) / total) * 100.0);
            }
        }

        if (wasteRiskLabel != null) wasteRiskLabel.setText(wasteRisk + "%");
        if (freshnessScoreLabel != null) freshnessScoreLabel.setText(freshnessScore + "%");
        if (activeCategoriesLabel != null) activeCategoriesLabel.setText(String.valueOf(activeCategories));

        if (totalItemsLabel != null) totalItemsLabel.setText(String.valueOf(total));
        if (safeItemsLabel != null) safeItemsLabel.setText(String.valueOf(safe));
        if (useSoonItemsLabel != null) useSoonItemsLabel.setText(String.valueOf(useSoon));
        if (expiresTodayItemsLabel != null) expiresTodayItemsLabel.setText(String.valueOf(expiresToday));
        if (expiredItemsLabel != null) expiredItemsLabel.setText(String.valueOf(expired));

        updateHealthBanner(total, safe, useSoon, expiresToday, expired);
        loadCategoryChart(foods);
        loadStatusChart(safe, useSoon, expiresToday, expired);
        loadStorageDistribution(foods);
    }

    private void updateHealthBanner(int total, int safe, int useSoon, int expiresToday, int expired) {
        if (healthBanner == null) return;

        if (total == 0) {
            healthIcon.setText("ℹ️");
            healthTitle.setText("Pantry is empty");
            healthDesc.setText("Add groceries to monitor waste risk and view freshness insights.");
            return;
        }

        double safeRatio = (double) safe / total;
        int safePercent = (int) Math.round(safeRatio * 100);

        if (expired > 0) {
            healthIcon.setText("⚠️");
            healthTitle.setText("Attention Needed: " + expired + " item(s) expired (" + safePercent + "% fresh)");
            healthDesc.setText("Discard or consume expired items to maintain a healthy inventory.");
        } else if (expiresToday > 0 || useSoon > 0) {
            healthIcon.setText("⚡");
            healthTitle.setText("Urgent: " + (expiresToday + useSoon) + " item(s) expiring soon (" + safePercent + "% fresh)");
            healthDesc.setText("Prioritize consuming items expiring today or in the next 3 days.");
        } else {
            healthIcon.setText("🛡️");
            healthTitle.setText("Excellent: 100% of your items are safe & fresh");
            healthDesc.setText("Your inventory is in peak condition with zero waste risk.");
        }
    }

    private void loadCategoryChart(List<Food> foods) {
        if (categoryContainer == null) return;
        categoryContainer.getChildren().clear();

        if (foods.isEmpty()) {
            showEmptyNotice(categoryContainer, "No food items to display.");
            return;
        }

        Map<String, Integer> categories = new LinkedHashMap<>();
        for (Food food : foods) {
            String cat = food.getCategory() != null && !food.getCategory().isBlank()
                    ? food.getCategory() : "Others";
            categories.put(cat, categories.getOrDefault(cat, 0) + 1);
        }

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Category");
        yAxis.setLabel("Item Count");
        yAxis.setTickUnit(1);
        yAxis.setMinorTickVisible(false);

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        barChart.setPrefHeight(250);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (var entry : categories.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChart.getData().add(series);
        categoryContainer.getChildren().add(barChart);
    }

    private void loadStatusChart(int safe, int useSoon, int expiresToday, int expired) {
        if (statusContainer == null) return;
        statusContainer.getChildren().clear();

        int total = safe + useSoon + expiresToday + expired;
        if (total == 0) {
            showEmptyNotice(statusContainer, "No food items to display.");
            return;
        }

        PieChart pieChart = new PieChart();
        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(true);
        pieChart.setAnimated(false);
        pieChart.setPrefHeight(250);

        if (safe > 0) {
            pieChart.getData().add(new PieChart.Data("Safe (" + safe + ")", safe));
        }
        if (useSoon > 0) {
            pieChart.getData().add(new PieChart.Data("Use Soon (" + useSoon + ")", useSoon));
        }
        if (expiresToday > 0) {
            pieChart.getData().add(new PieChart.Data("Expires Today (" + expiresToday + ")", expiresToday));
        }
        if (expired > 0) {
            pieChart.getData().add(new PieChart.Data("Expired (" + expired + ")", expired));
        }

        statusContainer.getChildren().add(pieChart);

        for (PieChart.Data data : pieChart.getData()) {
            applySliceStyle(data);
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    applySliceStyle(data);
                }
            });
        }

        applyLegendColors(pieChart);
    }

    private void applySliceStyle(PieChart.Data data) {
        if (data == null || data.getNode() == null) return;
        String name = data.getName();
        String color = getStatusColor(name);
        String cssClass = getStatusPieClass(name);

        data.getNode().setStyle("-fx-pie-color: " + color + "; -fx-background-color: " + color + ";");
        if (cssClass != null && !data.getNode().getStyleClass().contains(cssClass)) {
            data.getNode().getStyleClass().add(cssClass);
        }
    }

    private void applyLegendColors(PieChart pieChart) {
        Runnable updateLegend = () -> {
            Set<Node> items = pieChart.lookupAll(".chart-legend-item");
            for (Node item : items) {
                if (item instanceof Label label) {
                    String color = getStatusColor(label.getText());
                    Node symbol = label.getGraphic();
                    if (symbol != null) {
                        symbol.setStyle("-fx-background-color: " + color + ";");
                    }
                }
            }
        };

        Platform.runLater(updateLegend);
        pieChart.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(updateLegend);
            }
        });
        pieChart.layoutBoundsProperty().addListener((obs, oldB, newB) -> {
            Platform.runLater(updateLegend);
        });
    }

    private String getStatusColor(String name) {
        if (name == null) return "#94a3b8";
        if (name.startsWith("Safe")) return "#10b981"; 
        if (name.startsWith("Use Soon")) return "#f59e0b"; 
        if (name.startsWith("Expires Today")) return "#f97316"; 
        if (name.startsWith("Expired")) return "#ef4444";
        return "#6366f1";
    }

    private String getStatusPieClass(String name) {
        if (name == null) return null;
        if (name.startsWith("Safe")) return "pie-slice-safe";
        if (name.startsWith("Use Soon")) return "pie-slice-soon";
        if (name.startsWith("Expires Today")) return "pie-slice-today";
        if (name.startsWith("Expired")) return "pie-slice-expired";
        return null;
    }

    private void loadStorageDistribution(List<Food> foods) {
        if (storageContainer == null) return;
        storageContainer.getChildren().clear();

        int total = foods.size();
        int fridge = 0;
        int freezer = 0;
        int pantry = 0;

        for (Food f : foods) {
            String loc = f.getLocation() != null ? f.getLocation() : "Fridge";
            switch (loc) {
                case "Fridge" -> fridge++;
                case "Freezer" -> freezer++;
                case "Pantry" -> pantry++;
                default -> fridge++;
            }
        }

        storageContainer.getChildren().addAll(
                createStorageCard("❄️ Fridge", fridge, total),
                createStorageCard("🧊 Freezer", freezer, total),
                createStorageCard("🥫 Pantry", pantry, total)
        );
    }

    private VBox createStorageCard(String name, int count, int total) {
        VBox card = new VBox(6);
        card.getStyleClass().add("storage-stat-card");
        HBox.setHgrow(card, Priority.ALWAYS);

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(name);
        title.getStyleClass().add("storage-stat-title");
        HBox.setHgrow(title, Priority.ALWAYS);

        Label countLbl = new Label(count + " items");
        countLbl.getStyleClass().add("storage-stat-count");

        top.getChildren().addAll(title, countLbl);

        double ratio = total > 0 ? (double) count / total : 0.0;
        ProgressBar bar = new ProgressBar(ratio);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.getStyleClass().add("storage-progress-bar");

        int percent = (int) Math.round(ratio * 100);
        Label percentLbl = new Label(percent + "% of inventory");
        percentLbl.getStyleClass().add("storage-stat-pct");

        card.getChildren().addAll(top, bar, percentLbl);
        return card;
    }

    private void showEmptyNotice(VBox container, String text) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new javafx.geometry.Insets(30));

        Label icon = new Label("📊");
        icon.setStyle("-fx-font-size: 28px;");

        Label label = new Label(text);
        label.getStyleClass().add("analytics-empty");

        box.getChildren().addAll(icon, label);
        container.getChildren().add(box);
    }
}