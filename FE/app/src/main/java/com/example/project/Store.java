package com.example.project;

import com.google.gson.annotations.SerializedName;

public class Store implements java.io.Serializable {
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
    @SerializedName("isOpenNow")
    private Boolean isOpenNow; // Giá trị từ API, null nếu chưa được set
    private OperatingHours operatingHours;
    @SerializedName("createdAt")
    private String createdAt;
    @SerializedName("updatedAt")
    private String updatedAt;
    
    // Legacy fields for backward compatibility
    private String status;
    private int productCount;
    private String ownerName;
    
    // Distance from user location (in km)
    private Double distance;

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
            // If status contains distance (like "1.2 km"), return it
            if (status.contains("km")) {
                return status;
            }
            return status;
        }
        return isActive ? "Hoạt động" : "Đóng cửa";
    }
    
    public Double getDistance() {
        return distance;
    }
    
    public void setDistance(Double distance) {
        this.distance = distance;
    }
    
    public String getDistanceText() {
        if (distance != null) {
            if (distance < 1) {
                // Less than 1km, show in meters
                return String.format("%.0f m", distance * 1000);
            } else {
                // Show in km
                return String.format("%.2f km", distance);
            }
        }
        return null;
    }
    
    /**
     * Kiểm tra xem cửa hàng có đang mở hay không
     * Ưu tiên dùng giá trị từ API, nếu không có thì tính toán dựa trên operatingHours
     */
    public boolean isOpenNow() {
        // Nếu API đã trả về giá trị isOpenNow, dùng giá trị đó
        if (isOpenNow != null) {
            return isOpenNow;
        }
        
        // Fallback: tính toán dựa trên operatingHours
        if (operatingHours == null) {
            return isActive;
        }
        
        // Lấy thời gian hiện tại theo timezone Việt Nam (GMT+7)
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        java.util.TimeZone vietnamTimeZone = java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        calendar.setTimeZone(vietnamTimeZone);
        
        int dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK);
        
        // Convert Calendar day to our day name (Calendar: Sunday=1, Monday=2, ..., Saturday=7)
        String dayName = getDayNameFromCalendar(dayOfWeek);
        OperatingHours.DaySchedule daySchedule = operatingHours.getScheduleForDay(dayName);
        
        if (daySchedule == null || !daySchedule.isOpen()) {
            return false;
        }
        
        // Get current time in minutes from 00:00
        int currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(java.util.Calendar.MINUTE);
        int currentMinutes = currentHour * 60 + currentMinute;
        
        // Parse open and close times to minutes
        String openTime = daySchedule.getOpen();
        String closeTime = daySchedule.getClose();
        
        if (openTime == null || closeTime == null) {
            return false;
        }
        
        // Convert time string (HH:mm) to minutes
        int openMinutes = timeToMinutes(openTime);
        int closeMinutes = timeToMinutes(closeTime);
        
        // Xử lý trường hợp đóng cửa sau nửa đêm (ví dụ: 22:00 - 02:00)
        if (closeMinutes < openMinutes) {
            // Cửa hàng mở qua đêm
            return currentMinutes >= openMinutes || currentMinutes <= closeMinutes;
        }
        
        return currentMinutes >= openMinutes && currentMinutes <= closeMinutes;
    }
    
    /**
     * Chuyển đổi thời gian từ format HH:mm sang phút từ 00:00
     */
    private int timeToMinutes(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return 0;
        }
        String[] parts = timeStr.split(":");
        if (parts.length != 2) {
            return 0;
        }
        try {
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return hours * 60 + minutes;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Lấy text hiển thị operating hours cho ngày hiện tại
     */
    public String getOperatingHoursText() {
        if (operatingHours == null) {
            return null;
        }
        
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        java.util.TimeZone vietnamTimeZone = java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        calendar.setTimeZone(vietnamTimeZone);
        int dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK);
        String dayName = getDayNameFromCalendar(dayOfWeek);
        OperatingHours.DaySchedule daySchedule = operatingHours.getScheduleForDay(dayName);
        
        if (daySchedule == null) {
            return null;
        }
        
        return daySchedule.getFormattedHours();
    }
    
    /**
     * Lấy text hiển thị giờ hoạt động chi tiết cho tất cả các ngày trong tuần
     */
    public String getDetailedOperatingHoursText() {
        if (operatingHours == null) {
            return null;
        }
        
        java.util.List<String> dayNames = java.util.Arrays.asList(
            "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"
        );
        java.util.List<String> dayKeys = java.util.Arrays.asList(
            "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"
        );
        
        java.util.List<String> hoursList = new java.util.ArrayList<>();
        
        for (int i = 0; i < dayNames.size(); i++) {
            String dayKey = dayKeys.get(i);
            String dayName = dayNames.get(i);
            OperatingHours.DaySchedule schedule = operatingHours.getScheduleForDay(dayKey);
            
            if (schedule != null && schedule.isOpen() && schedule.getOpen() != null && schedule.getClose() != null) {
                hoursList.add(dayName + ": " + schedule.getOpen() + " - " + schedule.getClose());
            } else {
                hoursList.add(dayName + ": Đóng cửa");
            }
        }
        
        return java.lang.String.join("\n", hoursList);
    }
    
    /**
     * Convert Calendar day of week to day name
     * Calendar: Sunday=1, Monday=2, Tuesday=3, Wednesday=4, Thursday=5, Friday=6, Saturday=7
     */
    private String getDayNameFromCalendar(int calendarDay) {
        switch (calendarDay) {
            case java.util.Calendar.MONDAY:
                return "monday";
            case java.util.Calendar.TUESDAY:
                return "tuesday";
            case java.util.Calendar.WEDNESDAY:
                return "wednesday";
            case java.util.Calendar.THURSDAY:
                return "thursday";
            case java.util.Calendar.FRIDAY:
                return "friday";
            case java.util.Calendar.SATURDAY:
                return "saturday";
            case java.util.Calendar.SUNDAY:
                return "sunday";
            default:
                return "monday";
        }
    }
}

