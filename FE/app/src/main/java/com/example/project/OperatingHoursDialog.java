package com.example.project;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

public class OperatingHoursDialog extends Dialog {
    
    private Context context;
    private OperatingHours operatingHours;
    private OnOperatingHoursUpdatedListener listener;
    
    // Views for each day
    private Map<String, DayViews> dayViewsMap = new HashMap<>();
    
    private Button btnCancel, btnSave;
    
    public interface OnOperatingHoursUpdatedListener {
        void onOperatingHoursUpdated(OperatingHours operatingHours);
    }
    
    public OperatingHoursDialog(@NonNull Context context, OperatingHours operatingHours, OnOperatingHoursUpdatedListener listener) {
        super(context);
        this.context = context;
        this.operatingHours = operatingHours != null ? operatingHours : new OperatingHours();
        this.listener = listener;
    }
    
    private static class DayViews {
        TextView tvDayName;
        CheckBox cbIsOpen;
        TextInputEditText etOpenTime;
        TextInputEditText etCloseTime;
        LinearLayout llTimeContainer;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_operating_hours);
        setCancelable(true);
        
        initViews();
        populateFields();
        setupListeners();
    }
    
    private void initViews() {
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
        
        // Initialize views for each day
        DayViews mondayViews = new DayViews();
        mondayViews.tvDayName = findViewById(R.id.tvMonday);
        mondayViews.cbIsOpen = findViewById(R.id.cbMonday);
        mondayViews.etOpenTime = findViewById(R.id.etMondayOpen);
        mondayViews.etCloseTime = findViewById(R.id.etMondayClose);
        mondayViews.llTimeContainer = findViewById(R.id.llMondayContainer);
        dayViewsMap.put("monday", mondayViews);
        
        DayViews tuesdayViews = new DayViews();
        tuesdayViews.tvDayName = findViewById(R.id.tvTuesday);
        tuesdayViews.cbIsOpen = findViewById(R.id.cbTuesday);
        tuesdayViews.etOpenTime = findViewById(R.id.etTuesdayOpen);
        tuesdayViews.etCloseTime = findViewById(R.id.etTuesdayClose);
        tuesdayViews.llTimeContainer = findViewById(R.id.llTuesdayContainer);
        dayViewsMap.put("tuesday", tuesdayViews);
        
        DayViews wednesdayViews = new DayViews();
        wednesdayViews.tvDayName = findViewById(R.id.tvWednesday);
        wednesdayViews.cbIsOpen = findViewById(R.id.cbWednesday);
        wednesdayViews.etOpenTime = findViewById(R.id.etWednesdayOpen);
        wednesdayViews.etCloseTime = findViewById(R.id.etWednesdayClose);
        wednesdayViews.llTimeContainer = findViewById(R.id.llWednesdayContainer);
        dayViewsMap.put("wednesday", wednesdayViews);
        
        DayViews thursdayViews = new DayViews();
        thursdayViews.tvDayName = findViewById(R.id.tvThursday);
        thursdayViews.cbIsOpen = findViewById(R.id.cbThursday);
        thursdayViews.etOpenTime = findViewById(R.id.etThursdayOpen);
        thursdayViews.etCloseTime = findViewById(R.id.etThursdayClose);
        thursdayViews.llTimeContainer = findViewById(R.id.llThursdayContainer);
        dayViewsMap.put("thursday", thursdayViews);
        
        DayViews fridayViews = new DayViews();
        fridayViews.tvDayName = findViewById(R.id.tvFriday);
        fridayViews.cbIsOpen = findViewById(R.id.cbFriday);
        fridayViews.etOpenTime = findViewById(R.id.etFridayOpen);
        fridayViews.etCloseTime = findViewById(R.id.etFridayClose);
        fridayViews.llTimeContainer = findViewById(R.id.llFridayContainer);
        dayViewsMap.put("friday", fridayViews);
        
        DayViews saturdayViews = new DayViews();
        saturdayViews.tvDayName = findViewById(R.id.tvSaturday);
        saturdayViews.cbIsOpen = findViewById(R.id.cbSaturday);
        saturdayViews.etOpenTime = findViewById(R.id.etSaturdayOpen);
        saturdayViews.etCloseTime = findViewById(R.id.etSaturdayClose);
        saturdayViews.llTimeContainer = findViewById(R.id.llSaturdayContainer);
        dayViewsMap.put("saturday", saturdayViews);
        
        DayViews sundayViews = new DayViews();
        sundayViews.tvDayName = findViewById(R.id.tvSunday);
        sundayViews.cbIsOpen = findViewById(R.id.cbSunday);
        sundayViews.etOpenTime = findViewById(R.id.etSundayOpen);
        sundayViews.etCloseTime = findViewById(R.id.etSundayClose);
        sundayViews.llTimeContainer = findViewById(R.id.llSundayContainer);
        dayViewsMap.put("sunday", sundayViews);
    }
    
    private void populateFields() {
        // Populate Monday
        populateDay("monday", operatingHours.getMonday());
        populateDay("tuesday", operatingHours.getTuesday());
        populateDay("wednesday", operatingHours.getWednesday());
        populateDay("thursday", operatingHours.getThursday());
        populateDay("friday", operatingHours.getFriday());
        populateDay("saturday", operatingHours.getSaturday());
        populateDay("sunday", operatingHours.getSunday());
    }
    
    private void populateDay(String day, OperatingHours.DaySchedule schedule) {
        DayViews views = dayViewsMap.get(day);
        if (views == null) return;
        
        if (schedule != null) {
            if (views.cbIsOpen != null) {
                views.cbIsOpen.setChecked(schedule.isOpen());
            }
            if (views.etOpenTime != null && schedule.getOpen() != null) {
                views.etOpenTime.setText(schedule.getOpen());
            }
            if (views.etCloseTime != null && schedule.getClose() != null) {
                views.etCloseTime.setText(schedule.getClose());
            }
        } else {
            // Default values
            if (views.cbIsOpen != null) {
                views.cbIsOpen.setChecked(true);
            }
            if (views.etOpenTime != null) {
                views.etOpenTime.setText("08:00");
            }
            if (views.etCloseTime != null) {
                views.etCloseTime.setText("22:00");
            }
        }
        
        // Update time container visibility based on isOpen
        updateTimeContainerVisibility(views);
        
        // Add listener to checkbox
        if (views.cbIsOpen != null) {
            views.cbIsOpen.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateTimeContainerVisibility(views);
            });
        }
    }
    
    private void updateTimeContainerVisibility(DayViews views) {
        if (views.llTimeContainer != null && views.cbIsOpen != null) {
            views.llTimeContainer.setVisibility(views.cbIsOpen.isChecked() ? android.view.View.VISIBLE : android.view.View.GONE);
        }
    }
    
    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnSave.setOnClickListener(v -> {
            if (validateAndSave()) {
                if (listener != null) {
                    listener.onOperatingHoursUpdated(operatingHours);
                }
                dismiss();
            }
        });
    }
    
    private boolean validateAndSave() {
        String[] days = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        
        for (String day : days) {
            DayViews views = dayViewsMap.get(day);
            if (views == null) continue;
            
            boolean isOpen = views.cbIsOpen != null && views.cbIsOpen.isChecked();
            
            if (isOpen) {
                // Validate time format if open
                String openTime = views.etOpenTime != null ? views.etOpenTime.getText().toString().trim() : "";
                String closeTime = views.etCloseTime != null ? views.etCloseTime.getText().toString().trim() : "";
                
                if (TextUtils.isEmpty(openTime) || !isValidTimeFormat(openTime)) {
                    Toast.makeText(context, "Giờ mở cửa không hợp lệ cho " + getDayName(day) + " (định dạng: HH:mm)", Toast.LENGTH_SHORT).show();
                    if (views.etOpenTime != null) views.etOpenTime.requestFocus();
                    return false;
                }
                
                if (TextUtils.isEmpty(closeTime) || !isValidTimeFormat(closeTime)) {
                    Toast.makeText(context, "Giờ đóng cửa không hợp lệ cho " + getDayName(day) + " (định dạng: HH:mm)", Toast.LENGTH_SHORT).show();
                    if (views.etCloseTime != null) views.etCloseTime.requestFocus();
                    return false;
                }
                
                // Check if close time is after open time
                if (compareTime(openTime, closeTime) >= 0) {
                    Toast.makeText(context, "Giờ đóng cửa phải sau giờ mở cửa cho " + getDayName(day), Toast.LENGTH_SHORT).show();
                    if (views.etCloseTime != null) views.etCloseTime.requestFocus();
                    return false;
                }
                
                // Save to operatingHours
                OperatingHours.DaySchedule schedule = getDaySchedule(day);
                schedule.setOpen(openTime); // Set open time (String)
                schedule.setClose(closeTime);
                schedule.setIsOpen(isOpen); // Set isOpen flag (boolean)
            } else {
                // Store closed state
                OperatingHours.DaySchedule schedule = getDaySchedule(day);
                schedule.setIsOpen(false); // Set isOpen flag to false
                schedule.setOpen(null); // Clear open time (String)
                schedule.setClose(null); // Clear close time
            }
        }
        
        return true;
    }
    
    private OperatingHours.DaySchedule getDaySchedule(String day) {
        OperatingHours.DaySchedule schedule = null;
        switch (day) {
            case "monday":
                schedule = operatingHours.getMonday();
                if (schedule == null) {
                    schedule = new OperatingHours.DaySchedule();
                    operatingHours.setMonday(schedule);
                }
                break;
            case "tuesday":
                schedule = operatingHours.getTuesday();
                if (schedule == null) {
                    schedule = new OperatingHours.DaySchedule();
                    operatingHours.setTuesday(schedule);
                }
                break;
            case "wednesday":
                schedule = operatingHours.getWednesday();
                if (schedule == null) {
                    schedule = new OperatingHours.DaySchedule();
                    operatingHours.setWednesday(schedule);
                }
                break;
            case "thursday":
                schedule = operatingHours.getThursday();
                if (schedule == null) {
                    schedule = new OperatingHours.DaySchedule();
                    operatingHours.setThursday(schedule);
                }
                break;
            case "friday":
                schedule = operatingHours.getFriday();
                if (schedule == null) {
                    schedule = new OperatingHours.DaySchedule();
                    operatingHours.setFriday(schedule);
                }
                break;
            case "saturday":
                schedule = operatingHours.getSaturday();
                if (schedule == null) {
                    schedule = new OperatingHours.DaySchedule();
                    operatingHours.setSaturday(schedule);
                }
                break;
            case "sunday":
                schedule = operatingHours.getSunday();
                if (schedule == null) {
                    schedule = new OperatingHours.DaySchedule();
                    operatingHours.setSunday(schedule);
                }
                break;
        }
        return schedule;
    }
    
    private String getDayName(String day) {
        switch (day) {
            case "monday": return "Thứ 2";
            case "tuesday": return "Thứ 3";
            case "wednesday": return "Thứ 4";
            case "thursday": return "Thứ 5";
            case "friday": return "Thứ 6";
            case "saturday": return "Thứ 7";
            case "sunday": return "Chủ nhật";
            default: return day;
        }
    }
    
    private boolean isValidTimeFormat(String time) {
        // Format: HH:mm (24-hour format)
        return time.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
    }
    
    private int compareTime(String time1, String time2) {
        // Compare two time strings in HH:mm format
        String[] parts1 = time1.split(":");
        String[] parts2 = time2.split(":");
        
        int hour1 = Integer.parseInt(parts1[0]);
        int minute1 = Integer.parseInt(parts1[1]);
        int hour2 = Integer.parseInt(parts2[0]);
        int minute2 = Integer.parseInt(parts2[1]);
        
        int totalMinutes1 = hour1 * 60 + minute1;
        int totalMinutes2 = hour2 * 60 + minute2;
        
        return Integer.compare(totalMinutes1, totalMinutes2);
    }
}

