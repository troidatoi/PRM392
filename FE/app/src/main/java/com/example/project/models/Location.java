package com.example.project.models;

import java.util.List;

public class Location {
    private String name;
    private int code;
    private List<District> districts;

    public String getName() { return name; }
    public int getCode() { return code; }
    public List<District> getDistricts() { return districts; }

    public static class District {
        private String name;
        private int code;

        public String getName() { return name; }
        public int getCode() { return code; }
    }
}
