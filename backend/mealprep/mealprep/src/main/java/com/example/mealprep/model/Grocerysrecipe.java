package com.example.mealprep.model;

public class Grocerysrecipe {
    private String id;
    private int quantity;
    private String groceryListId;
    private int recipeId;

    public Grocerysrecipe(){

    }

    public Grocerysrecipe(int quantity,
                          String GroceryListId,
                          int recipeId){
        this.quantity = quantity;
        this.groceryListId = groceryListId;
        this.recipeId = recipeId;
    }
    public String getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getGroceryListId() {
        return groceryListId;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setGroceryListId(String groceryListId) {
        this.groceryListId = groceryListId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }
}
