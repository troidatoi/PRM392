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
 * Service để cập nhật payment badge khi app đóng
 * Kiểm tra và hiển thị thông báo thanh toán chưa hoàn tất
 */
public class PaymentBadgeService extends Service {
    private static final String TAG = "PaymentBadgeService";

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
     * Lấy danh sách payment pending từ API và cập nhật badge
     */
    private void updateBadgeFromApi() {
        AuthManager auth = AuthManager.getInstance(this);
        User user = auth.getCurrentUser();
        
        if (user == null) {
            // Không có user đăng nhập, xóa badge
            NotificationHelper.removePaymentBadge(this);
            return;
        }

        ApiService api = RetrofitClient.getInstance().getApiService();
        
        api.getPendingPayments(auth.getAuthHeader(), user.getId())
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
                            
                            int pendingCount = 0;
                            String firstOrderId = null;
                            
                            if (dataMap != null) {
                                // Lấy count
                                if (dataMap.get("count") instanceof Number) {
                                    pendingCount = ((Number) dataMap.get("count")).intValue();
                                }
                                
                                // Lấy orderId đầu tiên nếu có
                                java.util.List payments = (java.util.List) dataMap.get("payments");
                                if (payments != null && !payments.isEmpty()) {
                                    java.util.Map firstPayment = (java.util.Map) payments.get(0);
                                    if (firstPayment != null) {
                                        java.util.Map orderMap = (java.util.Map) firstPayment.get("order");
                                        if (orderMap != null && orderMap.get("_id") != null) {
                                            firstOrderId = orderMap.get("_id").toString();
                                        }
                                    }
                                }
                            }
                            
                            // Cập nhật badge
                            NotificationHelper.updatePaymentBadge(PaymentBadgeService.this, pendingCount, firstOrderId);
                            Log.d(TAG, "Payment badge updated: " + pendingCount);
                            
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing payment data: " + e.getMessage(), e);
                            NotificationHelper.removePaymentBadge(PaymentBadgeService.this);
                        }
                    } else {
                        NotificationHelper.removePaymentBadge(PaymentBadgeService.this);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<ApiResponse<Object>> call, Throwable t) {
                    Log.e(TAG, "Error fetching pending payments: " + t.getMessage());
                    // Không cập nhật badge nếu lỗi
                }
            });
    }
}

