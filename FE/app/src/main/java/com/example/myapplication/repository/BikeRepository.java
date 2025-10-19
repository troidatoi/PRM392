package com.example.myapplication.repository;

import com.example.myapplication.api.ApiClient;
import com.example.myapplication.api.BikeApiService;
import com.example.myapplication.model.ApiResponse;
import com.example.myapplication.model.Bike;
import com.example.myapplication.model.Category;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BikeRepository {
    private BikeApiService bikeApiService;

    public BikeRepository() {
        bikeApiService = ApiClient.getBikeApiService();
    }

    // Get all bikes with callback
    public void getBikes(BikeCallback<List<Bike>> callback) {
        getBikes(null, null, null, null, null, null, null, null, null, callback);
    }

    public void getBikes(Integer page, Integer limit, String category, String status,
                        Double minPrice, Double maxPrice, String brand, String search,
                        String sortBy, BikeCallback<List<Bike>> callback) {
        Call<ApiResponse<List<Bike>>> call = bikeApiService.getBikes(
                page, limit, category, status, minPrice, maxPrice, brand, search, sortBy);
        
        call.enqueue(new Callback<ApiResponse<List<Bike>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Bike>>> call, Response<ApiResponse<List<Bike>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to fetch bikes: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Bike>>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Get bike by ID
    public void getBikeById(String id, BikeCallback<Bike> callback) {
        Call<ApiResponse<Bike>> call = bikeApiService.getBikeById(id);
        
        call.enqueue(new Callback<ApiResponse<Bike>>() {
            @Override
            public void onResponse(Call<ApiResponse<Bike>> call, Response<ApiResponse<Bike>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to fetch bike: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Bike>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Create new bike
    public void createBike(Bike bike, BikeCallback<Bike> callback) {
        Call<ApiResponse<Bike>> call = bikeApiService.createBike(bike);
        
        call.enqueue(new Callback<ApiResponse<Bike>>() {
            @Override
            public void onResponse(Call<ApiResponse<Bike>> call, Response<ApiResponse<Bike>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to create bike: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Bike>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Update bike
    public void updateBike(String id, Bike bike, BikeCallback<Bike> callback) {
        Call<ApiResponse<Bike>> call = bikeApiService.updateBike(id, bike);
        
        call.enqueue(new Callback<ApiResponse<Bike>>() {
            @Override
            public void onResponse(Call<ApiResponse<Bike>> call, Response<ApiResponse<Bike>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to update bike: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Bike>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Delete bike
    public void deleteBike(String id, BikeCallback<Void> callback) {
        Call<ApiResponse<Void>> call = bikeApiService.deleteBike(id);
        
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to delete bike: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Get featured bikes
    public void getFeaturedBikes(BikeCallback<List<Bike>> callback) {
        getFeaturedBikes(null, callback);
    }

    public void getFeaturedBikes(Integer limit, BikeCallback<List<Bike>> callback) {
        Call<ApiResponse<List<Bike>>> call = bikeApiService.getFeaturedBikes(limit);
        
        call.enqueue(new Callback<ApiResponse<List<Bike>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Bike>>> call, Response<ApiResponse<List<Bike>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to fetch featured bikes: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Bike>>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Get categories
    public void getCategories(BikeCallback<List<Category>> callback) {
        Call<ApiResponse<List<Category>>> call = bikeApiService.getCategories();
        
        call.enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Category>>> call, Response<ApiResponse<List<Category>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to fetch categories: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Health check
    public void healthCheck(BikeCallback<Object> callback) {
        Call<ApiResponse<Object>> call = bikeApiService.healthCheck();
        
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Health check failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Callback interface
    public interface BikeCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }
}