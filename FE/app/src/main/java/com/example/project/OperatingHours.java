package com.example.project;

import com.google.gson.annotations.SerializedName;

public class OperatingHours {
    private DaySchedule monday;
    private DaySchedule tuesday;
    private DaySchedule wednesday;
    private DaySchedule thursday;
    private DaySchedule friday;
    private DaySchedule saturday;
    private DaySchedule sunday;

    public OperatingHours() {}

    // Getters and Setters
    public DaySchedule getMonday() {
        return monday;
    }

    public void setMonday(DaySchedule monday) {
        this.monday = monday;
    }

    public DaySchedule getTuesday() {
        return tuesday;
    }

    public void setTuesday(DaySchedule tuesday) {
        this.tuesday = tuesday;
    }

    public DaySchedule getWednesday() {
        return wednesday;
    }

    public void setWednesday(DaySchedule wednesday) {
        this.wednesday = wednesday;
    }

    public DaySchedule getThursday() {
        return thursday;
    }

    public void setThursday(DaySchedule thursday) {
        this.thursday = thursday;
    }

    public DaySchedule getFriday() {
        return friday;
    }

    public void setFriday(DaySchedule friday) {
        this.friday = friday;
    }

    public DaySchedule getSaturday() {
        return saturday;
    }

    public void setSaturday(DaySchedule saturday) {
        this.saturday = saturday;
    }

    public DaySchedule getSunday() {
        return sunday;
    }

    public void setSunday(DaySchedule sunday) {
        this.sunday = sunday;
    }

    // Helper method to get schedule for a specific day
    public DaySchedule getScheduleForDay(String dayName) {
        switch (dayName.toLowerCase()) {
            case "monday":
                return monday;
            case "tuesday":
                return tuesday;
            case "wednesday":
                return wednesday;
            case "thursday":
                return thursday;
            case "friday":
                return friday;
            case "saturday":
                return saturday;
            case "sunday":
                return sunday;
            default:
                return null;
        }
    }

    // Inner class for day schedule
    public static class DaySchedule {
        private String open;
        private String close;
        @SerializedName("isOpen")
        private boolean isOpen;

        public DaySchedule() {}

        public DaySchedule(String open, String close, boolean isOpen) {
            this.open = open;
            this.close = close;
            this.isOpen = isOpen;
        }

        // Getters and Setters
        public String getOpen() {
            return open;
        }

        public void setOpen(String open) {
            this.open = open;
        }

        public String getClose() {
            return close;
        }

        public void setClose(String close) {
            this.close = close;
        }

        public boolean isOpen() {
            return isOpen;
        }

        public void setIsOpen(boolean isOpen) {
            this.isOpen = isOpen;
        }
        
        // Legacy method for backward compatibility
        @Deprecated
        public void setOpen(boolean open) {
            this.isOpen = open;
        }

        // Helper method to get formatted hours
        public String getFormattedHours() {
            if (!isOpen) {
                return "Đóng cửa";
            }
            return open + " - " + close;
        }
    }
}
