package com.example.mealprep.model;

public class Grocerylist {
    private String id;
    private String listName;
    private String createdAt;
    private String memberId;

    public Grocerylist(){

    }
    public Grocerylist(String listName,
                       String createdAt,
                       String memberId){
        this.listName = listName;
        this.createdAt = createdAt;
        this.memberId = memberId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getId() {
        return id;
    }

    public String getListName() {
        return listName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getMemberId() {
        return memberId;
    }
}
