package com.example.project;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.project.utils.AuthManager;
import com.example.project.utils.NotificationHelper;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.models.ApiResponse;
import com.example.project.models.User;

/**
 * Service để cập nhật badge khi app đóng
 * Có thể được gọi để đồng bộ badge từ server khi app ở background
 */
public class CartBadgeService extends Service {
    private static final String TAG = "CartBadgeService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Cập nhật badge từ API
        updateBadgeFromApi();
        
        // Không cần giữ service chạy liên tục
        // Stop self sau khi hoàn thành
        stopSelf();
        
        return START_NOT_STICKY;
    }

    /**
     * Lấy số lượng sản phẩm trong giỏ hàng từ API và cập nhật badge
     */
    private void updateBadgeFromApi() {
        AuthManager auth = AuthManager.getInstance(this);
        User user = auth.getCurrentUser();
        
        if (user == null) {
            // Không có user đăng nhập, xóa badge
            NotificationHelper.removeCartBadge(this);
            return;
        }

        ApiService api = RetrofitClient.getInstance().getApiService();
        
        api.getCartByUser(auth.getAuthHeader(), user.getId())
            .enqueue(new retrofit2.Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(
                    retrofit2.Call<ApiResponse<Object>> call,
                    retrofit2.Response<ApiResponse<Object>> response
                ) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        try {
                            Object data = response.body().getData();
                            java.util.Map dataMap = (java.util.Map) data;
                            java.util.Map cartMap = (java.util.Map) dataMap.get("cart");
                            
                            int itemCount = 0;
                            if (cartMap != null && cartMap.get("itemCount") instanceof Number) {
                                itemCount = ((Number) cartMap.get("itemCount")).intValue();
                            }
                            
                            // Cập nhật badge
                            NotificationHelper.updateCartBadge(CartBadgeService.this, itemCount);
                            Log.d(TAG, "Badge updated: " + itemCount);
                            
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing cart data: " + e.getMessage(), e);
                            NotificationHelper.removeCartBadge(CartBadgeService.this);
                        }
                    } else {
                        NotificationHelper.removeCartBadge(CartBadgeService.this);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<ApiResponse<Object>> call, Throwable t) {
                    Log.e(TAG, "Error fetching cart: " + t.getMessage());
                    // Không cập nhật badge nếu lỗi
                }
            });
    }
}

