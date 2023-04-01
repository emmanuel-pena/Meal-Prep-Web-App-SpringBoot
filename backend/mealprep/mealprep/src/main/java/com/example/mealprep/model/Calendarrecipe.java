package com.example.mealprep.model;

public class Calendarrecipe {
    private String memberId;
    private String meal;
    private String title;
    private int recipeId;
    private String planned;

    public Calendarrecipe(){

    }

    public Calendarrecipe(String memberId,
                          String meal, String title,
                          int recipeId, String planned){
        this.memberId = memberId;
        this.meal = meal;
        this.title = title;
        this.recipeId = recipeId;
        this.planned = planned;
    }
    public String getMemberId() {
        return memberId;
    }

    public String getMeal() {
        return meal;
    }

    public String getTitle() {
        return title;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public String getPlanned() {
        return planned;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public void setMeal(String meal) {
        this.meal = meal;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public void setPlanned(String planned) {
        this.planned = planned;
    }
}
