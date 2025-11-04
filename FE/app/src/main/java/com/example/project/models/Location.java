package com.example.project.models;

import java.util.List;

public class Location {
    private String name;
    private String code;  // Changed from int to String
    private String division_type;  // Added to match JSON
    private String codename;       // Added to match JSON
    private List<District> districts;

    public String getName() { return name; }
    public String getCode() { return code; }  // Updated return type
    public String getDivisionType() { return division_type; }
    public String getCodename() { return codename; }
    public List<District> getDistricts() { return districts; }

    public static class District {
        private String name;
        private String code;  // Changed from int to String
        private String division_type;  // Added to match JSON
        private String codename;       // Added to match JSON

        public String getName() { return name; }
        public String getCode() { return code; }  // Updated return type
        public String getDivisionType() { return division_type; }
        public String getCodename() { return codename; }
    }
}
