package com.example.project.network;

import com.example.project.models.ApiResponse;
import com.example.project.models.LoginRequest;
import com.example.project.models.RegisterRequest;
import com.example.project.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ApiService {
    
    // Auth endpoints
    @POST("auth/register")
    Call<ApiResponse<User>> register(@Body RegisterRequest request);
    
    @POST("auth/login")
    Call<ApiResponse<User>> login(@Body LoginRequest request);
    
    @POST("auth/logout")
    Call<ApiResponse<Void>> logout(@Header("Authorization") String token);
    
    @GET("auth/me")
    Call<ApiResponse<User>> getMe(@Header("Authorization") String token);
    
    @PUT("auth/me")
    Call<ApiResponse<User>> updateProfile(@Header("Authorization") String token, @Body User user);
    
    @PUT("auth/me/change-password")
    Call<ApiResponse<Void>> changePassword(@Header("Authorization") String token, @Body ChangePasswordRequest request);
    
    // User management endpoints (Admin/Staff only)
    @GET("users")
    Call<ApiResponse<User[]>> getUsers(@Header("Authorization") String token);
    
    @GET("users/{id}")
    Call<ApiResponse<User>> getUser(@Header("Authorization") String token, @retrofit2.http.Path("id") String userId);
    
    @PUT("users/{id}")
    Call<ApiResponse<User>> updateUser(@Header("Authorization") String token, @retrofit2.http.Path("id") String userId, @Body User user);
    
    @retrofit2.http.DELETE("users/{id}")
    Call<ApiResponse<Void>> deleteUser(@Header("Authorization") String token, @retrofit2.http.Path("id") String userId);
    
    // Change password request model
    class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
        
        public ChangePasswordRequest() {}
        
        public ChangePasswordRequest(String currentPassword, String newPassword) {
            this.currentPassword = currentPassword;
            this.newPassword = newPassword;
        }
        
        public String getCurrentPassword() {
            return currentPassword;
        }
        
        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }
        
        public String getNewPassword() {
            return newPassword;
        }
        
        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}

