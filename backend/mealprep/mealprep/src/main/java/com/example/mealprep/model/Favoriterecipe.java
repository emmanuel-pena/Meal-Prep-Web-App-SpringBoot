package com.example.mealprep.model;

public class Favoriterecipe {
    private String id;
    private String memberId;
    private int recipeId;

    public Favoriterecipe(){

    }

    public Favoriterecipe(String memberId,
                          int recipeId){
        this.memberId = memberId;
        this.recipeId = recipeId;
    }
    public void setId(String id) {
        this.id = id;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public String getId() {
        return id;
    }

    public String getMemberId() {
        return memberId;
    }

    public int getRecipeId() {
        return recipeId;
    }
}
