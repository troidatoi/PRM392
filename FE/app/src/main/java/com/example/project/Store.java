package com.example.project;

import com.google.gson.annotations.SerializedName;

public class Store {
    @SerializedName("_id")
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private String address;
    private String city;
    private String phone;
    private String email;
    private String description;
    @SerializedName("isActive")
    private boolean isActive;
    private OperatingHours operatingHours;
    @SerializedName("createdAt")
    private String createdAt;
    @SerializedName("updatedAt")
    private String updatedAt;
    
    // Legacy fields for backward compatibility
    private String status;
    private int productCount;
    private String ownerName;

    public Store() {}

    public Store(String id, String name, String address, String phone, String status, int productCount) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.status = status;
        this.productCount = productCount;
        this.isActive = "Hoạt động".equals(status) || "active".equals(status);
    }

    public Store(String id, String name, String address, String phone, String status, int productCount, String ownerName, String email) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.status = status;
        this.productCount = productCount;
        this.ownerName = ownerName;
        this.email = email;
        this.isActive = "Hoạt động".equals(status) || "active".equals(status);
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive || "active".equalsIgnoreCase(status) || "Hoạt động".equalsIgnoreCase(status);
    }

    // New getters and setters for BE schema fields
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public OperatingHours getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(OperatingHours operatingHours) {
        this.operatingHours = operatingHours;
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

    // Helper methods
    public String getFullAddress() {
        if (city != null && !city.isEmpty()) {
            return address + ", " + city;
        }
        return address;
    }

    public String getDisplayStatus() {
        if (status != null && !status.isEmpty()) {
            return status;
        }
        return isActive ? "Hoạt động" : "Đóng cửa";
    }
}

