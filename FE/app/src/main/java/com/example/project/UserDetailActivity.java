package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.project.models.ApiResponse;
import com.example.project.models.User;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDetailActivity extends AppCompatActivity {

    private CardView btnBack;
    private TextView tvTitle, tvUsername, tvEmail, tvPhoneNumber, tvAddress, tvRole, tvStatus, tvCreatedAt, tvLastLogin;
    private ImageView imgAvatar, imgStatus;
    private ProgressBar progressBar;
    private CardView cardViewUserInfo;

    private String userId;
    private ApiService apiService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        // Get user ID from intent
        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initData();
        loadUserDetail();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvAddress = findViewById(R.id.tvAddress);
        tvRole = findViewById(R.id.tvRole);
        tvStatus = findViewById(R.id.tvStatus);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        tvLastLogin = findViewById(R.id.tvLastLogin);
        imgAvatar = findViewById(R.id.imgAvatar);
        imgStatus = findViewById(R.id.imgStatus);
        progressBar = findViewById(R.id.progressBar);
        cardViewUserInfo = findViewById(R.id.cardViewUserInfo);
    }

    private void initData() {
        apiService = RetrofitClient.getInstance().getApiService();
        authManager = AuthManager.getInstance(this);
    }

    private void loadUserDetail() {
        // Check if user has permission
        if (!authManager.isStaff()) {
            Toast.makeText(this, "Bạn không có quyền truy cập chức năng này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading(true);
        
        String authHeader = authManager.getAuthHeader();
        if (authHeader == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Call<ApiResponse<User>> call = apiService.getUser(authHeader, userId);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getUser() != null) {
                        displayUserDetail(apiResponse.getUser());
                    } else {
                        showError("Không thể tải thông tin người dùng");
                    }
                } else {
                    showError("Lỗi kết nối: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void displayUserDetail(User user) {
        // Username
        tvUsername.setText(user.getUsername());

        // Email
        tvEmail.setText(user.getEmail());

        // Phone Number
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            tvPhoneNumber.setText(user.getPhoneNumber());
            tvPhoneNumber.setVisibility(View.VISIBLE);
        } else {
            tvPhoneNumber.setVisibility(View.GONE);
        }

        // Address
        if (user.getAddress() != null && !user.getAddress().isEmpty()) {
            tvAddress.setText(user.getAddress());
            tvAddress.setVisibility(View.VISIBLE);
        } else {
            tvAddress.setVisibility(View.GONE);
        }

        // Role
        String role = user.getRole();
        tvRole.setText(getRoleDisplayName(role));
        setRoleColor(tvRole, role);

        // Status
        boolean isActive = user.isActive();
        tvStatus.setText(isActive ? "Hoạt động" : "Tạm khóa");
        tvStatus.setTextColor(isActive ? 
            getResources().getColor(android.R.color.holo_green_dark) : 
            getResources().getColor(android.R.color.holo_red_dark));
        
        imgStatus.setImageResource(isActive ? R.drawable.ic_home : R.drawable.ic_search);
        imgStatus.setColorFilter(isActive ? 
            getResources().getColor(android.R.color.holo_green_dark) : 
            getResources().getColor(android.R.color.holo_red_dark));

        // Last login
        if (user.getLastLogin() != null && !user.getLastLogin().isEmpty()) {
            tvLastLogin.setText("Đăng nhập cuối: " + formatDate(user.getLastLogin()));
        } else {
            tvLastLogin.setText("Chưa đăng nhập");
        }

        // Created date
        if (user.getCreatedAt() != null && !user.getCreatedAt().isEmpty()) {
            tvCreatedAt.setText("Ngày tạo: " + formatDate(user.getCreatedAt()));
        } else {
            tvCreatedAt.setText("");
        }

        // Show user info card
        cardViewUserInfo.setVisibility(View.VISIBLE);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            cardViewUserInfo.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    private String getRoleDisplayName(String role) {
        switch (role) {
            case "admin":
                return "Quản trị viên";
            case "staff":
                return "Nhân viên";
            case "customer":
                return "Khách hàng";
            default:
                return role;
        }
    }

    private void setRoleColor(TextView textView, String role) {
        int color;
        switch (role) {
            case "admin":
                color = getResources().getColor(android.R.color.holo_red_dark);
                break;
            case "staff":
                color = getResources().getColor(android.R.color.holo_blue_dark);
                break;
            case "customer":
                color = getResources().getColor(android.R.color.holo_green_dark);
                break;
            default:
                color = getResources().getColor(android.R.color.darker_gray);
                break;
        }
        textView.setTextColor(color);
    }

    private String formatDate(String dateString) {
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
            java.util.Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }
}
