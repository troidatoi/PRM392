package com.example.project;

public class Product {
    private String bikeId;
    private String name;
    private String description;
    private String price;
    private String originalPrice;
    private int imageResId;
    private String imageUrl;
    private boolean isBestSeller;
    private boolean isSoldOut;

    public Product(String name, String description, String price, int imageResId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageResId = imageResId;
        this.isBestSeller = false;
        this.isSoldOut = false;
    }

    public Product(String name, String description, String price, int imageResId, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageResId = imageResId;
        this.imageUrl = imageUrl;
        this.isBestSeller = false;
        this.isSoldOut = false;
    }

    public Product(String name, String description, String price, String originalPrice, int imageResId, String imageUrl, boolean isBestSeller, boolean isSoldOut) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.originalPrice = originalPrice;
        this.imageResId = imageResId;
        this.imageUrl = imageUrl;
        this.isBestSeller = isBestSeller;
        this.isSoldOut = isSoldOut;
    }

    public String getBikeId() {
        return bikeId;
    }

    public void setBikeId(String bikeId) {
        this.bikeId = bikeId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getOriginalPrice() {
        return originalPrice;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isBestSeller() {
        return isBestSeller;
    }

    public boolean isSoldOut() {
        return isSoldOut;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setOriginalPrice(String originalPrice) {
        this.originalPrice = originalPrice;
    }

    public void setBestSeller(boolean bestSeller) {
        isBestSeller = bestSeller;
    }

    public void setSoldOut(boolean soldOut) {
        isSoldOut = soldOut;
    }
}

