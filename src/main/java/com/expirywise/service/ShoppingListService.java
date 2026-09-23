package com.expirywise.service;

import com.expirywise.dao.ShoppingListDAO;
import com.expirywise.model.ShoppingItem;
import java.util.List;

public class ShoppingListService {

    private final ShoppingListDAO shoppingListDAO = new ShoppingListDAO();

    public void addItem(ShoppingItem item) {
        shoppingListDAO.addItem(item);
    }

    public List<ShoppingItem> getAllItems() {
        return shoppingListDAO.getAllItems();
    }

    public void updateItem(ShoppingItem item) {
        shoppingListDAO.updateItem(item);
    }

    public void deleteItem(int id) {
        shoppingListDAO.deleteItem(id);
    }

    public void deleteCompletedItems() {
        shoppingListDAO.deleteCompletedItems();
    }
}