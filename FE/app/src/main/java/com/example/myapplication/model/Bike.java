package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Bike {
    @SerializedName("_id")
    private String id;
    
    private String name;
    private String brand;
    private String model;
    private double price;
    
    @SerializedName("originalPrice")
    private double originalPrice;
    
    private String description;
    private Specifications specifications;
    private List<Image> images;
    private List<Color> colors;
    private String category;
    private String status;
    private int stock;
    private List<String> features;
    private String warranty;
    private Rating rating;
    
    @SerializedName("isFeatured")
    private boolean featured;
    
    @SerializedName("isNew")
    private boolean isNew;
    
    private List<String> tags;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("updatedAt")
    private String updatedAt;
    
    @SerializedName("discountPercentage")
    private int discountPercentage;

    // Constructors
    public Bike() {}

    public Bike(String name, String brand, String model, double price, String description, String category) {
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.price = price;
        this.description = description;
        this.category = category;
    }

    // Getters and Setters
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

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Specifications getSpecifications() {
        return specifications;
    }

    public void setSpecifications(Specifications specifications) {
        this.specifications = specifications;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public List<Color> getColors() {
        return colors;
    }

    public void setColors(List<Color> colors) {
        this.colors = colors;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public String getWarranty() {
        return warranty;
    }

    public void setWarranty(String warranty) {
        this.warranty = warranty;
    }

    public Rating getRating() {
        return rating;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(int discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    // Helper methods
    public String getFormattedPrice() {
        return String.format("%,.0f VND", price);
    }

    public String getFormattedOriginalPrice() {
        return String.format("%,.0f VND", originalPrice);
    }

    public String getCategoryDisplayName() {
        switch (category) {
            case "city": return "Xe đạp điện thành phố";
            case "mountain": return "Xe đạp điện leo núi";
            case "folding": return "Xe đạp điện gấp";
            case "cargo": return "Xe đạp điện chở hàng";
            case "sport": return "Xe đạp điện thể thao";
            default: return "Khác";
        }
    }

    public String getFirstImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).getUrl();
        }
        return null;
    }

    // Inner classes
    public static class Specifications {
        private String battery;
        private String motor;
        private String range;
        
        @SerializedName("maxSpeed")
        private String maxSpeed;
        
        private String weight;
        
        @SerializedName("chargingTime")
        private String chargingTime;

        // Getters and Setters
        public String getBattery() { return battery; }
        public void setBattery(String battery) { this.battery = battery; }
        public String getMotor() { return motor; }
        public void setMotor(String motor) { this.motor = motor; }
        public String getRange() { return range; }
        public void setRange(String range) { this.range = range; }
        public String getMaxSpeed() { return maxSpeed; }
        public void setMaxSpeed(String maxSpeed) { this.maxSpeed = maxSpeed; }
        public String getWeight() { return weight; }
        public void setWeight(String weight) { this.weight = weight; }
        public String getChargingTime() { return chargingTime; }
        public void setChargingTime(String chargingTime) { this.chargingTime = chargingTime; }
    }

    public static class Image {
        private String url;
        private String alt;

        public Image() {}
        public Image(String url, String alt) {
            this.url = url;
            this.alt = alt;
        }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getAlt() { return alt; }
        public void setAlt(String alt) { this.alt = alt; }
    }

    public static class Color {
        private String name;
        private String hex;

        public Color() {}
        public Color(String name, String hex) {
            this.name = name;
            this.hex = hex;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getHex() { return hex; }
        public void setHex(String hex) { this.hex = hex; }
    }

    public static class Rating {
        private double average;
        private int count;

        public Rating() {}
        public Rating(double average, int count) {
            this.average = average;
            this.count = count;
        }

        public double getAverage() { return average; }
        public void setAverage(double average) { this.average = average; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }
}


