package com.example.project;

public class Order {
    private String orderId;
    private String orderNumber;
    private String orderDate;
    private String status;
    private String items;
    private String totalAmount;
    private String statusColor;

    public Order(String orderId, String orderNumber, String orderDate, String status, String items, String totalAmount, String statusColor) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.status = status;
        this.items = items;
        this.totalAmount = totalAmount;
        this.statusColor = statusColor;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
    }

    public static String mapStatusText(String st) {
        if(st==null) return "";
        switch(st.toLowerCase()) {
            case "pending": return "Chờ xác nhận";
            case "confirmed": return "Đã xác nhận";
            case "shipped": return "Đang giao hàng";
            case "delivered": return "✓ Đã giao";
            case "cancelled": return "Đã hủy";
            default: return st;
        }
    }
    public static String mapStatusColor(String st) {
        if(st==null) return "#2196F3";
        switch(st.toLowerCase()) {
            case "pending": return "#FFC107";
            case "confirmed": return "#64B5F6";
            case "shipped": return "#2196F3";
            case "delivered": return "#4CAF50";
            case "cancelled": return "#F44336";
            default: return "#2196F3";
        }
    }
}

