package com.expirywise.model;

public class ShoppingItem {

    private int id;
    private String itemName;
    private boolean completed;

    public ShoppingItem() {
    }

    public ShoppingItem(int id, String itemName, boolean completed) {
        this.id = id;
        this.itemName = itemName;
        this.completed = completed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}