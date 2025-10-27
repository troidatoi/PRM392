package com.example.project.utils;

import android.util.Log;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkTest {
    private static final String TAG = "NetworkTest";
    
    public static void testConnection(String url) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                
                Response response = client.newCall(request).execute();
                Log.d(TAG, "Test connection to " + url + " - Status: " + response.code());
                Log.d(TAG, "Response body: " + response.body().string());
                response.close();
            } catch (IOException e) {
                Log.e(TAG, "Connection test failed: " + e.getMessage());
            }
        }).start();
    }
}
