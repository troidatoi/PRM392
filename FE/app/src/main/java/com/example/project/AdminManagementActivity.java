package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AdminNavHelper;
import com.example.project.utils.AuthManager;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminManagementActivity extends AppCompatActivity {

    private TextView tvTotalSales, tvTotalSalesLabel, tvOrderUser, tvTotalProducts, tvTotalUsers;
    private LinearLayout navDashboard, navUserManagement, navProductManagement, navStoreManagement, navOrderManagement, navChatManagement;
    private ImageView iconDashboard, iconUserManagement, iconProductManagement, iconStoreManagement, iconOrderManagement, iconChatManagement;
    private TextView tvDashboard, tvUserManagement, tvProductManagement, tvStoreManagement, tvOrderManagement, tvChatManagement;
    private CardView cardDashboard, cardUserManagement, cardProductManagement, cardStoreManagement, cardOrderManagement, cardChatManagement;
    
    private ApiService apiService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_management);

        initViews();
        initData();
        loadStatistics();
        setupClickListeners();
    }
    
    private void initData() {
        apiService = RetrofitClient.getInstance().getApiService();
        authManager = AuthManager.getInstance(this);
    }

    private void initViews() {
        tvTotalSales = findViewById(R.id.tvTotalSales);
        tvTotalSalesLabel = findViewById(R.id.tvTotalSalesLabel);
        tvOrderUser = findViewById(R.id.tvOrderUser);
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        
        // Ensure label is visible and set text
        if (tvTotalSalesLabel != null) {
            tvTotalSalesLabel.setText("Tổng doanh thu");
            tvTotalSalesLabel.setVisibility(android.view.View.VISIBLE);
        }

        // Bottom navigation views
        navDashboard = findViewById(R.id.navDashboard);
        navUserManagement = findViewById(R.id.navUserManagement);
        navProductManagement = findViewById(R.id.navProductManagement);
        navStoreManagement = findViewById(R.id.navStoreManagement);
        navOrderManagement = findViewById(R.id.navOrderManagement);
        navChatManagement = findViewById(R.id.navChatManagement);

        iconDashboard = findViewById(R.id.iconDashboard);
        iconUserManagement = findViewById(R.id.iconUserManagement);
        iconProductManagement = findViewById(R.id.iconProductManagement);
        iconStoreManagement = findViewById(R.id.iconStoreManagement);
        iconOrderManagement = findViewById(R.id.iconOrderManagement);
        iconChatManagement = findViewById(R.id.iconChatManagement);

        tvDashboard = findViewById(R.id.tvDashboard);
        tvUserManagement = findViewById(R.id.tvUserManagement);
        tvProductManagement = findViewById(R.id.tvProductManagement);
        tvStoreManagement = findViewById(R.id.tvStoreManagement);
        tvOrderManagement = findViewById(R.id.tvOrderManagement);
        tvChatManagement = findViewById(R.id.tvChatManagement);

        cardDashboard = findViewById(R.id.cardDashboard);
        cardUserManagement = findViewById(R.id.cardUserManagement);
        cardProductManagement = findViewById(R.id.cardProductManagement);
        cardStoreManagement = findViewById(R.id.cardStoreManagement);
        cardOrderManagement = findViewById(R.id.cardOrderManagement);
        cardChatManagement = findViewById(R.id.cardChatManagement);

        // Mark current tab active with gradient background
        setActiveTab(cardDashboard, iconDashboard, tvDashboard);
    }

    private void setActiveTab(CardView activeCard, ImageView activeIcon, TextView activeText) {
        // Reset all tabs to inactive state
        AdminNavHelper.resetAllTabs(
                cardDashboard, iconDashboard, tvDashboard,
                cardUserManagement, iconUserManagement, tvUserManagement,
                cardProductManagement, iconProductManagement, tvProductManagement,
                cardStoreManagement, iconStoreManagement, tvStoreManagement,
                cardOrderManagement, iconOrderManagement, tvOrderManagement,
                cardChatManagement, iconChatManagement, tvChatManagement
        );

        // Set active tab
        AdminNavHelper.setActiveTab(activeCard, activeIcon, activeText);
    }

    private void resetTab(CardView card, ImageView icon, TextView text) {
        AdminNavHelper.resetTab(card, icon, text);
    }

    private void loadStatistics() {
        // Get auth token
        String token = authManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String authHeader = "Bearer " + token;
        
        // Call API to get total revenue
        Call<ApiService.RevenueResponse> call = apiService.getTotalRevenue(
            authHeader,
            null,  // startDate
            null,  // endDate
            null   // storeId
        );
        
        call.enqueue(new Callback<ApiService.RevenueResponse>() {
            @Override
            public void onResponse(Call<ApiService.RevenueResponse> call, Response<ApiService.RevenueResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.RevenueResponse revenueResponse = response.body();
                    
                    if (revenueResponse.isSuccess() && revenueResponse.getData() != null) {
                        ApiService.RevenueData revenueData = revenueResponse.getData();
                        
                        // Format and display total revenue
                        double totalRevenue = revenueData.getTotalRevenue();
                        String formattedRevenue = formatRevenue(totalRevenue);
                        tvTotalSales.setText(formattedRevenue);
                        
                        // Display total orders
                        int totalOrders = revenueData.getTotalOrders();
                        tvOrderUser.setText(String.valueOf(totalOrders));
                        
                        // TODO: Load total products and total users from other APIs
                        // For now, keep default values
                        tvTotalProducts.setText("0");
                        tvTotalUsers.setText("0");
                    } else {
                        // Show error message
                        showError("Không thể tải dữ liệu thống kê");
                    }
                } else {
                    showError("Lỗi kết nối server");
                }
            }
            
            @Override
            public void onFailure(Call<ApiService.RevenueResponse> call, Throwable t) {
                showError("Lỗi kết nối: " + t.getMessage());
                // Show default values on error
                tvTotalSales.setText("0");
                tvOrderUser.setText("0");
            }
        });
    }
    
    private String formatRevenue(double revenue) {
        // Format VND with thousand separators
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        
        // If revenue is very large, use abbreviated format
        if (revenue >= 1_000_000_000) {
            // Format as billions (tỷ) - e.g., 1.5T VND
            double billions = revenue / 1_000_000_000;
            if (billions == (long) billions) {
                return String.format(Locale.getDefault(), "%.0fT", billions);
            } else {
                return String.format(Locale.getDefault(), "%.1fT", billions);
            }
        } else if (revenue >= 1_000_000) {
            // Format as millions (triệu) - e.g., 1.5M VND
            double millions = revenue / 1_000_000;
            if (millions == (long) millions) {
                return String.format(Locale.getDefault(), "%.0fM", millions);
            } else {
                return String.format(Locale.getDefault(), "%.1fM", millions);
            }
        } else if (revenue >= 1_000) {
            // Format as thousands (nghìn) - e.g., 1.5K VND
            double thousands = revenue / 1_000;
            if (thousands == (long) thousands) {
                return String.format(Locale.getDefault(), "%.0fK", thousands);
            } else {
                return String.format(Locale.getDefault(), "%.1fK", thousands);
            }
        } else {
            // Format with thousand separators for small amounts
            return formatter.format(revenue);
        }
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setupClickListeners() {
        // No management buttons in new design - only bottom navigation

        // Bottom navigation actions
        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> {
                // Already here
            });
        }
        if (navUserManagement != null) {
            navUserManagement.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManagementActivity.this, UserManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navProductManagement != null) {
            navProductManagement.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManagementActivity.this, ProductManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navStoreManagement != null) {
            navStoreManagement.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManagementActivity.this, StoreManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navOrderManagement != null) {
            navOrderManagement.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManagementActivity.this, OrderManagementActivity.class);
                startActivity(intent);
                finish();
            });
        }
        if (navChatManagement != null) {
            navChatManagement.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManagementActivity.this, AdminChatListActivity.class);
                startActivity(intent);
            });
        }
    }
}
