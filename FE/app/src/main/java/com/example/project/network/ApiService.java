package com.example.project.network;

import com.example.project.models.ApiResponse;
import com.example.project.models.Bike;
import com.example.project.models.LoginRequest;
import com.example.project.models.RegisterRequest;
import com.example.project.models.User;
import com.example.project.Store;

import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

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
    Call<ApiResponse<User>> updateProfile(@Header("Authorization") String token, @Body java.util.Map<String, Object> body);
    
    @PUT("auth/me/change-password")
    Call<ApiResponse<Void>> changePassword(@Header("Authorization") String token, @Body ChangePasswordRequest request);

    @POST("auth/forgot-password")
    Call<ApiResponse<Void>> forgotPassword(@Body java.util.Map<String, String> body);

    @PUT("auth/reset-password/{token}")
    Call<ApiResponse<User>> resetPassword(@retrofit2.http.Path("token") String token, @Body java.util.Map<String, String> body);
    
    // User management endpoints (Admin/Staff only)
    @GET("users")
    Call<ApiResponse<User[]>> getUsers(@Header("Authorization") String token);
    
    @GET("users/{id}")
    Call<ApiResponse<User>> getUser(@Header("Authorization") String token, @retrofit2.http.Path("id") String userId);
    
    @PUT("users/{id}")
    Call<ApiResponse<User>> updateUser(@Header("Authorization") String token, @retrofit2.http.Path("id") String userId, @Body User user);
    
    @retrofit2.http.DELETE("users/{id}")
    Call<ApiResponse<Void>> deleteUser(@Header("Authorization") String token, @retrofit2.http.Path("id") String userId);
    
    // Bike endpoints
    @GET("bikes")
    Call<ApiResponse<Bike[]>> getBikes(@retrofit2.http.Query("page") int page,
                                       @retrofit2.http.Query("limit") int limit,
                                       @retrofit2.http.Query("category") String category,
                                       @retrofit2.http.Query("status") String status,
                                       @retrofit2.http.Query("brand") String brand,
                                       @retrofit2.http.Query("minPrice") Double minPrice,
                                       @retrofit2.http.Query("maxPrice") Double maxPrice,
                                       @retrofit2.http.Query("search") String search,
                                       @retrofit2.http.Query("sortBy") String sortBy);
    
    @GET("bikes/{id}")
    Call<ApiResponse<Bike>> getBikeById(@retrofit2.http.Path("id") String bikeId);
    
    @Multipart
    @POST("bikes")
    Call<ApiResponse<Bike>> createBike(
        @Header("Authorization") String token,
        @Part("name") RequestBody name,
        @Part("brand") RequestBody brand,
        @Part("model") RequestBody model,
        @Part("price") RequestBody price,
        @Part("description") RequestBody description,
        @Part("stock") RequestBody stock,
        @Part("category") RequestBody category,
        @Part("status") RequestBody status,
        @Part("battery") RequestBody battery,
        @Part("motor") RequestBody motor,
        @Part("range") RequestBody range,
        @Part("maxSpeed") RequestBody maxSpeed,
        @Part("weight") RequestBody weight,
        @Part("chargingTime") RequestBody chargingTime,
        @Part("features") RequestBody features,
        @Part("warranty") RequestBody warranty,
        @Part("originalPrice") RequestBody originalPrice,
        @Part("tags") RequestBody tags,
        @Part List<MultipartBody.Part> images
    );
    
    @Multipart
    @PUT("bikes/{id}")
    Call<ApiResponse<Bike>> updateBike(
        @Header("Authorization") String token,
        @retrofit2.http.Path("id") String bikeId,
        @Part("name") RequestBody name,
        @Part("brand") RequestBody brand,
        @Part("model") RequestBody model,
        @Part("price") RequestBody price,
        @Part("description") RequestBody description,
        @Part("stock") RequestBody stock,
        @Part("category") RequestBody category,
        @Part("status") RequestBody status,
        @Part List<MultipartBody.Part> images
    );
    
    @retrofit2.http.DELETE("bikes/{id}")
    Call<ApiResponse<Void>> deleteBike(@Header("Authorization") String token, @retrofit2.http.Path("id") String bikeId);
    
    @GET("bikes/featured/list")
    Call<ApiResponse<Bike[]>> getFeaturedBikes(@retrofit2.http.Query("limit") int limit);
    
    @GET("bikes/categories/list")
    Call<ApiResponse<Object[]>> getCategories();
    
    // Store endpoints
    @GET("stores")
    Call<StoreResponse> getStores(
        @retrofit2.http.Query("city") String city,
        @retrofit2.http.Query("district") String district,
        @retrofit2.http.Query("storeType") String storeType,
        @retrofit2.http.Query("isActive") Boolean isActive,
        @retrofit2.http.Query("page") int page,
        @retrofit2.http.Query("limit") int limit,
        @retrofit2.http.Query("sort") String sort
    );
    
    @GET("stores/{id}")
    Call<ApiResponse<Store>> getStoreById(@retrofit2.http.Path("id") String storeId);
    
    @POST("stores")
    Call<ApiResponse<Store>> createStore(@Header("Authorization") String token, @Body Store store);
    
    @PUT("stores/{id}")
    Call<ApiResponse<Store>> updateStore(@Header("Authorization") String token, 
                                       @retrofit2.http.Path("id") String storeId, 
                                       @Body Store store);
    
    @retrofit2.http.DELETE("stores/{id}")
    Call<ApiResponse<Void>> deleteStore(@Header("Authorization") String token, 
                                      @retrofit2.http.Path("id") String storeId);

    @GET("locations")
    Call<ApiResponse<java.util.List<com.example.project.models.Location>>> getLocations();
    
    @POST("stores/nearby")
    Call<ApiResponse<Store[]>> getNearbyStores(@Body NearbyStoreRequest request);
    
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
    
    // Store response model for pagination
    class StoreResponse {
        private boolean success;
        private String message;
        private int count;
        private int total;
        private int page;
        private int pages;
        private List<Store> data;
        private Store singleData; // For single store response
        
        public StoreResponse() {}
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
        
        public int getTotal() {
            return total;
        }
        
        public void setTotal(int total) {
            this.total = total;
        }
        
        public int getPage() {
            return page;
        }
        
        public void setPage(int page) {
            this.page = page;
        }
        
        public int getPages() {
            return pages;
        }
        
        public void setPages(int pages) {
            this.pages = pages;
        }
        
        public List<Store> getData() {
            return data;
        }
        
        public void setData(List<Store> data) {
            this.data = data;
        }
        
        public Store getSingleData() {
            return singleData;
        }
        
        public void setSingleData(Store singleData) {
            this.singleData = singleData;
        }
    }
    
    // Nearby store request model
    class NearbyStoreRequest {
        private double lat;
        private double lng;
        private double radius;
        private int limit;
        
        public NearbyStoreRequest() {}
        
        public NearbyStoreRequest(double lat, double lng, double radius, int limit) {
            this.lat = lat;
            this.lng = lng;
            this.radius = radius;
            this.limit = limit;
        }
        
        public double getLat() {
            return lat;
        }
        
        public void setLat(double lat) {
            this.lat = lat;
        }
        
        public double getLng() {
            return lng;
        }
        
        public void setLng(double lng) {
            this.lng = lng;
        }
        
        public double getRadius() {
            return radius;
        }
        
        public void setRadius(double radius) {
            this.radius = radius;
        }
        
        public int getLimit() {
            return limit;
        }
        
        public void setLimit(int limit) {
            this.limit = limit;
        }
    }
}
