package com.example.project.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class Bike {
    @SerializedName("_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("brand")
    private String brand;

    @SerializedName("model")
    private String model;

    @SerializedName("price")
    private double price;

    @SerializedName("originalPrice")
    private double originalPrice;

    @SerializedName("discount")
    private double discount;

    @SerializedName("description")
    private String description;

    @SerializedName("year")
    private int year;

    @SerializedName("color")
    private String color;

    @SerializedName("category")
    private String category;

    @SerializedName("status")
    private String status;

    @SerializedName("stock")
    private int stock;

    @SerializedName("images")
    private List<BikeImage> images;

    @SerializedName("colors")
    private List<BikeColor> colors;

    @SerializedName("specifications")
    private Specifications specifications;

    @SerializedName("features")
    private List<String> features;

    @SerializedName("warranty")
    private String warranty;

    @SerializedName("rating")
    private Rating rating;

    @SerializedName("isFeatured")
    private boolean isFeatured;

    @SerializedName("tags")
    private List<String> tags;

    @SerializedName("createdAt")
    private Date createdAt;

    @SerializedName("updatedAt")
    private Date updatedAt;

    // Constructors
    public Bike() {}

    public Bike(String name, String brand, String model, double price, String description) {
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.price = price;
        this.description = description;
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

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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

    public List<BikeImage> getImages() {
        return images;
    }

    public void setImages(List<BikeImage> images) {
        this.images = images;
    }

    public List<BikeColor> getColors() {
        return colors;
    }

    public void setColors(List<BikeColor> colors) {
        this.colors = colors;
    }

    public Specifications getSpecifications() {
        return specifications;
    }

    public void setSpecifications(Specifications specifications) {
        this.specifications = specifications;
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
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Inner classes
    public static class BikeImage {
        @SerializedName("url")
        private String url;

        @SerializedName("alt")
        private String alt;

        public BikeImage() {}

        public BikeImage(String url, String alt) {
            this.url = url;
            this.alt = alt;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getAlt() {
            return alt;
        }

        public void setAlt(String alt) {
            this.alt = alt;
        }
    }

    public static class BikeColor {
        @SerializedName("name")
        private String name;

        @SerializedName("hex")
        private String hex;

        public BikeColor() {}

        public BikeColor(String name, String hex) {
            this.name = name;
            this.hex = hex;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHex() {
            return hex;
        }

        public void setHex(String hex) {
            this.hex = hex;
        }
    }

    public static class Specifications {
        @SerializedName("battery")
        private String battery;

        @SerializedName("motor")
        private String motor;

        @SerializedName("range")
        private String range;

        @SerializedName("maxSpeed")
        private String maxSpeed;

        @SerializedName("weight")
        private String weight;

        @SerializedName("chargingTime")
        private String chargingTime;

        public Specifications() {}

        public String getBattery() {
            return battery;
        }

        public void setBattery(String battery) {
            this.battery = battery;
        }

        public String getMotor() {
            return motor;
        }

        public void setMotor(String motor) {
            this.motor = motor;
        }

        public String getRange() {
            return range;
        }

        public void setRange(String range) {
            this.range = range;
        }

        public String getMaxSpeed() {
            return maxSpeed;
        }

        public void setMaxSpeed(String maxSpeed) {
            this.maxSpeed = maxSpeed;
        }

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

        public String getChargingTime() {
            return chargingTime;
        }

        public void setChargingTime(String chargingTime) {
            this.chargingTime = chargingTime;
        }
    }

    public static class Rating {
        @SerializedName("average")
        private double average;

        @SerializedName("count")
        private int count;

        @SerializedName("distribution")
        private RatingDistribution distribution;

        public Rating() {}

        public double getAverage() {
            return average;
        }

        public void setAverage(double average) {
            this.average = average;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public RatingDistribution getDistribution() {
            return distribution;
        }

        public void setDistribution(RatingDistribution distribution) {
            this.distribution = distribution;
        }
    }

    public static class RatingDistribution {
        @SerializedName("5")
        private int five;

        @SerializedName("4")
        private int four;

        @SerializedName("3")
        private int three;

        @SerializedName("2")
        private int two;

        @SerializedName("1")
        private int one;

        public RatingDistribution() {}

        public int getFive() {
            return five;
        }

        public void setFive(int five) {
            this.five = five;
        }

        public int getFour() {
            return four;
        }

        public void setFour(int four) {
            this.four = four;
        }

        public int getThree() {
            return three;
        }

        public void setThree(int three) {
            this.three = three;
        }

        public int getTwo() {
            return two;
        }

        public void setTwo(int two) {
            this.two = two;
        }

        public int getOne() {
            return one;
        }

        public void setOne(int one) {
            this.one = one;
        }
    }
}
