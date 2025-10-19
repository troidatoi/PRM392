package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class Category {
    private String id;
    private String name;
    private int count;
    
    @SerializedName("availableCount")
    private int availableCount;

    public Category() {}

    public Category(String id, String name, int count, int availableCount) {
        this.id = id;
        this.name = name;
        this.count = count;
        this.availableCount = availableCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getAvailableCount() {
        return availableCount;
    }

    public void setAvailableCount(int availableCount) {
        this.availableCount = availableCount;
    }
}


