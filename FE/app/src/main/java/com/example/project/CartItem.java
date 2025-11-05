package com.example.project;

public class CartItem {
    private String name;
    private String description;
    private String price;
    private int imageResId;
    private int quantity;
    private int priceValue; // Store numeric price for calculations
    private boolean isSelected; // For checkbox state
    private String itemId; // Cart item ID from API
    private String productId; // Product ID
    private String storeId; // Store ID
    private long unitPrice; // Unit price for API calls
    private String imageUrl; // Image URL from API

    public CartItem(String name, String description, String price, int imageResId, int quantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageResId = imageResId;
        this.quantity = quantity;
        this.priceValue = parsePriceFromString(price);
        this.isSelected = true; // Default selected
    }

    private int parsePriceFromString(String priceStr) {
        try {
            // Remove "VNĐ" and dots, then parse
            String cleanPrice = priceStr.replace(" VNĐ", "").replace(".", "").trim();
            return Integer.parseInt(cleanPrice);
        } catch (Exception e) {
            return 0;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
        this.priceValue = parsePriceFromString(price);
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPriceValue() {
        return priceValue;
    }

    public int getTotalPrice() {
        return priceValue * quantity;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    // Getters and setters for API IDs
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(long unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
