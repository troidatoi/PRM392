package com.example.myapplication.api;

import com.example.myapplication.model.ApiResponse;
import com.example.myapplication.model.Bike;
import com.example.myapplication.model.Category;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BikeApiService {
    
    // Get all bikes with filtering and pagination
    @GET("bikes")
    Call<ApiResponse<List<Bike>>> getBikes(
        @Query("page") Integer page,
        @Query("limit") Integer limit,
        @Query("category") String category,
        @Query("status") String status,
        @Query("minPrice") Double minPrice,
        @Query("maxPrice") Double maxPrice,
        @Query("brand") String brand,
        @Query("search") String search,
        @Query("sortBy") String sortBy
    );

    // Get single bike by ID
    @GET("bikes/{id}")
    Call<ApiResponse<Bike>> getBikeById(@Path("id") String id);

    // Create new bike
    @POST("bikes")
    Call<ApiResponse<Bike>> createBike(@Body Bike bike);

    // Update bike
    @PUT("bikes/{id}")
    Call<ApiResponse<Bike>> updateBike(@Path("id") String id, @Body Bike bike);

    // Delete bike
    @DELETE("bikes/{id}")
    Call<ApiResponse<Void>> deleteBike(@Path("id") String id);

    // Get featured bikes
    @GET("bikes/featured/list")
    Call<ApiResponse<List<Bike>>> getFeaturedBikes(@Query("limit") Integer limit);

    // Get all categories with count
    @GET("bikes/categories/list")
    Call<ApiResponse<List<Category>>> getCategories();

    // Health check
    @GET("health")
    Call<ApiResponse<Object>> healthCheck();
}


