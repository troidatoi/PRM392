package com.example.myapplication.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:5000/api/"; // Android emulator localhost
    // For real device, use your computer's IP address: "http://192.168.1.xxx:5000/api/"
    
    private static Retrofit retrofit = null;
    private static BikeApiService bikeApiService = null;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            // Create logging interceptor
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create OkHttp client
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            // Create Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static BikeApiService getBikeApiService() {
        if (bikeApiService == null) {
            bikeApiService = getRetrofit().create(BikeApiService.class);
        }
        return bikeApiService;
    }

    // Method to update base URL (useful for switching between environments)
    public static void updateBaseUrl(String newBaseUrl) {
        retrofit = null;
        bikeApiService = null;
        // Note: You would need to recreate the Retrofit instance with new URL
    }
}


