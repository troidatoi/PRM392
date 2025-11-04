package com.example.project;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.models.ApiResponse;
import com.example.project.utils.AuthManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentResultActivity extends AppCompatActivity {

    private TextView tvStatus;
    private String orderCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        tvStatus = findViewById(R.id.tvStatus);

        // Lấy orderCode từ intent hoặc deep link
        orderCode = getIntent().getStringExtra("orderCode");
        
        // Nếu không có trong extra, thử lấy từ data URI (deep link)
        if (orderCode == null && getIntent().getData() != null) {
            orderCode = getIntent().getData().getQueryParameter("orderCode");
        }
        
        // Nếu vẫn không có, thử lấy từ query string trong URL
        if (orderCode == null) {
            android.net.Uri uri = getIntent().getData();
            if (uri != null) {
                orderCode = uri.getQueryParameter("orderCode");
            }
        }

        if (orderCode != null) {
            verifyPayment(orderCode);
        } else {
            showError("Không tìm thấy mã đơn hàng");
        }
    }

    private void verifyPayment(String orderCode) {
        AuthManager auth = AuthManager.getInstance(this);
        ApiService api = RetrofitClient.getInstance().getApiService();

        tvStatus.setText("Đang xác minh thanh toán...");

        api.verifyPayment(auth.getAuthHeader(), orderCode).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    try {
                        Object data = response.body().getData();
                        if (data instanceof java.util.Map) {
                            java.util.Map paymentData = (java.util.Map) data;
                            Object isPaid = paymentData.get("isPaid");
                            Object payosStatus = paymentData.get("payosStatus");

                            if (Boolean.TRUE.equals(isPaid) || "PAID".equals(payosStatus)) {
                                showSuccess("Thanh toán thành công!");
                            } else {
                                showPending("Đang xử lý thanh toán...");
                            }
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                // Kiểm tra lại sau 2 giây (có thể webhook chưa xử lý xong)
                new android.os.Handler().postDelayed(() -> {
                    verifyPayment(orderCode);
                }, 2000);
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                showError("Lỗi xác minh thanh toán: " + t.getMessage());
            }
        });
    }

    private void showSuccess(String message) {
        tvStatus.setText(message);
        tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));

        new AlertDialog.Builder(this)
            .setTitle("Thanh toán thành công")
            .setMessage(message + "\n\nĐơn hàng của bạn đã được xác nhận.")
            .setPositiveButton("OK", (dialog, which) -> {
                // Quay về màn hình đơn hàng hoặc home
                finish();
                // Có thể startActivity(new Intent(this, OrderHistoryActivity.class));
            })
            .setCancelable(false)
            .show();
    }

    private void showPending(String message) {
        tvStatus.setText(message);
        tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark, null));

        Toast.makeText(this, "Vui lòng đợi trong giây lát...", Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        tvStatus.setText(message);
        tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));

        new AlertDialog.Builder(this)
            .setTitle("Lỗi")
            .setMessage(message)
            .setPositiveButton("OK", (dialog, which) -> finish())
            .show();
    }
}

