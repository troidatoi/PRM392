package com.example.project.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.project.models.User;
import com.google.gson.Gson;

public class AuthManager {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER = "user";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    private static AuthManager instance;
    private SharedPreferences preferences;
    private Gson gson;
    
    private AuthManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context.getApplicationContext());
        }
        return instance;
    }
    
    public void saveAuthData(String token, User user) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER, gson.toJson(user));
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
    
    public void clearAuthData() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_USER);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }
    
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }
    
    public String getAuthHeader() {
        String token = getToken();
        return token != null ? "Bearer " + token : null;
    }
    
    public User getCurrentUser() {
        String userJson = preferences.getString(KEY_USER, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }
    
    public void updateUser(User user) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER, gson.toJson(user));
        editor.apply();
    }
    
    public boolean isAdmin() {
        User user = getCurrentUser();
        return user != null && "admin".equals(user.getRole());
    }
    
    public boolean isStaff() {
        User user = getCurrentUser();
        return user != null && ("admin".equals(user.getRole()) || "staff".equals(user.getRole()));
    }
}

